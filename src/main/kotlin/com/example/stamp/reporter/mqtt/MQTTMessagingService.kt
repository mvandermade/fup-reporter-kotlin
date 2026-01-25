package com.example.stamp.reporter.mqtt

import com.example.stamp.reporter.websockets.domain.WebSocketClientMessage
import com.example.stamp.reporter.websockets.domain.WebsocketEventType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class MQTTMessagingService(
    @param:Qualifier("messagingToSerialEvent") private val messagingGateway: MessagingToSerialEvent,
) {
    private val objectMapper = jacksonObjectMapper()

    fun sendToMqtt(data: WebSocketClientMessage) {
        val serializedData = objectMapper.writeValueAsString(data)
        when (data.type) {
            WebsocketEventType.SERIAL_EVENT -> {
                messagingGateway.sendString(serializedData)
            }
            WebsocketEventType.SYNCHRONIZATION -> {
                messagingGateway.sendString(serializedData)
            }
            WebsocketEventType.HEARTBEAT -> {
                messagingGateway.sendString(serializedData)
            }
            WebsocketEventType.POST_EXCHANGE -> {
                messagingGateway.sendString(serializedData)
            }
            WebsocketEventType.ACK_EXCHANGE -> {
                messagingGateway.sendString(serializedData)
            }
        }
    }
}
