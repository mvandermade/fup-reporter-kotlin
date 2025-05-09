package com.example.postzegelreporter.domain.messages

interface KafkaMessage {
    val kafkaKey: String
}
