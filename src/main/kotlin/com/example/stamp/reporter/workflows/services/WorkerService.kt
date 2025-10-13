package com.example.stamp.reporter.workflows.services

import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkerService(
    private val workflowRepository: WorkflowRepository,
    private val workerRepository: WorkerRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    var workflowId: Long? = null

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
}
