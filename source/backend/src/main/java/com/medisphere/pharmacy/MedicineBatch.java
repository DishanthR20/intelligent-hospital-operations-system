package com.medisphere.pharmacy;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "medicine_batches")
public class MedicineBatch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private String batchNumber;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private String supplier;

    @Column(nullable = false)
    private int quantity;

    protected MedicineBatch() {
    }

    public MedicineBatch(Medicine medicine, String batchNumber, LocalDate expiryDate, String supplier, int quantity) {
        this.medicine = medicine;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
        this.quantity = quantity;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getSupplier() {
        return supplier;
    }

    public int getQuantity() {
        return quantity;
    }

    public void reduce(int amount) {
        this.quantity -= amount;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }
}
