package com.medisphere.consultation;

import com.medisphere.appointment.Appointment;
import com.medisphere.common.entity.BaseEntity;
import com.medisphere.doctor.Doctor;
import com.medisphere.patient.Patient;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A single doctor-patient encounter. This, together with lab results and
 * discharge records, forms the patient's normalized medical record — deliberately
 * not one giant free-text field.
 */
@Entity
@Table(name = "consultations")
public class Consultation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(nullable = false, length = 2000)
    private String notes;

    private String diagnosis;

    private String followUpPlan;

    protected Consultation() {
    }

    public Consultation(Patient patient, Doctor doctor, Appointment appointment, String notes, String diagnosis, String followUpPlan) {
        this.patient = patient;
        this.doctor = doctor;
        this.appointment = appointment;
        this.notes = notes;
        this.diagnosis = diagnosis;
        this.followUpPlan = followUpPlan;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public String getNotes() {
        return notes;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getFollowUpPlan() {
        return followUpPlan;
    }
}
