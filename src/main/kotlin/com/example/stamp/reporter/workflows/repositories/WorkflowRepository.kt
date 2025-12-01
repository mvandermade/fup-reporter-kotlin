package com.example.stamp.reporter.workflows.repositories

import com.example.stamp.reporter.workflows.entities.Worker
import com.example.stamp.reporter.workflows.entities.Workflow
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface WorkflowRepository : JpaRepository<Workflow, Long> {
    fun findFirstByWorkerIsNull(): Workflow?

    @Modifying
    @Query("update Workflow w set w.worker = :worker where w.id = (select min(w2.id) from Workflow w2 where w2.worker is null)")
    fun assignNextWorkflowToWorker(
        @Param("worker") worker: Worker,
    ): Int

    fun findByWorkerId(workerId: Long): Workflow?

    fun findAllByWorkerIdIs(workerId: Long): List<Workflow>
}
