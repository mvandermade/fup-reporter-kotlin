package com.example.stamp.reporter.domain.messages

interface KafkaMessage {
    val kafkaKey: String
}
