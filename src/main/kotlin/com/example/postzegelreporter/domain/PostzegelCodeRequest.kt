package com.example.postzegelreporter.domain

import java.time.Instant

data class PostzegelCodeRequest(
    val readAt: Instant,
    val code: String,
)
