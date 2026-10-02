package com.medisphere.queue;

import com.medisphere.common.entity.BaseEntity;
import com.medisphere.department.Department;
import com.medisphere.doctor.Doctor;
import com.medisphere.patient.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One patient's place in a department's live queue. The priority SCORE itself is
 * never stored — it changes every minute purely from waiting-time aging, so it is
 * always computed fresh by {@link QueuePriorityEngine} from these stable inputs.
 */
@Entity
@Table(name = "queue_entries")
public class QueueEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(nullable = false)
    private String tokenNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status = QueueStatus.WAITING;

    @Column(nullable = false)
    private boolean emergency = false;

    /** Clinical urgency 0 (routine) to 10 (life-threatening), set at triage. */
    @Column(nullable = false)
    private int clinicalRisk = 0;

    private Instant consultationStartedAt;
    private Instant completedAt;

    protected QueueEntry() {
    }

    public QueueEntry(Patient patient, Department department, String tokenNumber, boolean emergency, int clinicalRisk) {
        this.patient = patient;
        this.department = department;
        this.tokenNumber = tokenNumber;
        this.emergency = emergency;
        this.clinicalRisk = clinicalRisk;
    }

    public Patient getPatient() {
        return patient;
    }

    public Department getDepartment() {
        return department;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public String getTokenNumber() {
        return tokenNumber;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public void setStatus(QueueStatus status) {
        this.status = status;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public int getClinicalRisk() {
        return clinicalRisk;
    }

    public void setClinicalRisk(int clinicalRisk) {
        this.clinicalRisk = clinicalRisk;
    }

    public Instant getConsultationStartedAt() {
        return consultationStartedAt;
    }

    public void startConsultation() {
        this.consultationStartedAt = Instant.now();
        this.status = QueueStatus.IN_CONSULTATION;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void complete() {
        this.completedAt = Instant.now();
        this.status = QueueStatus.COMPLETED;
    }

    public long waitingMinutes() {
        // ponytail: getCreatedAt() is only null before the entity is first persisted (JPA
        // auditing sets it on insert) — treat that as "just arrived" rather than throwing.
        if (getCreatedAt() == null) return 0;
        Instant end = consultationStartedAt != null ? consultationStartedAt : Instant.now();
        return java.time.Duration.between(getCreatedAt(), end).toMinutes();
    }
}
