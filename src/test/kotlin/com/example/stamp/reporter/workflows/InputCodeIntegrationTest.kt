package com.example.stamp.reporter.workflows

import com.example.stamp.reporter.apicallers.feign.StampServerApi
import com.example.stamp.reporter.domain.messages.ReadStampCode
import com.example.stamp.reporter.domain.requests.StampCodeRequest
import com.example.stamp.reporter.providers.RandomProvider
import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.testutils.startPostgresContainer
import com.example.stamp.reporter.workflows.brokers.SendToExchangeBroker
import com.example.stamp.reporter.workflows.workers.WorkerStarter
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import java.time.Duration

@SpringBootTest(
    properties = ["spring.profiles.active=test"],
)
class InputCodeIntegrationTest(
    @param:Autowired private val randomProvider: RandomProvider,
    @param:Autowired private val timeProvider: TimeProvider,
    @param:Autowired private val sendToExchangeBroker: SendToExchangeBroker,
    @param:Autowired private val workerStarter: WorkerStarter,
) {
    @MockkBean
    private lateinit var stampServerApi: StampServerApi

    @Test
    fun `Stamp code is read and published to exchange`() {
        val slot = slot<StampCodeRequest>()
        every { stampServerApi.postStampCode(capture(slot), any()) } just Runs

        val input = randomProvider.randomString(1)
        val zdt = timeProvider.zonedDateTimeNowSystem()
        val idempotencyKey = randomProvider.randomUUID().toString()
        sendToExchangeBroker.save(
            ReadStampCode(
                readAt = zdt,
                code = input,
                idempotencyKey = idempotencyKey,
            ),
        )
        workerStarter.incrementWork()

        await().atMost(Duration.ofSeconds(5)).untilAsserted {
            verify { stampServerApi.postStampCode(any(), idempotencyKey) }
        }

        assertThat(slot.captured.code).isEqualTo(input)
        assertThat(slot.captured.readAt).isEqualTo(zdt)
    }

    companion object {
        @Container
        @ServiceConnection
        @Suppress("unused")
        val postgresContainer = startPostgresContainer()
    }
}
