package com.example.stamp.reporter.workflows.brokers

import com.example.stamp.reporter.domain.messages.ReadStampCode
import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.entities.StepCallbackType
import com.example.stamp.reporter.workflows.entities.Workflow
import com.example.stamp.reporter.workflows.entities.WorkflowStep
import com.example.stamp.reporter.workflows.model.WorkflowType
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SendToExchangeBroker(
    private val workFlowRepository: WorkflowRepository,
    private val workflowStepRepository: WorkflowStepRepository,
    private val timeProvider: TimeProvider,
) {
    private val objectMapper =
        with(jacksonObjectMapper()) {
            this.registerModule(JavaTimeModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

    @Transactional(rollbackFor = [Exception::class])
    fun save(input: ReadStampCode) {
        val programCounter = 1
        val workflow =
            workFlowRepository.save(
                Workflow(WorkflowType.SEND_TO_EXCHANGE, programCounter = programCounter),
            )
        workflowStepRepository.save(
            WorkflowStep(
                workflow = workflow,
                input = objectMapper.writeValueAsString(input),
                stepNumber = programCounter,
                startedAt = timeProvider.offsetDateTimeNowSystem(),
                callback = StepCallbackType.TAKE_NEXT,
            ),
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun nextStepNumberIs(
        workflow: Workflow,
        workflowStepId: Long,
    ): Int {
        val workflowStep =
            workflowStepRepository.findByIdOrNull(workflowStepId)
                ?: throw Exception("Workflow step with id $workflowStepId not found")

        when (workflow.programCounter) {
            1 -> {
                workflowStepRepository.save(
                    WorkflowStep(
                        workflow = workflow,
                        input = workflowStep.output,
                        stepNumber = workflow.programCounter + 1,
                        startedAt = workflowStep.startedAt,
                        callback = StepCallbackType.TAKE_NEXT,
                    ),
                )
                return workflow.programCounter + 1
            }
            2 -> {
                workflowStepRepository.save(
                    WorkflowStep(
                        workflow = workflow,
                        input = workflowStep.output,
                        stepNumber = workflow.programCounter + 1,
                        startedAt = workflowStep.startedAt,
                        callback = StepCallbackType.TOMBSTONE,
                    ),
                )
                return workflow.programCounter + 1
            }
            else -> throw Exception("Invalid program counter: ${workflow.programCounter}")
        }
    }
}
