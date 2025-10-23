package com.example.stamp.reporter.workflows.scheduled

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.entities.Worker
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.workers.WorkerManagement
import com.example.stamp.reporter.workflows.workers.WorkerProcessor
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.system.exitProcess

@Service
class WorkerAssignment(
    workerRepository: WorkerRepository,
    private val workerManagement: WorkerManagement,
    timeProvider: TimeProvider,
    private val workerProcessor: WorkerProcessor,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    var workerId by Delegates.notNull<Long>()

    init {
        workerId =
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
    }

    @Scheduled(fixedDelay = 1000)
    fun scheduledUpdateWorker() {
        try {
            workerManagement.assignWorker(workerId)
        } catch (e: Exception) {
            logger.error("Failed to assign worker: ${e.message}")
        }
    }

    @Scheduled(fixedDelay = 1000)
    fun scheduledUpdateWorkerHeartBeat() {
        try {
            workerManagement.updateHeartBeat(workerId)
        } catch (e: Exception) {
            logger.error("Failed to update worker heartbeat: ${e.message} exiting hard...")
            exitProcess(-1)
        }
    }

    @Scheduled(fixedDelay = 1000)
    fun cleanupAnyExpiredHeartBeat() {
        workerManagement.cleanupAnyExpiredHeartBeat()
    }

    @Scheduled(fixedDelay = 1000)
    fun processAnyWorkflow() {
        try {
            if (!workerManagement.workflowIdLock.tryLock(800, TimeUnit.MILLISECONDS)) return
            workerProcessor.processAnyWorkflow()
        } finally {
            workerManagement.workflowIdLock.unlock()
        }
    }
}
