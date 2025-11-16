package com.example.stamp.reporter.workflows.workers

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.providers.TransactionProvider
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.example.stamp.reporter.workflows.services.SendToExchangeService
import com.example.stamp.reporter.workflows.services.WorkflowService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

@Service
@Profile("!test")
class WorkerStarter(
    @param:Qualifier("applicationTaskExecutor") private val taskExecutor: TaskExecutor,
    private val workflowRepository: WorkflowRepository,
    private val workflowStepRepository: WorkflowStepRepository,
    private val sendToExchangeService: SendToExchangeService,
    private val workflowService: WorkflowService,
    private val workerRepository: WorkerRepository,
    private val timeProvider: TimeProvider,
    private val transactionProvider: TransactionProvider,
) {
    private val lock1 = ReentrantLock(true)
    private val lock2 = ReentrantLock(true)
    private val wakeUpCondition1 = lock1.newCondition()
    private val wakeUpCondition2 = lock2.newCondition()
    private val queueDepthCounter1 = AtomicLong(0)
    private val queueDepthCounter2 = AtomicLong(0)
    private val isShuttingDown = AtomicBoolean(false)

    private lateinit var workerDaemon1: WorkerDaemon
    private lateinit var workerDaemon2: WorkerDaemon

    @EventListener(ApplicationReadyEvent::class)
    fun startWakeUpListener() {
        taskExecutor.execute {
            workerDaemon1 = newWorkerDaemon(lock1, wakeUpCondition1, queueDepthCounter1, isShuttingDown)
            workerDaemon1.awaitWakeUp()
        }
        taskExecutor.execute {
            workerDaemon2 = newWorkerDaemon(lock2, wakeUpCondition2, queueDepthCounter2, isShuttingDown)
            workerDaemon2.awaitWakeUp()
        }
    }

    @EventListener(ContextClosedEvent::class)
    fun onContextClosedEvent(contextClosedEvent: ContextClosedEvent) {
        println("ContextClosedEvent occurred at millis: " + contextClosedEvent.getTimestamp())
        isShuttingDown.set(true)
    }

    fun poke() {
        taskExecutor.execute {
            poke(lock2, wakeUpCondition2)
        }
        taskExecutor.execute {
            poke(lock1, wakeUpCondition1)
        }
    }

    fun poke(
        lock: ReentrantLock,
        wakeUpCondition: Condition,
    ) {
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
        queueDepthCounter1.incrementAndGet()
        queueDepthCounter2.incrementAndGet()
        poke()
    }

    fun updateHeartBeat() {
        taskExecutor.execute {
            workerDaemon1.updateHeartBeat()
        }
        taskExecutor.execute {
            workerDaemon2.updateHeartBeat()
        }
    }

    fun newWorkerDaemon(
        lock: ReentrantLock,
        wakeUpCondition: Condition,
        queueDepthCounter: AtomicLong,
        isShuttingDown: AtomicBoolean,
    ) = WorkerDaemon(
        workflowRepository = workflowRepository,
        workflowStepRepository = workflowStepRepository,
        sendToExchangeService = sendToExchangeService,
        workflowService = workflowService,
        workerRepository = workerRepository,
        timeProvider = timeProvider,
        transactionProvider = transactionProvider,
        lock = lock,
        wakeUpCondition = wakeUpCondition,
        queueDepthCounter = queueDepthCounter,
        isShuttingDown = isShuttingDown,
    )
}
