package com.example.stamp.reporter.workflows.domain

import java.time.ZonedDateTime

data class SendToExchangeInput1(
    val readAt: ZonedDateTime,
    val code: String,
    val idempotencyKey: String,
)
