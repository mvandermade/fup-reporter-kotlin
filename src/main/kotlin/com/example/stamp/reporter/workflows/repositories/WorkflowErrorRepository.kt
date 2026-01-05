package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.WorkflowError
import org.springframework.data.jpa.repository.JpaRepository

interface WorkflowErrorRepository : JpaRepository<WorkflowError, Long>
