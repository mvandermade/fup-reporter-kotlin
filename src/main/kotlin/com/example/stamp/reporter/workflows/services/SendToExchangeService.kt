package com.example.stamp.reporter.workflows.services

import com.example.stamp.reporter.apicallers.feign.StampServerApi
import com.example.stamp.reporter.domain.mappers.StampCodeMapper
import com.example.stamp.reporter.workflows.domain.ReadStampCode
import com.example.stamp.reporter.workflows.domain.WorkflowResult
import com.example.stamp.reporter.workflows.processors.WorkflowStepRegistry
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SendToExchangeService(
    private val stampServerApi: StampServerApi,
    private val stampCodeMapper: StampCodeMapper,
    private val workFlowStepRepository: WorkflowStepRepository,
    private val workflowStepRegistry: WorkflowStepRegistry,
) {
    private val objectMapper =
        with(jacksonObjectMapper()) {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

    private val logger = LoggerFactory.getLogger("SendToExchangeExecutor")

    fun process(
        workflowId: Long,
        step: Int,
    ) {
        val workflowStep =
            workFlowStepRepository.findFirstByWorkflowIdAndStepNumber(workflowId, step)
                ?: return logger.info("WorkflowStep with id $step not found")

        when (val result = step1(workflowStep.input)) {
            is WorkflowResult.Success -> {
                workflowStepRegistry.save(workflowStep.id, result)
                logger.info("SendToExchangeStep1: OK $workflowStep")
            }
            is WorkflowResult.Error -> {
                workflowStepRegistry.save(workflowStep.id, result)
                logger.info("SendToExchangeStep1: NOK $workflowStep")
            }
        }
    }

    fun step1(rawInput: String): WorkflowResult {
        val input = objectMapper.readValue<ReadStampCode>(rawInput)
        logger.info("Received input: $input")

        try {
            stampServerApi.postStampCode(
                stampCodeMapper.toRequest(input),
                input.idempotencyKey,
            )
        } catch (e: Exception) {
            return WorkflowResult.Error("Failed to send stamp code to exchange: ${e.message}")
        }

        return WorkflowResult.Success("Successfully sent stamp code to exchange")
    }
}
