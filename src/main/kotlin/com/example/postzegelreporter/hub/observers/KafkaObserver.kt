package com.example.postzegelreporter.hub.observers

import com.example.postzegelreporter.domain.kafka.TOPIC_SERIAL_POSTZEGEL
import com.example.postzegelreporter.domain.messages.PostzegelCodeDTO
import com.example.postzegelreporter.hub.domain.WebSocketSerialEventMessage
import com.example.postzegelreporter.hub.handlers.TrackerWebsocketHandler
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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

    @KafkaListener(
        id = "kafkaObserver",
        topics = [TOPIC_SERIAL_POSTZEGEL],
        containerFactory = "kafkaListenerContainerFactoryNoDeadLetter",
    )
    fun observeSerialMessages(
        @Payload message: String,
    ) {
        val postzegelCodeDTO = objectMapper.readValue(message, PostzegelCodeDTO::class.java)

        trackerWebsocketHandler.sendAll(WebSocketSerialEventMessage(code = postzegelCodeDTO.code))
    }
}
