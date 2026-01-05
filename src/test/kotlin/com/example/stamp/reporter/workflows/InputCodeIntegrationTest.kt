package com.example.stamp.reporter.workflows

import com.example.stamp.reporter.apicallers.feign.StampServerApi
import com.example.stamp.reporter.domain.messages.ReadStampCode
import com.example.stamp.reporter.domain.requests.StampCodeRequest
import com.example.stamp.reporter.providers.RandomProvider
import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.testutils.startPostgresContainer
import com.example.stamp.reporter.workflows.brokers.SendToExchangeBroker
import com.example.stamp.reporter.workflows.repositories.WorkflowErrorRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowTombstoneRepository
import com.example.stamp.reporter.workflows.workers.WorkerStarter
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
    @param:Autowired private val workflowTombstoneRepository: WorkflowTombstoneRepository,
    @param:Autowired private val workflowErrorRepository: WorkflowErrorRepository,
) {
    @MockkBean
    private lateinit var stampServerApi: StampServerApi

    @BeforeEach
    fun setUp() {
        workflowTombstoneRepository.deleteAll()
        workflowErrorRepository.deleteAll()
    }

    @Test
    fun `Stamp code is read and published to exchange`() {
        val slot = slot<StampCodeRequest>()
        every { stampServerApi.postStampCode(capture(slot), any()) } returns Unit

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

    @Test
    fun `Stamp code is read and published to exchange and tombstoned`() {
        val slot = slot<StampCodeRequest>()
        every { stampServerApi.postStampCode(capture(slot), any()) } returns Unit

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
        assertThat(workflowTombstoneRepository.findAll()).isEmpty()
        workerStarter.incrementWork()

        await().atMost(Duration.ofSeconds(5)).untilAsserted {
            assertThat(workflowTombstoneRepository.findAll()).isNotEmpty
        }
    }

    @Test
    fun `Stamp code is read but publish fails expect it to be in error`() {
        val slot = slot<StampCodeRequest>()
        every {
            stampServerApi.postStampCode(capture(slot), any())
        } throws RuntimeException("Simulated publish failure")

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
        assertThat(workflowErrorRepository.findAll()).isEmpty()
        workerStarter.incrementWork()

        await().atMost(Duration.ofSeconds(5)).untilAsserted {
            assertThat(workflowErrorRepository.findAll()).isNotEmpty
        }
    }

    companion object {
        @Container
        @ServiceConnection
        @Suppress("unused")
        val postgresContainer = startPostgresContainer()
    }
}
