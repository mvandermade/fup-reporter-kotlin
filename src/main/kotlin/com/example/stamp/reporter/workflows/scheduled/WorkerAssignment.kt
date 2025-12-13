package com.example.stamp.reporter.workflows.scheduled

import com.example.stamp.reporter.workflows.workers.WorkerManagement
import com.example.stamp.reporter.workflows.workers.WorkerStarter
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

sealed class WorkResult {
    object WorkerBusy : WorkResult()

    object Success : WorkResult()

    object Failure : WorkResult()
}

@Service
class WorkerAssignment(
    private val workerManagement: WorkerManagement,
    private val workerStarter: WorkerStarter,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Every second make sure the worker picks something up if it's idling
    @Scheduled(fixedDelay = 1000)
    fun scheduledUpdateWorker() {
        logger.trace("Scheduled Update Worker increment work each 1000ms")
        workerStarter.incrementWork()
    }

    @Scheduled(fixedDelay = 1000, initialDelay = 5000)
    fun scheduledUpdateWorkerHeartBeat() {
        try {
            workerStarter.updateHeartBeat()
        } catch (e: Exception) {
            logger.error("Failed to update worker heartbeat: ${e.message}...", e)
        }
    }

    @Scheduled(fixedDelay = 1000)
    fun cleanupAnyExpiredHeartBeat() {
        workerManagement.cleanupAnyExpiredHeartBeat()
    }

    // In the case a poke did not arrive
    @Scheduled(fixedDelay = 2000)
    fun pokeWorker() {
        workerStarter.poke()
    }
}
