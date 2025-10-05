package com.example.stamp.reporter.workflows.scheduled

import com.example.stamp.reporter.workflows.domain.WorkflowResult
import com.example.stamp.reporter.workflows.executors.SendToExchangeExecutor
import com.example.stamp.reporter.workflows.processors.WorkflowStepMarker
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SendToExchangeScheduled(
    private val sendToExchangeExecutor: SendToExchangeExecutor,
    private val workflowStepRepository: WorkflowStepRepository,
    private val workflowStepMarker: WorkflowStepMarker,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 1000)
    fun sendToExchangeStep1() {
        val workflowStep =
            workflowStepRepository.findFirstByOutputIsNullAndErrorMessageIsNull()
                ?: return

        when (val result = sendToExchangeExecutor.step1(workflowStep.input)) {
            is WorkflowResult.Success -> {
                workflowStepMarker.save(workflowStep.id, result)
                logger.info("SendToExchangeStep1: OK $workflowStep")
            }
            is WorkflowResult.Error -> {
                workflowStepMarker.save(workflowStep.id, result)
                logger.info("SendToExchangeStep1: NOK $workflowStep")
            }
        }
    }

    @Scheduled(fixedRate = 1000)
    fun findStepsWithErrors() {
        val workflowSteps = workflowStepRepository.countByErrorMessageIsNotNull()
        logger.info("Found $workflowSteps steps with errors")
    }
}
