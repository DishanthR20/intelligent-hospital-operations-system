package com.medisphere.queue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueRepository extends JpaRepository<QueueEntry, UUID> {
    List<QueueEntry> findByDepartment_IdAndStatus(UUID departmentId, QueueStatus status);
    List<QueueEntry> findByStatus(QueueStatus status);
    long countByDepartment_IdAndStatus(UUID departmentId, QueueStatus status);
    long countByDoctor_IdAndStatus(UUID doctorId, QueueStatus status);
    long countByCreatedAtBetween(Instant start, Instant end);
    long countByEmergencyTrueAndCreatedAtBetween(Instant start, Instant end);
}
