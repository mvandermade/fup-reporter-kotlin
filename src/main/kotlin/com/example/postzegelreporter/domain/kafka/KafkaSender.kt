package com.example.postzegelreporter.domain.kafka

import com.example.postzegelreporter.domain.messages.KafkaMessage
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSender(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper =
        with(jacksonObjectMapper()) {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

    fun sendMessage(
        topic: String,
        kafkaMessage: KafkaMessage,
    ) {
        val message = objectMapper.writeValueAsString(kafkaMessage)
        logger.debug("Sending message to topic {}", topic)
        kafkaTemplate.send(topic, kafkaMessage.kafkaKey, message)
    }
}
