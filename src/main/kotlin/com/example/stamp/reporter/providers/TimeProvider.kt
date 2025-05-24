package com.example.stamp.reporter.providers

import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Component
class TimeProvider {
    fun zonedDateTimeNowSystem() = ZonedDateTime.now(ZoneOffset.systemDefault())
}
