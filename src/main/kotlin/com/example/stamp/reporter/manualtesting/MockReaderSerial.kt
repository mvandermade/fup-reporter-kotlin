package com.example.stamp.reporter.manualtesting

import com.example.stamp.reporter.domain.messages.ReadStampCode
import com.example.stamp.reporter.providers.RandomProvider
import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.brokers.SendToExchangeBroker
import com.example.stamp.reporter.workflows.workers.WorkerStarter
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("!test")
class MockReaderSerial(
    private val randomProvider: RandomProvider,
    private val timeProvider: TimeProvider,
    private val sendToExchangeBroker: SendToExchangeBroker,
    private val workerStarter: WorkerStarter,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1)
    fun scanPostZegel() {
        val input = randomProvider.randomString(1)
        val zdt = timeProvider.zonedDateTimeNowSystem()
        logger.trace("Mock Serial Event: {} @ {}", input, zdt)
        sendToExchangeBroker.save(
            ReadStampCode(
                readAt = zdt,
                code = input,
                idempotencyKey = randomProvider.randomUUID().toString(),
            ),
        )
        workerStarter.incrementWork()
    }
}
