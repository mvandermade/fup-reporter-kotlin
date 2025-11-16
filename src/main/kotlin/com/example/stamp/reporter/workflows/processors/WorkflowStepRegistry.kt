package com.example.stamp.reporter.workflows.processors

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.domain.WorkflowResult
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkflowStepRegistry(
    private val workflowStepRepository: WorkflowStepRepository,
    private val timeProvider: TimeProvider,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    fun save(
        workflowStepId: Long,
        workflowResult: WorkflowResult.Success,
    ) {
        val workflowStep =
            workflowStepRepository.findByIdOrNull(workflowStepId)
                ?: throw Exception("Workflow step with id $workflowStepId not found")
        workflowStep.errorMessage = null
        workflowStep.output = workflowResult.output
        workflowStep.outputAt = timeProvider.offsetDateTimeNowSystem()

        workflowStepRepository.save(workflowStep)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun save(
        workflowStepId: Long,
        workflowResult: WorkflowResult.Error,
    ) {
        val workflow =
            workflowStepRepository.findByIdOrNull(workflowStepId)
                ?: throw Exception("Workflow step with id $workflowStepId not found")
        workflow.errorMessage = workflowResult.message
        workflow.errorAt = timeProvider.offsetDateTimeNowSystem()

        workflowStepRepository.save(workflow)
    }
}
