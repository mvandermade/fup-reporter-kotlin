package com.example.postzegelreporter.readers

import com.example.postzegelreporter.domain.PostzegelCode
import com.example.postzegelreporter.providers.RandomProvider
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MockReader(
    private val randomProvider: RandomProvider,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 10_000)
    @Transactional
    fun scanPostZegel() {
        // Just a simple scanner
        val input = randomProvider.randomString(1)
        logger.info("Read $input")

        // Push an event using Spring Modulith
        applicationEventPublisher.publishEvent(
            PostzegelCode(code = input),
        )
    }
}
