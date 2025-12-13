package com.example.stamp.reporter.workflows.workers

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.providers.TransactionProvider
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.example.stamp.reporter.workflows.services.SendToExchangeService
import com.example.stamp.reporter.workflows.services.WorkflowService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicBoolean

@Service
class WorkerStarter(
    @param:Qualifier("applicationTaskExecutor") private val taskExecutor: TaskExecutor,
    private val workflowRepository: WorkflowRepository,
    private val workflowStepRepository: WorkflowStepRepository,
    private val sendToExchangeService: SendToExchangeService,
    private val workflowService: WorkflowService,
    private val workerRepository: WorkerRepository,
    private val timeProvider: TimeProvider,
    private val transactionProvider: TransactionProvider,
    @param:Value($$"${application.workers.amount:5}")
    private val numberOfWorkers: Int,
) {
    private val workerContexts = mutableListOf<WorkerContext>()

    private val appIsShuttingDown = AtomicBoolean(false)
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        repeat(numberOfWorkers) {
            val worker = newWorkerDaemon(appIsShuttingDown)
            val workerContext = WorkerContext(isShuttingDown = appIsShuttingDown, worker = worker)
            workerContexts.add(workerContext)
        }
    }

    @EventListener(ApplicationReadyEvent::class)
    fun spawnThreads() {
        workerContexts.forEachIndexed { index, it ->
            logger.info("WakeUp of thread $index with workerId ${it.worker.workerId}")
            taskExecutor.execute {
                it.worker.awaitWakeUp()
            }
        }
    }

    @EventListener(ContextClosedEvent::class)
    fun onContextClosedEvent(contextClosedEvent: ContextClosedEvent) {
        logger.warn(
            "ContextClosedEvent occurred at millis, notifying appIsShuttingDown ${contextClosedEvent.timestamp}",
        )
        appIsShuttingDown.set(true)
    }

    fun poke() {
        workerContexts.forEach {
            taskExecutor.execute {
                it.worker.poke()
            }
        }
    }

    fun incrementWork() {
        workerContexts.forEach {
            it.worker.incrementWork()
        }
    }

    fun updateHeartBeat() {
        workerContexts.forEach {
            it.worker.updateHeartBeat()
        }
    }

    fun newWorkerDaemon(isShuttingDown: AtomicBoolean) =
        WorkerDaemon(
            workflowRepository = workflowRepository,
            workflowStepRepository = workflowStepRepository,
            sendToExchangeService = sendToExchangeService,
            workflowService = workflowService,
            workerRepository = workerRepository,
            timeProvider = timeProvider,
            transactionProvider = transactionProvider,
            isShuttingDown = isShuttingDown,
        )
}
