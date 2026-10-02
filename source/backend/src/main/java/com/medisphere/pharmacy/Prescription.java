package com.medisphere.pharmacy;

import com.medisphere.common.entity.BaseEntity;
import com.medisphere.consultation.Consultation;
import com.medisphere.doctor.Doctor;
import com.medisphere.patient.Patient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions")
public class Prescription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PrescriptionItem> items = new ArrayList<>();

    protected Prescription() {
    }

    public Prescription(Patient patient, Doctor doctor, Consultation consultation) {
        this.patient = patient;
        this.doctor = doctor;
        this.consultation = consultation;
    }

    public Patient getPatient() {
        return patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public Consultation getConsultation() {
        return consultation;
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }

    public void addItem(PrescriptionItem item) {
        item.setPrescription(this);
        items.add(item);
    }
}
