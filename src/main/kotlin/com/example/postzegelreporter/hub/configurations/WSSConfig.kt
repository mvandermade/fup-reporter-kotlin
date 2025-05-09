package com.example.postzegelreporter.hub.configurations

import com.example.postzegelreporter.hub.handlers.TrackerWebsocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WSSConfig(
    private val trackerWebsocketHandler: TrackerWebsocketHandler,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(trackerWebsocketHandler, "/hub/tracker") // .setAllowedOrigins("*")
    }
}
