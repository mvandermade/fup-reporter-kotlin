package com.example.stamp.reporter.workflows.brokers

import com.example.stamp.reporter.providers.TimeProvider
import com.example.stamp.reporter.workflows.domain.SendToExchangeInput1
import com.example.stamp.reporter.workflows.entities.CallbackType
import com.example.stamp.reporter.workflows.entities.Workflow
import com.example.stamp.reporter.workflows.entities.WorkflowStep
import com.example.stamp.reporter.workflows.model.WorkflowType
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
    fun save(input: SendToExchangeInput1) {
        val workflow = workFlowRepository.save(Workflow(WorkflowType.SEND_TO_EXCHANGE))
        workflowStepRepository.save(
            WorkflowStep(
                workflow = workflow,
                input = objectMapper.writeValueAsString(input),
                stepNumber = 1,
                startedAt = timeProvider.offsetDateTimeNowSystem(),
                callback = CallbackType.TOMBSTONE,
            ),
        )
    }
}
