package com.example.stamp.reporter.workflows.domain

import java.time.ZonedDateTime

data class ReadStampCode(
    val readAt: ZonedDateTime,
    val code: String,
    val idempotencyKey: String,
)
