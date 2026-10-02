package com.medisphere.feedback;

import com.medisphere.common.entity.BaseEntity;
import com.medisphere.patient.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** One post-visit survey. Each rating is 1 (worst) to 10 (best). */
@Entity
@Table(name = "patient_feedback")
public class PatientFeedback extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private int waitingRating;
    @Column(nullable = false)
    private int doctorRating;
    @Column(nullable = false)
    private int staffRating;
    @Column(nullable = false)
    private int cleanlinessRating;
    @Column(nullable = false)
    private int billingRating;
    @Column(nullable = false)
    private int communicationRating;

    @Column(length = 2000)
    private String comments;

    protected PatientFeedback() {
    }

    public PatientFeedback(Patient patient, int waitingRating, int doctorRating, int staffRating,
                            int cleanlinessRating, int billingRating, int communicationRating, String comments) {
        this.patient = patient;
        this.waitingRating = waitingRating;
        this.doctorRating = doctorRating;
        this.staffRating = staffRating;
        this.cleanlinessRating = cleanlinessRating;
        this.billingRating = billingRating;
        this.communicationRating = communicationRating;
        this.comments = comments;
    }

    public Patient getPatient() {
        return patient;
    }

    public int getWaitingRating() {
        return waitingRating;
    }

    public int getDoctorRating() {
        return doctorRating;
    }

    public int getStaffRating() {
        return staffRating;
    }

    public int getCleanlinessRating() {
        return cleanlinessRating;
    }

    public int getBillingRating() {
        return billingRating;
    }

    public int getCommunicationRating() {
        return communicationRating;
    }

    public String getComments() {
        return comments;
    }
}
