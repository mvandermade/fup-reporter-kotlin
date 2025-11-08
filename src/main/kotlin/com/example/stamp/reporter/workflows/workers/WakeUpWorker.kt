package com.example.stamp.reporter.workflows.workers

import com.example.stamp.reporter.workflows.scheduled.WorkResult
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock

// Keep this small its only job is to wake up the worker
const val AWAIT_WAKEUP_TIMEOUT_MS = 1000L
const val WAIT_FOR_WORKER_TIMEOUT_MS = 20L

@Component
class WakeUpWorker(
    private val workerManagement: WorkerManagement,
    @param:Qualifier("applicationTaskExecutor") private val taskExecutor: TaskExecutor,
    private val workerProcessor: WorkerProcessor,
) {
    private val lock = ReentrantLock(true)
    private val wakeUpCondition = lock.newCondition()
    private val queueDepthCounter = AtomicLong(0)

    private val logger = LoggerFactory.getLogger(javaClass)

    private val isShuttingDown = AtomicBoolean(false)

    @EventListener(ApplicationReadyEvent::class)
    fun startWakeUpListener() {
        taskExecutor.execute {
            logger.info("Starting wake-up listener thread")
            awaitWakeUp()
            logger.info("Wake-up listener thread terminated")
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
                    logger.info("Queue depth is ${queueDepthCounter.get()}, trying to assign a worker...")
                    for (i in 0..<queueDepthCounter.get()) {
                        queueDepthCounter.decrementAndGet()
                        when (workerManagement.assignWorker()) {
                            WorkerAssignmentResult.Assigned -> {
                                logger.info("Assigned a worker! Proceeding to do work...")
                                doWork()
                                logger.info("Work done, going to await next wakeup")
                            }
                            WorkerAssignmentResult.NoWorkflowFound -> {
                                logger.info("No workflow found, going to await next wakeup")
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
        val acquired = workerManagement.workflowIdLock.tryLock(800, TimeUnit.MILLISECONDS)
        if (!acquired) return WorkResult.WorkerBusy
        return try {
            workerProcessor.processAnyWorkflow()
            WorkResult.Success
        } catch (e: Exception) {
            logger.error("Failed to process any workflow: ${e.message}")
            WorkResult.Failure
        } finally {
            workerManagement.workflowIdLock.unlock()
        }
    }
}
