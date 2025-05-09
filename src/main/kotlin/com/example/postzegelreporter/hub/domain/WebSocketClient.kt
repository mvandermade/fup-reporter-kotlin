package com.example.postzegelreporter.hub.domain

import org.springframework.web.socket.WebSocketSession

data class WebSocketClient(
    val session: WebSocketSession,
)
