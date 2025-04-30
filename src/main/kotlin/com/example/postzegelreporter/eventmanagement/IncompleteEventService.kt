package com.example.postzegelreporter.eventmanagement

import org.slf4j.LoggerFactory
import org.springframework.modulith.events.IncompleteEventPublications
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class IncompleteEventService(private val incompleteEventPublications: IncompleteEventPublications) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1_000)
    fun check() {
        logger.info("Checking for resubmissions")
        incompleteEventPublications.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(1))
    }
}
