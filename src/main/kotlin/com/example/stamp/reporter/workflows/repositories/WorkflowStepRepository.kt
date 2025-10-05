package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.WorkflowStep
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkflowStepRepository : JpaRepository<WorkflowStep, Long> {
    fun findFirstByOutputIsNullAndErrorMessageIsNull(): WorkflowStep?

    fun countByErrorMessageIsNotNull(): Long
}
