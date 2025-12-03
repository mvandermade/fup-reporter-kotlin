package com.example.stamp.reporter.workflows.mappers

import com.example.stamp.reporter.workflows.entities.WorkflowError
import com.example.stamp.reporter.workflows.entities.WorkflowStep
import com.example.stamp.reporter.workflows.entities.WorkflowStepError
import com.example.stamp.reporter.workflows.entities.WorkflowStepTombstone
import com.example.stamp.reporter.workflows.entities.WorkflowTombstone
import org.springframework.stereotype.Component

@Component
class WorkflowStepMapper {
    fun toError(
        workflowStep: WorkflowStep,
        workflowError: WorkflowError,
    ): WorkflowStepError =
        WorkflowStepError(
            workflowError = workflowError,
            input = workflowStep.input,
            stepNumber = workflowStep.stepNumber,
            startedAt = workflowStep.startedAt,
            callback = workflowStep.callback,
        )

    fun toTombstone(
        workflowStep: WorkflowStep,
        workflowTombstone: WorkflowTombstone,
    ): WorkflowStepTombstone =
        WorkflowStepTombstone(
            workflowTombstone = workflowTombstone,
            input = workflowStep.input,
            stepNumber = workflowStep.stepNumber,
            startedAt = workflowStep.startedAt,
            callback = workflowStep.callback,
        )
}
