package com.example.postzegelreporter.eventmanagement

import org.springframework.modulith.events.IncompleteEventPublications
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class IncompleteEventService(private val incompleteEventPublications: IncompleteEventPublications) {
    @Scheduled(fixedDelay = 1_000)
    fun check() {
        println("Checking incomplete event (notice the ordering of events is not the same as they went in)")
        incompleteEventPublications.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(1))
    }
}
