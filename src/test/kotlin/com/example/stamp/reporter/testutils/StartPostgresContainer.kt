package com.example.stamp.reporter.testutils

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

fun startPostgresContainer(): PostgreSQLContainer<*> =
    PostgreSQLContainer<Nothing>("postgres:17")
        .apply {
            this.waitingFor(Wait.defaultWaitStrategy())
            this.start()
        }
