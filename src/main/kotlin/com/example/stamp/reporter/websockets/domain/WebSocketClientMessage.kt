package com.example.stamp.reporter.websockets.domain

interface WebSocketClientMessage {
    val type: WebsocketEventType
}

enum class WebsocketEventType {
    SERIAL_EVENT,
    SYNCHRONIZATION,
    HEARTBEAT,
    POST_EXCHANGE,
    ACK_EXCHANGE,
}

data class WebSocketSerialEventMessage(
    val code: String,
) : WebSocketClientMessage {
    override val type: WebsocketEventType = WebsocketEventType.SERIAL_EVENT
}

data class WebSocketPostExchangeMessage(
    val code: String,
): WebSocketClientMessage {
    override val type: WebsocketEventType = WebsocketEventType.POST_EXCHANGE
}

data class WebSocketAckExchangeMessage(
    val code: String,
): WebSocketClientMessage {
    override val type: WebsocketEventType = WebsocketEventType.ACK_EXCHANGE
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
