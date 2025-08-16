package com.example.stamp.reporter.websockets.domain

import org.springframework.web.socket.WebSocketSession

data class WebSocketClient(
    val session: WebSocketSession,
)
