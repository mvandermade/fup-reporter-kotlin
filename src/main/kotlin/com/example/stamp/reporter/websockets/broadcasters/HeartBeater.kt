package com.example.stamp.reporter.websockets.broadcasters

import com.example.stamp.reporter.providers.RandomProvider
import com.example.stamp.reporter.websockets.domain.WebSocketHeartbeatMessage
import com.example.stamp.reporter.websockets.handlers.TrackerWebsocketHandler
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class HeartBeater(
    private val trackerWebsocketHandler: TrackerWebsocketHandler,
    private val randomProvider: RandomProvider,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 10_000)
    fun heartBeat() {
        logger.info("HeartBeater started")
        trackerWebsocketHandler.sendAll(
            WebSocketHeartbeatMessage(datetime = randomProvider.randomUUID().toString()),
        )
    }
}
