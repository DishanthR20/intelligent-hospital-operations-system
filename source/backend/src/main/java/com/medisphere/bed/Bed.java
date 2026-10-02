package com.medisphere.bed;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "beds")
public class Bed extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @Column(nullable = false, unique = true)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedStatus status = BedStatus.AVAILABLE;

    private java.util.UUID currentPatientId;

    /** Bed turnaround tracking (spec section 25). */
    private Instant lastDischargedAt;
    private Instant cleaningStartedAt;
    private Instant cleaningCompletedAt;

    protected Bed() {
    }

    public Bed(Ward ward, String bedNumber) {
        this.ward = ward;
        this.bedNumber = bedNumber;
    }

    public Ward getWard() {
        return ward;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public BedStatus getStatus() {
        return status;
    }

    public java.util.UUID getCurrentPatientId() {
        return currentPatientId;
    }

    public void occupy(java.util.UUID patientId) {
        this.status = BedStatus.OCCUPIED;
        this.currentPatientId = patientId;
    }

    public void release() {
        this.currentPatientId = null;
        this.status = BedStatus.CLEANING;
        this.lastDischargedAt = Instant.now();
        this.cleaningStartedAt = Instant.now();
    }

    public void finishCleaning() {
        this.cleaningCompletedAt = Instant.now();
        this.status = BedStatus.INSPECTION;
    }

    public void passInspection() {
        this.status = BedStatus.AVAILABLE;
    }

    public void reserve() {
        this.status = BedStatus.RESERVED;
    }

    public Instant getLastDischargedAt() {
        return lastDischargedAt;
    }

    public Instant getCleaningStartedAt() {
        return cleaningStartedAt;
    }

    public Instant getCleaningCompletedAt() {
        return cleaningCompletedAt;
    }

    /** Minutes from discharge to the bed becoming available again, once known. */
    public Long turnaroundMinutes() {
        if (lastDischargedAt == null || status != BedStatus.AVAILABLE) return null;
        return java.time.Duration.between(lastDischargedAt, getUpdatedAt()).toMinutes();
    }
}
