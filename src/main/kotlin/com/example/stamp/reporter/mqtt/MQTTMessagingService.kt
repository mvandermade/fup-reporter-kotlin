package com.example.stamp.reporter.mqtt

import com.example.stamp.reporter.websockets.domain.WebSocketClientMessage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class MQTTMessagingService(
    @param:Qualifier("messagingGatewayMQTTOutbound") private val messagingGateway: MessagingGatewayMQTTOutbound,
) {
    private val objectMapper = jacksonObjectMapper()

    fun sendToMqtt(data: WebSocketClientMessage) {
        messagingGateway.sendString(jacksonObjectMapper().writeValueAsString(data))
    }
}
