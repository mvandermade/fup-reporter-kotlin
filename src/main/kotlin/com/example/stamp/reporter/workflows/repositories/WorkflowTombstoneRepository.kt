package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.WorkflowTombstone
import org.springframework.data.jpa.repository.JpaRepository

interface WorkflowTombstoneRepository : JpaRepository<WorkflowTombstone, Long>
