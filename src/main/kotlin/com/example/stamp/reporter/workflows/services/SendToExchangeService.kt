package com.example.stamp.reporter.workflows.services

import com.example.stamp.reporter.apicallers.feign.StampServerApi
import com.example.stamp.reporter.domain.messages.ReadStampCode
import com.example.stamp.reporter.websockets.domain.WebSocketAckExchangeMessage
import com.example.stamp.reporter.websockets.domain.WebSocketPostExchangeMessage
import com.example.stamp.reporter.websockets.handlers.TrackerWebsocketHandler
import com.example.stamp.reporter.workflows.domain.WorkflowResult
import com.example.stamp.reporter.workflows.mappers.StampCodeMapper
import com.example.stamp.reporter.workflows.processors.WorkflowStepRegistry
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.time.measureTime

@Service
class SendToExchangeService(
    private val stampServerApi: StampServerApi,
    private val stampCodeMapper: StampCodeMapper,
    private val trackerWebsocketHandler: TrackerWebsocketHandler,
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
                val saveTime =
                    measureTime {
                        workflowStepRegistry.save(workflowStepId, result)
                    }
                logger.info("SendToExchangeStep1: OK $workflowStepId saving workflow took $saveTime ms")
            }
            is WorkflowResult.Error -> {
                workflowStepRegistry.save(workflowStepId, result)
                logger.info("SendToExchangeStep1: NOK stepId: $workflowStepId, message: ${result.message}")
            }
        }

        return result
    }

    fun sendToExchange(rawInput: String): WorkflowResult {
        val readStampCode = objectMapper.readValue<ReadStampCode>(rawInput)
        logger.info("Received input: $readStampCode")

        try {
            trackerWebsocketHandler.sendAll(WebSocketPostExchangeMessage(readStampCode.code))

            val time =
                measureTime {
                    stampServerApi.postStampCode(
                        stampCodeMapper.toRequest(readStampCode),
                        readStampCode.idempotencyKey,
                    )
                }
            logger.info("Sent stamp code to exchange in $time")

            trackerWebsocketHandler.sendAll(WebSocketAckExchangeMessage(readStampCode.code))
        } catch (e: Exception) {
            return WorkflowResult.Error("Failed to send stamp code to exchange: ${e.message}")
        }

        return WorkflowResult.Success("Successfully sent stamp code to exchange + notified WS")
    }
}
