package com.example.postzegelreporter.domain.messages

import java.time.Instant

data class PostzegelCodeDTO(
    val readAt: Instant,
    val code: String,
    val idempotencyKey: String,
    override val kafkaKey: String,
) : KafkaMessage
