package com.example.stamp.reporter.workflows.workers

import java.util.concurrent.atomic.AtomicBoolean

data class WorkerContext(
    val isShuttingDown: AtomicBoolean,
    val worker: WorkerDaemon,
)
