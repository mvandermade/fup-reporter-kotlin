package com.example.stamp.reporter.mqtt

import org.springframework.integration.annotation.MessagingGateway

@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
interface MessagingGatewayMQTTOutbound {
    fun sendString(data: String)
}

// TODO add more 1 for each type of message
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
interface MessagingGatewayMQTTOutboundSerialEvent {
    fun sendString(data: String)
}
