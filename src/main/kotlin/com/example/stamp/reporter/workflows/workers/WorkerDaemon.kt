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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.task.TaskExecutor
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.system.exitProcess

// Keep this small its only job is to wake up the worker
const val AWAIT_WAKEUP_TIMEOUT_MS = 1000L
private const val HEARTBEAT_ADD_SECONDS = 60L

sealed class WorkerAssignmentResult {
    object WorkerAlreadyAssigned : WorkerAssignmentResult()

    object NoWorkflowFound : WorkerAssignmentResult()

    object Assigned : WorkerAssignmentResult()
}

@Component
class WorkerDaemon(
    @param:Qualifier("applicationTaskExecutor") private val taskExecutor: TaskExecutor,
    private val workflowRepository: WorkflowRepository,
    private val workflowStepRepository: WorkflowStepRepository,
    private val sendToExchangeService: SendToExchangeService,
    private val workflowService: WorkflowService,
    private val workerRepository: WorkerRepository,
    private val timeProvider: TimeProvider,
    private val transactionProvider: TransactionProvider,
) {
    private val lock = ReentrantLock(true)
    private val wakeUpCondition = lock.newCondition()
    private val queueDepthCounter = AtomicLong(0)

    private val logger = LoggerFactory.getLogger(javaClass)

    private val isShuttingDown = AtomicBoolean(false)

    val workerId =
        try {
            workerRepository
                .save(
                    Worker(
                        hostname = "localhost",
                        expireHeartBeatAt = timeProvider.offsetDateTimeNowSystem(),
                    ),
                ).id
        } catch (e: Exception) {
            logger.error("Failed to save worker: ${e.message}")
            exitProcess(-1)
        }

    @EventListener(ApplicationReadyEvent::class)
    fun startWakeUpListener() {
        taskExecutor.execute {
            awaitWakeUp()
        }
    }

    @EventListener(ContextClosedEvent::class)
    fun onContextClosedEvent(contextClosedEvent: ContextClosedEvent) {
        println("ContextClosedEvent occurred at millis: " + contextClosedEvent.getTimestamp())
        isShuttingDown.set(true)
    }

    fun incrementWork() {
        queueDepthCounter.incrementAndGet()
        poke()
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

    fun awaitWakeUp() {
        while (!isShuttingDown.get()) {
            try {
                lock.lock()
                // Important to give a timeout because otherwise the application cannot shut down gracefully
                wakeUpCondition.await(AWAIT_WAKEUP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                if (queueDepthCounter.get() > 0) {
//                    logger.info("Queue depth is ${queueDepthCounter.get()}, trying to assign a worker...")
                    for (i in 0..<queueDepthCounter.get()) {
                        queueDepthCounter.decrementAndGet()
                        when (assignWorker()) {
                            WorkerAssignmentResult.Assigned -> {
                                logger.info("Assigned a worker! Proceeding to do work...")
                                doWork()
                                logger.info("Work done, going to await next wakeup")
                            }
                            WorkerAssignmentResult.NoWorkflowFound -> {
//                                logger.info("No workflow found, going to await next wakeup")
                            }
                            WorkerAssignmentResult.WorkerAlreadyAssigned -> {
                                logger.info("Worker is already assigned, going to await next wakeup")
                                // Do work anyway reentrantlock prevents deadlock
                                doWork()
                            }
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
                logger.info("Successfully processed a workflow, took $duration ms")
            }

            WorkResult.Failure -> {
                val duration = System.currentTimeMillis() - time
                logger.info("Failed to process a workflow, took $duration ms")
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
                ?: return logger.info("WorkflowStep with step ${workflow.programCounter} not found for workflowId $localWorkflowId")

        when (workflow.type) {
            WorkflowType.SEND_TO_EXCHANGE -> {
                logger.info("Processing workflow of type SEND_TO_EXCHANGE")
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
                            else -> {}
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
        return transactionProvider.newReadWrite {
            if (workflowId != null) return@newReadWrite WorkerAssignmentResult.WorkerAlreadyAssigned
            val newWorkflow =
                workflowRepository.findFirstByWorkerIsNull()
                    ?: return@newReadWrite WorkerAssignmentResult.NoWorkflowFound

            val worker = workerRepository.getReferenceById(workerId)
            newWorkflow.worker = worker
            workflowId = workflowRepository.save(newWorkflow).id
            logger.info("workflowId is now: $workflowId")
            WorkerAssignmentResult.Assigned
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateHeartBeat() {
        val worker =
            workerRepository.findByIdOrNull(workerId)
                ?: throw Exception("Worker with id $workerId not found")

        worker.expireHeartBeatAt = timeProvider.offsetDateTimeNowSystem().plusSeconds(HEARTBEAT_ADD_SECONDS)
//        logger.info("Updated expire heartbeat of worker ${worker.hostname} to ${worker.expireHeartBeatAt}")
        workerRepository.save(worker)
    }
}
