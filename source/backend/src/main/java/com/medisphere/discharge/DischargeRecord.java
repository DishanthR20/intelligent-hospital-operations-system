package com.medisphere.discharge;

import com.medisphere.common.entity.BaseEntity;
import com.medisphere.patient.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Discharge Planner checklist (spec section 29). Every step must be true before
 * {@link #isComplete()} allows the discharge to close, so a patient can never be
 * accidentally discharged with an unpaid bill or a pending lab report.
 */
@Entity
@Table(name = "discharge_records")
public class DischargeRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private boolean doctorApproved = false;
    @Column(nullable = false)
    private boolean labReportsReviewed = false;
    @Column(nullable = false)
    private boolean prescriptionIssued = false;
    @Column(nullable = false)
    private boolean billingCleared = false;
    @Column(nullable = false)
    private boolean pharmacyCleared = false;
    @Column(nullable = false)
    private boolean followUpScheduled = false;
    @Column(nullable = false)
    private boolean instructionsGiven = false;

    private String patientInstructions;
    private Instant completedAt;

    protected DischargeRecord() {
    }

    public DischargeRecord(Patient patient) {
        this.patient = patient;
    }

    public boolean isComplete() {
        return doctorApproved && labReportsReviewed && prescriptionIssued && billingCleared
                && pharmacyCleared && followUpScheduled && instructionsGiven;
    }

    public void complete() {
        this.completedAt = Instant.now();
    }

    public Patient getPatient() {
        return patient;
    }

    public boolean isDoctorApproved() {
        return doctorApproved;
    }

    public void setDoctorApproved(boolean v) {
        doctorApproved = v;
    }

    public boolean isLabReportsReviewed() {
        return labReportsReviewed;
    }

    public void setLabReportsReviewed(boolean v) {
        labReportsReviewed = v;
    }

    public boolean isPrescriptionIssued() {
        return prescriptionIssued;
    }

    public void setPrescriptionIssued(boolean v) {
        prescriptionIssued = v;
    }

    public boolean isBillingCleared() {
        return billingCleared;
    }

    public void setBillingCleared(boolean v) {
        billingCleared = v;
    }

    public boolean isPharmacyCleared() {
        return pharmacyCleared;
    }

    public void setPharmacyCleared(boolean v) {
        pharmacyCleared = v;
    }

    public boolean isFollowUpScheduled() {
        return followUpScheduled;
    }

    public void setFollowUpScheduled(boolean v) {
        followUpScheduled = v;
    }

    public boolean isInstructionsGiven() {
        return instructionsGiven;
    }

    public void setInstructionsGiven(boolean v) {
        instructionsGiven = v;
    }

    public String getPatientInstructions() {
        return patientInstructions;
    }

    public void setPatientInstructions(String patientInstructions) {
        this.patientInstructions = patientInstructions;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
