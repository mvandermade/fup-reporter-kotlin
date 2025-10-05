package com.example.stamp.reporter.providers

import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Component
class TimeProvider {
    fun zonedDateTimeNowSystem(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.systemDefault())

    fun offsetDateTimeNowSystem(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.systemDefault())
}
