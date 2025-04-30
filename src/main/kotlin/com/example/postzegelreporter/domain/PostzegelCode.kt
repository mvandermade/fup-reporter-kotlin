package com.example.postzegelreporter.domain

import org.springframework.modulith.events.Externalized
import java.time.Instant

@Externalized
data class PostzegelCode(
    val readAt: Instant,
    val code: String,
)
