package com.medisphere.laboratory;

import com.medisphere.common.entity.BaseEntity;
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
 * Covers sample collection through verified result. Modeled as one table rather
 * than separate lab_samples/lab_results tables since the relationship is
 * strictly 1:1 and the fields naturally belong to a single lifecycle record.
 */
@Entity
@Table(name = "lab_orders")
public class LabOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_doctor_id", nullable = false)
    private Doctor orderingDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private LabTestCatalog test;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LabOrderStatus status = LabOrderStatus.ORDERED;

    private String sampleId;
    private Instant sampleCollectedAt;

    private Double resultValue;
    private String resultNotes;
    private boolean critical = false;
    private Instant resultEnteredAt;
    private Instant verifiedAt;

    protected LabOrder() {
    }

    public LabOrder(Patient patient, Doctor orderingDoctor, LabTestCatalog test) {
        this.patient = patient;
        this.orderingDoctor = orderingDoctor;
        this.test = test;
    }

    public void collectSample(String sampleId) {
        this.sampleId = sampleId;
        this.sampleCollectedAt = Instant.now();
        this.status = LabOrderStatus.SAMPLE_COLLECTED;
    }

    public void startProcessing() {
        this.status = LabOrderStatus.PROCESSING;
    }

    public void enterResult(double value, String notes) {
        this.resultValue = value;
        this.resultNotes = notes;
        this.critical = test.isCritical(value);
        this.resultEnteredAt = Instant.now();
        this.status = LabOrderStatus.COMPLETED;
    }

    public void verify() {
        this.verifiedAt = Instant.now();
        this.status = LabOrderStatus.VERIFIED;
    }

    public void cancel() {
        this.status = LabOrderStatus.CANCELLED;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getOrderingDoctor() {
        return orderingDoctor;
    }

    public LabTestCatalog getTest() {
        return test;
    }

    public LabOrderStatus getStatus() {
        return status;
    }

    public String getSampleId() {
        return sampleId;
    }

    public Double getResultValue() {
        return resultValue;
    }

    public String getResultNotes() {
        return resultNotes;
    }

    public boolean isCritical() {
        return critical;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }
}
