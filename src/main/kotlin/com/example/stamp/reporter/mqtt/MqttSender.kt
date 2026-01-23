package com.example.stamp.reporter.mqtt

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class MqttSender(
    private val myGateway: MessagingGateway,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // This does not seem to work.. Also not postconstruct
    @Scheduled(fixedDelay = 10_000)
    fun sendToMqtt() {
        logger.info("Sending 'foo' to MQTT")
        try {
            myGateway.sendToMqtt("foo")
            logger.info("Successfully sent 'foo' to MQTT")
        } catch (e: Exception) {
            logger.error("Failed to send to MQTT", e)
        }
    }
}
