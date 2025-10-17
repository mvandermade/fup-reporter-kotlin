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

    fun doNext(
        workflowStepId: Long,
        step: Int,
        input: String,
    ): WorkflowResult {
        val result =
            when (step) {
                1 -> sendToExchange(input)
                else -> throw IllegalArgumentException("Unknown workflow step $step")
            }

        when (result) {
            is WorkflowResult.Success -> {
                workflowStepRegistry.save(workflowStepId, result)
                logger.info("SendToExchangeStep1: OK $workflowStepId")
            }
            is WorkflowResult.Error -> {
                workflowStepRegistry.save(workflowStepId, result)
                logger.info("SendToExchangeStep1: NOK $workflowStepId")
            }
        }

        return result
    }

    fun sendToExchange(rawInput: String): WorkflowResult {
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
