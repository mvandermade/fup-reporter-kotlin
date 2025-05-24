package com.example.stamp.reporter.websockets.observers

import com.example.stamp.reporter.domain.kafka.TOPIC_SERIAL_STAMP
import com.example.stamp.reporter.domain.messages.StampCodeDTO
import com.example.stamp.reporter.websockets.domain.WebSocketSerialEventMessage
import com.example.stamp.reporter.websockets.handlers.TrackerWebsocketHandler
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service

@Service
class KafkaObserver(private val trackerWebsocketHandler: TrackerWebsocketHandler) {
    private val objectMapper =
        with(jacksonObjectMapper()) {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        id = "kafkaObserver",
        topics = [TOPIC_SERIAL_STAMP],
    )
    fun observeSerialMessages(
        @Payload message: String,
    ) {
        val stampCodeDTO = objectMapper.readValue(message, StampCodeDTO::class.java)

        logger.info("Read Serial Event: $stampCodeDTO")

        trackerWebsocketHandler.sendAll(WebSocketSerialEventMessage(code = stampCodeDTO.code))
    }
}
