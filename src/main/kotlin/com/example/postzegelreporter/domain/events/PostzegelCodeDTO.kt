package com.example.postzegelreporter.domain.events

import org.springframework.modulith.events.Externalized
import java.time.Instant

@Externalized
data class PostzegelCodeDTO(
    val readAt: Instant,
    val code: String,
    val idempotencyKey: String,
)
