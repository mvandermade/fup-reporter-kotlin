package com.example.postzegelreporter.domain

import org.springframework.modulith.events.Externalized
import java.time.Instant

@Externalized
data class PostzegelCodeRequest(
    val readAt: Instant,
    val code: String,
)
