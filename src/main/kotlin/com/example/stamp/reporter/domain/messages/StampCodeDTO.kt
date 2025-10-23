package com.example.stamp.reporter.domain.messages

import java.time.ZonedDateTime

data class StampCodeDTO(
    val readAt: ZonedDateTime,
    val code: String,
    val idempotencyKey: String,
)
