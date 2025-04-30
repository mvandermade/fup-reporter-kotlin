package com.example.postzegelreporter.readers

import com.example.postzegelreporter.domain.events.PostzegelCodeDTO
import com.example.postzegelreporter.providers.RandomProvider
import com.example.postzegelreporter.providers.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MockReaderSerial(
    private val randomProvider: RandomProvider,
    private val timeProvider: TimeProvider,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1_000)
    @Transactional
    fun scanPostZegel() {
        val input = randomProvider.randomString(1)
        val instant = timeProvider.instantNow()
        logger.info("Read Serial Event: $input @ $instant")
        // Push an event using Spring Modulith
        applicationEventPublisher.publishEvent(
            PostzegelCodeDTO(
                readAt = instant,
                code = input,
                idempotencyKey = randomProvider.randomUUID().toString(),
            ),
        )
    }
}
