package com.example.stamp.reporter.websockets.handlers

import com.example.stamp.reporter.websockets.domain.WebSocketClient
import com.example.stamp.reporter.websockets.domain.WebSocketClientMessage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.CopyOnWriteArrayList

@Component
class TrackerWebsocketHandler : TextWebSocketHandler() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val webSockerClientList: MutableList<WebSocketClient> = CopyOnWriteArrayList()

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        webSockerClientList.removeIf({ webSocketClient -> webSocketClient.session == session })
    }

    private val objectMapper = jacksonObjectMapper()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val concurrentSession = ConcurrentWebSocketSessionDecorator(session, 2000, 4096)
        webSockerClientList += WebSocketClient(session = concurrentSession)
    }

    private fun sendEvent(
        webSocketClient: WebSocketClient,
        message: WebSocketClientMessage,
    ) {
        try {
            val text = objectMapper.writeValueAsString(message)
            webSocketClient.session.sendMessage(TextMessage(text))
        } catch (e: Exception) {
            // you can catch more specific exception here and handle it in a different ways, e.g.: when the session is closed unexpectedly
            webSockerClientList.remove(webSocketClient)
        }
    }

    fun sendAll(message: WebSocketClientMessage) {
        logger.info("Sending event: $message to ${webSockerClientList.size} clients")
        webSockerClientList.forEach {
            sendEvent(it, message)
        }
    }
}
