package com.example.stamp.reporter.domain.messages

import java.time.ZonedDateTime

data class ReadStampCode(
    val readAt: ZonedDateTime,
    val code: String,
    val idempotencyKey: String,
)
