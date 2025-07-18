package com.example.stamp.reporter.testutils

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.kafka.KafkaContainer

fun startPostgresContainer(): PostgreSQLContainer<*> =
    PostgreSQLContainer<Nothing>("postgres:17")
        .apply {
            this.waitingFor(Wait.defaultWaitStrategy())
            this.start()
        }

fun buildKafkaContainer(): KafkaContainer =
    KafkaContainer("apache/kafka-native:3.8.0")
        .apply { this.start() }
