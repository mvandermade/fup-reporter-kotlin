package com.example.stamp.reporter.workflows.workers

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.locks.ReentrantLock

private const val HEARTBEAT_ADD_SECONDS = 60L

sealed class WorkerAssignmentResult {
    object WorkerAlreadyAssigned : WorkerAssignmentResult()

    object NoWorkflowFound : WorkerAssignmentResult()

    object Assigned : WorkerAssignmentResult()
}

@Service
class WorkerManagement(
    private val workflowRepository: WorkflowRepository,
    private val workerRepository: WorkerRepository,
    private val timeProvider: TimeProvider,
    private val workerId: WorkerId,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    var workflowId: Long? = null

    // Prevent thread racing and use it as a queue
    val workflowIdLock = ReentrantLock(true)

    @Transactional(rollbackFor = [Exception::class])
    fun assignWorker(): WorkerAssignmentResult {
        if (workflowId != null) return WorkerAssignmentResult.WorkerAlreadyAssigned
        val newWorkflow =
            workflowRepository.findFirstByWorkerIsNull()
                ?: return WorkerAssignmentResult.NoWorkflowFound

        val worker = workerRepository.getReferenceById(workerId.workerId)
        newWorkflow.worker = worker
        workflowId = workflowRepository.save(newWorkflow).id
        logger.info("workflowId is now: $workflowId")
        return WorkerAssignmentResult.Assigned
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateHeartBeat() {
        val worker =
            workerRepository.findByIdOrNull(workerId.workerId)
                ?: throw Exception("Worker with id ${workerId.workerId} not found")

        worker.expireHeartBeatAt = timeProvider.offsetDateTimeNowSystem().plusSeconds(HEARTBEAT_ADD_SECONDS)
        logger.info("Updated expire heartbeat of worker ${worker.hostname} to ${worker.expireHeartBeatAt}")
        workerRepository.save(worker)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun cleanupAnyExpiredHeartBeat() {
        try {
            val offsetDateTimeNow = timeProvider.offsetDateTimeNowSystem()
            val expiredWorker =
                workerRepository.findFirstByExpireHeartBeatAtBefore(offsetDateTimeNow)
                    ?: return
            logger.info(
                "Cleaning up worker: ${expiredWorker.hostname}. Time now = $offsetDateTimeNow, " +
                    "expire heartbeat of the expired worker = ${expiredWorker.expireHeartBeatAt}",
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
