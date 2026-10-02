package com.medisphere.pharmacy;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescription_items")
public class PrescriptionItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private String dosageInstructions;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean dispensed = false;

    protected PrescriptionItem() {
    }

    public PrescriptionItem(Medicine medicine, String dosageInstructions, int quantity) {
        this.medicine = medicine;
        this.dosageInstructions = dosageInstructions;
        this.quantity = quantity;
    }

    void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public String getDosageInstructions() {
        return dosageInstructions;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isDispensed() {
        return dispensed;
    }

    public void markDispensed() {
        this.dispensed = true;
    }
}
