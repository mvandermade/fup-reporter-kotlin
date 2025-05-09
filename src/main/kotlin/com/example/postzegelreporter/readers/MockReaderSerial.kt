package com.example.postzegelreporter.readers

import com.example.postzegelreporter.domain.kafka.KafkaSender
import com.example.postzegelreporter.domain.kafka.TOPIC_SERIAL_POSTZEGEL
import com.example.postzegelreporter.domain.messages.PostzegelCodeDTO
import com.example.postzegelreporter.providers.RandomProvider
import com.example.postzegelreporter.providers.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class MockReaderSerial(
    private val randomProvider: RandomProvider,
    private val timeProvider: TimeProvider,
    private val kafkaSender: KafkaSender,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 10_000)
    fun scanPostZegel() {
        val input = randomProvider.randomString(1)
        val instant = timeProvider.instantNow()
        logger.info("Read Serial Event: $input @ $instant")
        // Push an event using Spring Modulith
        kafkaSender.sendMessage(
            TOPIC_SERIAL_POSTZEGEL,
            PostzegelCodeDTO(
                readAt = instant,
                code = input,
                idempotencyKey = randomProvider.randomUUID().toString(),
                kafkaKey = randomProvider.randomUUID().toString(),
            ),
        )
        logger.info("Sent to kafka Serial Event: $input @ $instant")
    }
}
