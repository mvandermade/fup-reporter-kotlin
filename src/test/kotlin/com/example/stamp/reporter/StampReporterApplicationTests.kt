package com.example.stamp.reporter

import com.example.stamp.reporter.testutils.buildKafkaContainer
import com.example.stamp.reporter.testutils.startPostgresContainer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import java.util.function.Supplier

@SpringBootTest
class StampReporterApplicationTests {
    @Test
    fun contextLoads() {
    }

    companion object {
        @Container
        @ServiceConnection
        @Suppress("unused")
        val postgresContainer = startPostgresContainer()

        @Container
        val kafkaContainer = buildKafkaContainer()

        @JvmStatic
        @DynamicPropertySource
        fun props(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", { kafkaContainer.bootstrapServers })
            registry.add("spring.kafka.producer.bootstrap-servers", Supplier { kafkaContainer.bootstrapServers })
            registry.add("application.kafka.deadletter-producer.bootstrap-servers", Supplier { kafkaContainer.bootstrapServers })
            registry.add("application.kafka.consumer.bootstrap-servers", Supplier { kafkaContainer.bootstrapServers })
        }
    }
}
