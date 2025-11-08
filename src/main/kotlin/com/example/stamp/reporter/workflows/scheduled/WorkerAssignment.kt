package com.example.stamp.reporter.workflows.scheduled

import com.example.stamp.reporter.workflows.workers.WakeUpWorker
import com.example.stamp.reporter.workflows.workers.WorkerManagement
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.system.exitProcess

sealed class WorkResult {
    object WorkerBusy : WorkResult()

    object Success : WorkResult()

    object Failure : WorkResult()
}

@Service
class WorkerAssignment(
    private val workerManagement: WorkerManagement,
    private val wakeUpWorker: WakeUpWorker,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Every second make sure the worker picks something up if it's idling
    @Scheduled(fixedDelay = 1000)
    fun scheduledUpdateWorker() {
        logger.info("Scheduled Update Worker increment work each 1000ms")
        wakeUpWorker.incrementWork()
    }

    @Scheduled(fixedDelay = 1000)
    fun scheduledUpdateWorkerHeartBeat() {
        try {
            workerManagement.updateHeartBeat()
        } catch (e: Exception) {
            logger.error("Failed to update worker heartbeat: ${e.message} exiting hard...", e)
            exitProcess(-1)
        }
    }

    @Scheduled(fixedDelay = 1000)
    fun cleanupAnyExpiredHeartBeat() {
        workerManagement.cleanupAnyExpiredHeartBeat()
    }

    // In the case a poke did not arrive
    @Scheduled(fixedDelay = 2000)
    fun pokeWorker() {
        wakeUpWorker.poke()
    }
}
