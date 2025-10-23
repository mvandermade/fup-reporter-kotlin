package com.example.stamp.reporter.domain.requests

import java.time.ZonedDateTime

data class StampCodeRequest(
    val readAt: ZonedDateTime,
    val code: String,
)
