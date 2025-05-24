package com.example.postzegelreporter.domain

import java.time.ZonedDateTime

data class PostzegelCodeRequest(
    val readAt: ZonedDateTime,
    val code: String,
)
