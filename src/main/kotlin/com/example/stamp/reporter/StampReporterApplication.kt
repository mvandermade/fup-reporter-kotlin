package com.example.stamp.reporter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.integration.annotation.IntegrationComponentScan
import org.springframework.integration.config.EnableIntegration
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan
@EnableScheduling
@EnableFeignClients
class StampReporterApplication

fun main(args: Array<String>) {
    runApplication<StampReporterApplication>(*args)
}
