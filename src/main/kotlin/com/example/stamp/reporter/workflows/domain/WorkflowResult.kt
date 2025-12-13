package com.example.stamp.reporter.workflows.domain

sealed class WorkflowResult {
    data class Success(
        val output: String?,
    ) : WorkflowResult()

    data class Error(
        val message: String,
    ) : WorkflowResult()
}
