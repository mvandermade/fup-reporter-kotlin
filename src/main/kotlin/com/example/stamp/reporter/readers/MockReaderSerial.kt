package com.example.stamp.reporter.readers

import com.example.stamp.reporter.domain.kafka.KafkaSender
import com.example.stamp.reporter.domain.kafka.TOPIC_SERIAL_STAMP
import com.example.stamp.reporter.domain.messages.StampCodeDTO
import com.example.stamp.reporter.providers.RandomProvider
import com.example.stamp.reporter.providers.TimeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MockReaderSerial(
    private val randomProvider: RandomProvider,
    private val timeProvider: TimeProvider,
    private val kafkaSender: KafkaSender,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

//    @Scheduled(fixedDelay = 10_000)
    fun scanPostZegel() {
        val input = randomProvider.randomString(1)
        val zdt = timeProvider.zonedDateTimeNowSystem()
        logger.info("Read Serial Event: $input @ $zdt")
        kafkaSender.sendMessage(
            TOPIC_SERIAL_STAMP,
            StampCodeDTO(
                readAt = zdt,
                code = input,
                idempotencyKey = randomProvider.randomUUID().toString(),
                kafkaKey = input.getOrNull(0).toString(),
            ),
        )
        logger.info("Sent to kafka Serial Event: $input @ $zdt")
    }
}
