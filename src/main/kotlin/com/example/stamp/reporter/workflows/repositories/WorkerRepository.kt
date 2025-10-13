package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.Worker
import org.springframework.data.jpa.repository.JpaRepository

interface WorkerRepository : JpaRepository<Worker, Long>
