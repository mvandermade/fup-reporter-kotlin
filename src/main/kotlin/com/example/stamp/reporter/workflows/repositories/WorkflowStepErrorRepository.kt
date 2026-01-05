package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.WorkflowStepError
import org.springframework.data.jpa.repository.JpaRepository

interface WorkflowStepErrorRepository : JpaRepository<WorkflowStepError, Long>
