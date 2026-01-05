package com.example.stamp.reporter.workflows.workers

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkerManagement(
    private val workflowRepository: WorkflowRepository,
    private val workerRepository: WorkerRepository,
    private val timeProvider: TimeProvider,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    fun cleanupAnyExpiredHeartBeat() {
        try {
            val offsetDateTimeNow = timeProvider.offsetDateTimeNowSystem()
            val expiredWorker =
                workerRepository.findFirstByExpireHeartBeatAtBefore(offsetDateTimeNow)
                    ?: return
            logger.info(
                "Cleaning up worker: {} - {}. Time now = {}, expire heartbeat of the expired worker = {}",
                expiredWorker.id,
                expiredWorker.hostname,
                offsetDateTimeNow,
                expiredWorker.expireHeartBeatAt,
            )
            // Clean up any reserved workflows first in transaction
            workflowRepository.findAllByWorkerIdIs(expiredWorker.id).forEach { workflow ->
                workflow.worker = null
            }
            workerRepository.delete(expiredWorker)
        } catch (e: Exception) {
            logger.error("Failed to cleanup any expired heart beat: ${e.message}")
            throw e
        }
    }
}
