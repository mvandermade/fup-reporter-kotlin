package com.example.stamp.reporter.workflows.mappers

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.entities.Workflow
import com.example.stamp.reporter.workflows.entities.WorkflowError
import com.example.stamp.reporter.workflows.entities.WorkflowTombstone
import org.springframework.stereotype.Component

@Component
class WorkflowMapper(
    private val timeProvider: TimeProvider,
) {
    fun toWorkflowError(workflow: Workflow): WorkflowError =
        WorkflowError(
            type = workflow.type,
            programCounter = workflow.programCounter,
            markedAsErrorAt = timeProvider.offsetDateTimeNowSystem(),
        )

    fun toWorkflowTombstone(workflow: Workflow): WorkflowTombstone =
        WorkflowTombstone(
            type = workflow.type,
            programCounter = workflow.programCounter,
            markedAsTombstoneAt = timeProvider.offsetDateTimeNowSystem(),
        )
}
