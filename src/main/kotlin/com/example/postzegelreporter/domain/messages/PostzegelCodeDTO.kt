package com.example.postzegelreporter.domain.messages

import java.time.ZonedDateTime

data class PostzegelCodeDTO(
    val readAt: ZonedDateTime,
    val code: String,
    val idempotencyKey: String,
    override val kafkaKey: String,
) : KafkaMessage
