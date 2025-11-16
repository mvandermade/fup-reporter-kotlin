package com.example.stamp.reporter.workflows.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "workers")
class Worker(
    var hostname: String,
    @Column(name = "expire_heartbeat_at")
    var expireHeartBeatAt: OffsetDateTime,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0L
}
