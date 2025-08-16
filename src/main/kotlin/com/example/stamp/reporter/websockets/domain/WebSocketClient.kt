package com.example.stamp.reporter.websockets.domain

import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator

data class WebSocketClient(
    val session: ConcurrentWebSocketSessionDecorator,
)
