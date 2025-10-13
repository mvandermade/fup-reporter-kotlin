package com.example.stamp.reporter.workflows.scheduled

import com.example.stamp.reporter.workflows.entities.Worker
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.services.WorkerService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.properties.Delegates
import kotlin.system.exitProcess

@Service
class WorkerAssignmentScheduled(
    private val workflowRepository: WorkflowRepository,
    private val workerRepository: WorkerRepository,
    private val workerService: WorkerService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    var workerId by Delegates.notNull<Long>()

    init {
        workerId =
            try {
                workerRepository.save(Worker("localhost")).id
            } catch (e: Exception) {
                logger.error("Failed to save worker: ${e.message}")
                exitProcess(-1)
            }
    }

    @Scheduled(fixedRate = 1000)
    fun scheduledUpdateWorker() {
        try {
            workerService.assignWorker(workerId)
        } catch (e: Exception) {
            logger.error("Failed to assign worker: ${e.message}")
        }
    }
}
