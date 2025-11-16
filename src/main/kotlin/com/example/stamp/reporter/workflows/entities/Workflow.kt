package com.example.stamp.reporter.workflows.entities

import com.example.stamp.reporter.workflows.model.WorkflowType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import jakarta.persistence.Version
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime

@Entity
@Table(name = "workflows")
class Workflow(
    @Enumerated(EnumType.STRING)
    var type: WorkflowType,
    @Column(name = "program_counter")
    var programCounter: Int = 0,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0L

    @Version
    var version: Long? = null

    @OneToOne(optional = true)
    @JoinColumn(name = "worker_id", unique = true)
    var worker: Worker? = null

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    var createdAt: OffsetDateTime? = null

    @OneToMany(mappedBy = "workflow", cascade = [CascadeType.REMOVE])
    var steps: MutableList<WorkflowStep> = mutableListOf()
}
