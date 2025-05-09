package com.example.postzegelreporter.hub.domain

interface WebSocketClientMessage {
    val type: WebsocketEventType
}

enum class WebsocketEventType {
    SERIAL_EVENT,
    SYNCHRONIZATION,
    HEARTBEAT,
}

data class WebSocketSerialEventMessage(
    val code: String,
) : WebSocketClientMessage {
    override val type: WebsocketEventType = WebsocketEventType.SERIAL_EVENT
}

data class WebSocketSynchronizationMessage(
    val tag: String,
) : WebSocketClientMessage {
    override val type = WebsocketEventType.SYNCHRONIZATION
}

data class WebSocketHeartbeatMessage(
    val datetime: String,
) : WebSocketClientMessage {
    override val type = WebsocketEventType.HEARTBEAT
}
