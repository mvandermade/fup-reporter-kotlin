package com.example.stamp.reporter.workflows.services

import com.example.stamp.reporter.workflows.entities.StepCallbackType
import com.example.stamp.reporter.workflows.entities.Workflow
import com.example.stamp.reporter.workflows.mappers.WorkflowMapper
import com.example.stamp.reporter.workflows.mappers.WorkflowStepMapper
import com.example.stamp.reporter.workflows.repositories.WorkflowErrorRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepErrorRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowStepTombstoneRepository
import com.example.stamp.reporter.workflows.repositories.WorkflowTombstoneRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkflowService(
    private val workflowRepository: WorkflowRepository,
    private val workflowStepRepository: WorkflowStepRepository,
    private val workflowMapper: WorkflowMapper,
    private val workflowErrorRepository: WorkflowErrorRepository,
    private val workflowStepMapper: WorkflowStepMapper,
    private val workflowStepErrorRepository: WorkflowStepErrorRepository,
    private val workflowTombstoneRepository: WorkflowTombstoneRepository,
    private val workflowStepTombstoneRepository: WorkflowStepTombstoneRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional(rollbackFor = [Exception::class])
    fun markSuccess(
        workflowId: Long,
        callbackType: StepCallbackType,
    ) {
        val workflow =
            workflowRepository.findByIdOrNull(workflowId)
                ?: throw IllegalArgumentException("Workflow with ID $workflowId not found markSuccess")

        when (callbackType) {
            StepCallbackType.TAKE_NEXT -> {
                workflow.programCounter += 1
                workflowRepository.save(workflow)
            }
            StepCallbackType.TOMBSTONE -> {
                markTombstone(workflow)
            }
        }
    }

    fun markTombstone(workflow: Workflow) {
        val workflowSteps = workflowStepRepository.findAllByWorkflowId(workflow.id)

        val workflowTombstone = workflowTombstoneRepository.save(workflowMapper.toWorkflowTombstone(workflow))
        val stepsTombstone = workflowSteps.map { workflowStepMapper.toTombstone(it, workflowTombstone) }

        workflowStepTombstoneRepository.saveAll(stepsTombstone)

        // Cascade delete
        workflowRepository.delete(workflow)
    }

    @Transactional(rollbackFor = [Exception::class])
    fun markError(workflowId: Long) {
        val workflow =
            workflowRepository.findByIdOrNull(workflowId)
                ?: throw IllegalArgumentException("Workflow with ID $workflowId not found markError")

        val workflowSteps = workflowStepRepository.findAllByWorkflowId(workflowId)
        val workflowError = workflowErrorRepository.save(workflowMapper.toWorkflowError(workflow))

        val stepsError = workflowSteps.map { workflowStepMapper.toError(it, workflowError) }
        workflowStepErrorRepository.saveAll(stepsError)

        // Cascade delete
        workflowRepository.delete(workflow)
    }
}
