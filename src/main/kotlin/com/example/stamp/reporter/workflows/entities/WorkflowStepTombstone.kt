package com.example.stamp.reporter.workflows.entities

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime

@Entity
@Table(
    name = "workflow_step_tombstones",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["workflow", "step_number"]),
    ],
)
class WorkflowStepTombstone(
    @ManyToOne(cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "workflow_tombstone_id")
    var workflowTombstone: WorkflowTombstone,
    var input: String?,
    @Column(name = "step_number")
    var stepNumber: Int,
    @Column(name = "started_at")
    var startedAt: OffsetDateTime? = null,
    @Column(name = "callback")
    @Enumerated(EnumType.STRING)
    var callback: StepCallbackType,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0L

    var output: String? = null

    @Column(name = "output_at")
    var outputAt: OffsetDateTime? = null

    @Column(name = "error_message")
    var errorMessage: String? = null

    @Column(name = "error_at")
    var errorAt: OffsetDateTime? = null

    // TODO Jpa buddy
}
