package com.example.stamp.reporter.mqtt

import org.springframework.integration.annotation.MessagingGateway

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
interface MessagingGateway {
    fun sendToMqtt(data: String?)
}
