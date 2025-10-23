package com.example.stamp.reporter.providers

import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
class TimeProvider {
    fun zonedDateTimeNowSystem(): ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

    fun offsetDateTimeNowSystem(): OffsetDateTime = OffsetDateTime.now(ZoneId.systemDefault())
}
