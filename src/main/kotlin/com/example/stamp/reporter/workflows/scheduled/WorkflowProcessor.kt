package com.example.stamp.reporter.workflows.scheduled

import com.example.stamp.reporter.workflows.domain.WorkflowResult
import com.example.stamp.reporter.workflows.model.WorkflowType
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.example.stamp.reporter.workflows.services.SendToExchangeService
import com.example.stamp.reporter.workflows.services.WorkerService
import com.example.stamp.reporter.workflows.services.WorkflowService
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class WorkflowProcessor(
    private val sendToExchangeService: SendToExchangeService,
    private val workflowStepRepository: WorkflowStepRepository,
    private val workflowRepository: WorkflowRepository,
    private val workerService: WorkerService,
    private val workflowService: WorkflowService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    fun processWorkflow() {
        val workflowId =
            workerService.workflowId
                ?: return
        val workflow = workflowRepository.findByIdOrNull(workflowId)
        if (workflow == null) {
            logger.info("Workflow with id $workflowId not found, possibly tombstoned or errored out, resetting worker")
            workerService.workflowId = null
            return
        }
        val workflowStep =
            workflowStepRepository.findFirstByWorkflowIdAndStepNumber(workflowId, workflow.programCounter)
                ?: return logger.info("WorkflowStep with step ${workflow.programCounter} not found for workflowId $workflowId")

        when (workflow.type) {
            WorkflowType.SEND_TO_EXCHANGE -> {
                logger.info("Processing workflow of type SEND_TO_EXCHANGE")
                val result = sendToExchangeService.doNext(workflowId, workflow.programCounter, workflowStep.input)
                when (result) {
                    is WorkflowResult.Success -> workflowService.markSuccess(workflowId, workflowStep.callback)
                    is WorkflowResult.Error -> workflowService.markError(workflowId)
                }
            }
        }
    }
}
