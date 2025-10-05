package com.example.stamp.reporter.workflows.executors

import com.example.stamp.reporter.apicallers.feign.StampServerApi
import com.example.stamp.reporter.domain.mappers.StampCodeMapper
import com.example.stamp.reporter.workflows.domain.SendToExchangeInput1
import com.example.stamp.reporter.workflows.domain.WorkflowResult
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SendToExchangeExecutor(
    private val stampServerApi: StampServerApi,
    private val stampCodeMapper: StampCodeMapper,
) {
    private val objectMapper =
        with(jacksonObjectMapper()) {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

    private val logger = LoggerFactory.getLogger("SendToExchangeExecutor")

    fun step1(rawInput: String): WorkflowResult {
        val input = objectMapper.readValue<SendToExchangeInput1>(rawInput)
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
