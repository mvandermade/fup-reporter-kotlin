package com.example.postzegelreporter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PostzegelReporterApplication

fun main(args: Array<String>) {
    runApplication<PostzegelReporterApplication>(*args)
}
