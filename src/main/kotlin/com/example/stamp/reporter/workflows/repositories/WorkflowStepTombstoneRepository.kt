package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.WorkflowStepTombstone
import org.springframework.data.jpa.repository.JpaRepository

interface WorkflowStepTombstoneRepository : JpaRepository<WorkflowStepTombstone, Long>
