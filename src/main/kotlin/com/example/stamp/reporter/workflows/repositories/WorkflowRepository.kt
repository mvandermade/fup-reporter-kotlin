package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.Workflow
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface WorkflowRepository : JpaRepository<Workflow, Long> {
    fun findFirstByWorkerIsNull(): Workflow?

    // Atomically assign the given workerId to one workflow that currently has worker_id IS NULL.
    // Uses SKIP LOCKED to avoid blocking and NOT EXISTS to prevent unique constraint violation.
    @Modifying
    @Transactional
    @Query(
        value = """
            with next as (
                select id
                from workflows
                where worker_id is null
                order by id
                for update skip locked
                limit 1
            )
            update workflows w
            set worker_id = :workerId
            from next
            where w.id = next.id
              and not exists (
                select 1 from workflows x where x.worker_id = :workerId
              )
        """,
        nativeQuery = true,
    )
    fun assignNextAvailable(workerId: Long): Int
}
