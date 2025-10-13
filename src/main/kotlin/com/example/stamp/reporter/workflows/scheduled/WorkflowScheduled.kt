package com.example.stamp.reporter.workflows.scheduled

import com.example.stamp.reporter.workflows.model.WorkflowType
import com.example.stamp.reporter.workflows.processors.WorkflowStepRegistry
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.example.stamp.reporter.workflows.services.SendToExchangeService
import com.example.stamp.reporter.workflows.services.WorkerService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class WorkflowScheduled(
    private val sendToExchangeService: SendToExchangeService,
    private val workflowStepRepository: WorkflowStepRepository,
    private val workflowStepRegistry: WorkflowStepRegistry,
    private val workflowRepository: WorkflowRepository,
    private val workerRepository: WorkerRepository,
    private val workerService: WorkerService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    fun processWorkflow() {
        logger.info("Processing workflow")
        val workflowId =
            workerService.workflowId
                ?: return
        val workflow = workflowRepository.findByIdOrNull(workflowId)
        if (workflow == null) {
            logger.info("Workflow with id $workflowId not found")
            workerService.workflowId = null
            return
        }
        when (workflow.type) {
            WorkflowType.SEND_TO_EXCHANGE -> {
                logger.info("Processing workflow of type SEND_TO_EXCHANGE")
                sendToExchangeService.process(workflowId, workflow.programCounter)
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    fun findStepsWithErrors() {
        val workflowSteps = workflowStepRepository.countByErrorMessageIsNotNull()
        logger.info("Found $workflowSteps steps with errors")
    }
}
