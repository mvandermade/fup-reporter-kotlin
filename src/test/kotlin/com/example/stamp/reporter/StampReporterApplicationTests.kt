package com.example.stamp.reporter

import com.example.stamp.reporter.testutils.startPostgresContainer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container

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
    }
}
