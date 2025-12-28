package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.Workflow
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkflowRepository : JpaRepository<Workflow, Long> {
    fun findFirstByWorkerIsNullOrderByIdAsc(): Workflow?

    fun findAllByWorkerIdIs(workerId: Long): List<Workflow>
}
