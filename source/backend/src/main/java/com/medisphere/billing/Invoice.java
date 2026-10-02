package com.medisphere.billing;

import com.medisphere.common.entity.BaseEntity;
import com.medisphere.patient.Patient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InvoiceItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Payment> payments = new ArrayList<>();

    protected Invoice() {
    }

    public Invoice(Patient patient) {
        this.patient = patient;
    }

    public void addItem(InvoiceItem item) {
        item.setInvoice(this);
        items.add(item);
    }

    public BigDecimal totalAmount() {
        return items.stream().map(InvoiceItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalPaid() {
        return payments.stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal balanceDue() {
        return totalAmount().subtract(totalPaid());
    }

    public void recordPayment(Payment payment) {
        payment.setInvoice(this);
        payments.add(payment);
        if (balanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            status = InvoiceStatus.PAID;
        } else if (totalPaid().compareTo(BigDecimal.ZERO) > 0) {
            status = InvoiceStatus.PARTIALLY_PAID;
        }
    }

    public void cancel() {
        this.status = InvoiceStatus.CANCELLED;
    }

    public Patient getPatient() {
        return patient;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public List<Payment> getPayments() {
        return payments;
    }
}
