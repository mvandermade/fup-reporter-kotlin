package com.example.postzegelreporter.providers

import org.springframework.stereotype.Component
import java.time.Instant

@Component
class TimeProvider {
    fun instantNow() = Instant.now()
}
