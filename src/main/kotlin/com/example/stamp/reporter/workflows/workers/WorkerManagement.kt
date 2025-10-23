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

@Service
class WorkerManagement(
    private val workflowRepository: WorkflowRepository,
    private val workerRepository: WorkerRepository,
    private val timeProvider: TimeProvider,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    var workflowId: Long? = null

    // Prevent thread racing and use it as a queue
    val workflowIdLock = ReentrantLock(true)

    @Transactional(rollbackFor = [Exception::class])
    fun assignWorker(workerId: Long) {
        if (workflowId != null) return
        val newWorkflow =
            workflowRepository.findFirstByWorkerIsNull()
                ?: return

        val worker = workerRepository.getReferenceById(workerId)
        newWorkflow.worker = worker
        workflowId = workflowRepository.save(newWorkflow).id
        logger.info("workflowId is now: $workflowId")
    }

    @Transactional(rollbackFor = [Exception::class])
    fun updateHeartBeat(workerId: Long) {
        val worker =
            workerRepository.findByIdOrNull(workerId)
                ?: throw Exception("Worker with id $workerId not found")

        worker.expireHeartBeatAt = timeProvider.offsetDateTimeNowSystem().plusSeconds(HEARTBEAT_ADD_SECONDS)
        workerRepository.save(worker)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun cleanupAnyExpiredHeartBeat() {
        val worked =
            workerRepository.findFirstByExpireHeartBeatAtBefore(timeProvider.offsetDateTimeNowSystem())
                ?: return
        logger.info("Cleanup worker: ${worked.hostname}")
        // Clean up any reserved workflows first in transaction
        workflowRepository.findAllByWorkerIdIs(worked.id).forEach { workflow ->
            workflow.worker = null
        }
        workerRepository.delete(worked)
    }
}
