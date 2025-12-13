package com.example.stamp.reporter.workflows.workers

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.providers.TransactionProvider
import com.example.stamp.reporter.workflows.domain.WorkflowResult
import com.example.stamp.reporter.workflows.entities.StepCallbackType
import com.example.stamp.reporter.workflows.entities.Worker
import com.example.stamp.reporter.workflows.model.WorkflowType
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.example.stamp.reporter.workflows.scheduled.WorkResult
import com.example.stamp.reporter.workflows.services.SendToExchangeService
import com.example.stamp.reporter.workflows.services.WorkflowService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.orm.ObjectOptimisticLockingFailureException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.properties.Delegates
import kotlin.system.exitProcess

// Keep this small its only job is to wake up the worker
const val AWAIT_WAKEUP_TIMEOUT_MS = 1000L
private const val HEARTBEAT_ADD_SECONDS = 60L

sealed class WorkerAssignmentResult {
    object WorkerAlreadyAssigned : WorkerAssignmentResult()

    object NoWorkflowFound : WorkerAssignmentResult()

    object Assigned : WorkerAssignmentResult()

    object OptimisticLockingFailure : WorkerAssignmentResult()
}

class WorkerDaemon(
    private val workflowRepository: WorkflowRepository,
    private val workflowStepRepository: WorkflowStepRepository,
    private val sendToExchangeService: SendToExchangeService,
    private val workflowService: WorkflowService,
    private val workerRepository: WorkerRepository,
    private val timeProvider: TimeProvider,
    private val transactionProvider: TransactionProvider,
    private val isShuttingDown: AtomicBoolean,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val lock = ReentrantLock(true)
    private val queueDepthCounter = AtomicLong(0)
    private val wakeUpCondition = lock.newCondition()

    var workerId by Delegates.notNull<Long>()

    init {
        workerId =
            try {
                workerRepository
                    .save(
                        Worker(
                            hostname = "localhost",
                            expireHeartBeatAt = timeProvider.offsetDateTimeNowSystem().plusSeconds(HEARTBEAT_ADD_SECONDS),
                        ),
                    ).id
            } catch (e: Exception) {
                logger.error("Failed to save worker: ${e.message}")
                exitProcess(-1)
            }
    }

    fun poke() {
        val acquired = lock.tryLock(0, TimeUnit.MILLISECONDS)
        // Loop is already running, no need to wake it up
        if (!acquired) return

        try {
            wakeUpCondition.signal()
        } finally {
            lock.unlock()
        }
    }

    fun incrementWork() {
        queueDepthCounter.incrementAndGet()
        poke()
    }

    fun awaitWakeUp() {
        while (!isShuttingDown.get()) {
            try {
                lock.lock()
                // Important to give a timeout because otherwise the application cannot shut down gracefully
                wakeUpCondition.await(AWAIT_WAKEUP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                if (queueDepthCounter.get() > 0 && !isShuttingDown.get()) {
                    var queueLine = queueDepthCounter.get()
                    while (queueLine > 0) {
                        // Stop the queue depth counter when application wants to quit
                        if (isShuttingDown.get()) break
                        queueLine = 0
                        queueDepthCounter.set(0)
                        when (assignWorker()) {
                            // Because of the condition no thread race to doWork is possible.
                            WorkerAssignmentResult.Assigned -> {
                                doWork()
                                queueLine = queueDepthCounter.incrementAndGet()
                            }
                            WorkerAssignmentResult.WorkerAlreadyAssigned -> {
                                doWork()
                                queueLine = queueDepthCounter.incrementAndGet()
                            }
                            WorkerAssignmentResult.NoWorkflowFound -> {}
                            WorkerAssignmentResult.OptimisticLockingFailure -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to wait for wakeup", e)
            } finally {
                lock.unlock()
            }
        }
    }

    private fun doWork() {
        val time = System.currentTimeMillis()
        when (processAnyWorkflow()) {
            WorkResult.WorkerBusy -> {
                logger.info("Worker busy, waiting")
            }

            WorkResult.Success -> {
                val duration = System.currentTimeMillis() - time
                logger.trace("Successfully processed a doWork(), took $duration ms")
            }

            WorkResult.Failure -> {
                val duration = System.currentTimeMillis() - time
                logger.trace("Failed to process a doWork(), took $duration ms")
            }
        }
    }

    fun processAnyWorkflow(): WorkResult {
        val acquired = workflowIdLock.tryLock(800, TimeUnit.MILLISECONDS)
        if (!acquired) return WorkResult.WorkerBusy
        return try {
            anyWorkflow()
            WorkResult.Success
        } catch (e: Exception) {
            logger.error("Failed to process any workflow: ", e)
            WorkResult.Failure
        } finally {
            workflowIdLock.unlock()
        }
    }

    // Prevent thread racing and use a lock around this function
    fun anyWorkflow() {
        val localWorkflowId = workflowId ?: return
        val workflow = workflowRepository.findByIdOrNull(localWorkflowId)
        if (workflow == null) {
            logger.info("Workflow with id $localWorkflowId not found, possibly tombstoned or errored out, resetting worker")
            workflowId = null
            return
        }

        val workflowStep =
            workflowStepRepository.findFirstByWorkflowIdAndStepNumber(localWorkflowId, workflow.programCounter)

        if (workflowStep == null) {
            workflowService.markError(localWorkflowId)
            return logger.trace(
                "Worker $workerId: WorkflowStep not found with step ${workflow.programCounter} " +
                    "for workflowId $localWorkflowId - marked as error",
            )
        }

        when (workflow.type) {
            WorkflowType.SEND_TO_EXCHANGE -> {
                logger.trace("Processing workflow $workflowId of type SEND_TO_EXCHANGE programCounter: ${workflow.programCounter}")
                val result = sendToExchangeService.doNext(workflowStep.id, workflow.programCounter, workflowStep.input)
                when (result) {
                    is WorkflowResult.Success -> {
                        workflowService.markSuccess(localWorkflowId, workflowStep.id, workflowStep.callback)
                        when (workflowStep.callback) {
                            StepCallbackType.TOMBSTONE -> {
                                try {
                                    if (workflowIdLock.tryLock(1000, TimeUnit.MILLISECONDS)) {
                                        workflowId = null
                                    }
                                } finally {
                                    workflowIdLock.unlock()
                                }
                            }

                            StepCallbackType.TAKE_NEXT -> {
                                queueDepthCounter.incrementAndGet()
                            }
                        }
                    }
                    is WorkflowResult.Error -> workflowService.markError(localWorkflowId)
                }
            }
        }
    }

    var workflowId: Long? = null

    // Prevent thread racing and use it as a queue
    val workflowIdLock = ReentrantLock(true)

    fun assignWorker(): WorkerAssignmentResult {
        var newWorkflowId: Long? = null
        try {
            val result =
                transactionProvider.newReadWrite {
                    if (workflowId != null) return@newReadWrite WorkerAssignmentResult.WorkerAlreadyAssigned

                    val newWorkflow =
                        workflowRepository.findFirstByWorkerIsNullOrderByIdAsc()
                            ?: return@newReadWrite WorkerAssignmentResult.NoWorkflowFound
                    val worker = workerRepository.getReferenceById(workerId)

                    newWorkflow.worker = worker
                    newWorkflowId = workflowRepository.save(newWorkflow).id

                    WorkerAssignmentResult.Assigned
                }

            return if (result == WorkerAssignmentResult.Assigned) {
                logger.info("[Assigned] setting worker $workerId workflowId to: $newWorkflowId")
                workflowId = newWorkflowId
                WorkerAssignmentResult.Assigned
            } else {
                logger.trace("[NotAssigned] Lock free result: ${result.javaClass.simpleName}")
                result
            }
        } catch (_: ObjectOptimisticLockingFailureException) {
            logger.trace("[NotAssigned] another worker optimistically locked workerId $workerId")
            return WorkerAssignmentResult.OptimisticLockingFailure
        }
    }

    fun updateHeartBeat() {
        transactionProvider.newReadWrite {
            val worker =
                workerRepository.findByIdOrNull(workerId)
                    ?: throw Exception("Worker with id $workerId not found")

            worker.expireHeartBeatAt = timeProvider.offsetDateTimeNowSystem().plusSeconds(HEARTBEAT_ADD_SECONDS)
            workerRepository.save(worker)
        }
    }
}
