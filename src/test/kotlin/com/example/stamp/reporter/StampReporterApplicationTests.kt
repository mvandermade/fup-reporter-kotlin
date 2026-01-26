package com.example.stamp.reporter

import com.example.stamp.reporter.testutils.addMqttToRegistry
import com.example.stamp.reporter.testutils.startMqttContainer
import com.example.stamp.reporter.testutils.startPostgresContainer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
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
        private val mqttContainer = startMqttContainer()

        @JvmStatic
        @DynamicPropertySource
        @Suppress("unused")
        fun properties(registry: DynamicPropertyRegistry) {
            addMqttToRegistry(registry, mqttContainer)
        }

        // For closing down the application more quickly
        @JvmStatic
        @AfterAll
        fun tearDown(
            @Autowired mqttChannelAdapter: MqttPahoMessageDrivenChannelAdapter,
        ) {
            if (mqttChannelAdapter.isRunning) mqttChannelAdapter.stop()
        }
    }
}
