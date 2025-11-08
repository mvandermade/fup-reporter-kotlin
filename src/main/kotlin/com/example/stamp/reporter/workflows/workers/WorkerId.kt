package com.example.stamp.reporter.workflows.workers

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.entities.Worker
import com.example.stamp.reporter.workflows.repositories.WorkerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.properties.Delegates
import kotlin.system.exitProcess

@Component
class WorkerId(
    workerRepository: WorkerRepository,
    timeProvider: TimeProvider,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    var workerId by Delegates.notNull<Long>()

    init {
        workerId =
            try {
                workerRepository
                    .save(
                        Worker(
                            hostname = "localhost",
                            expireHeartBeatAt = timeProvider.offsetDateTimeNowSystem(),
                        ),
                    ).id
            } catch (e: Exception) {
                logger.error("Failed to save worker: ${e.message}")
                exitProcess(-1)
            }
    }
}
