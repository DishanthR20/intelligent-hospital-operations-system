package com.medisphere.billing;

import com.medisphere.audit.AuditAction;
import com.medisphere.audit.AuditService;
import com.medisphere.billing.dto.AddPaymentRequest;
import com.medisphere.billing.dto.CreateInvoiceRequest;
import com.medisphere.billing.dto.InvoiceResponse;
import com.medisphere.common.exception.ConflictException;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.event.PaymentCompletedEvent;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PatientService patientService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    public BillingService(InvoiceRepository invoiceRepository, PatientService patientService,
                           ApplicationEventPublisher eventPublisher, AuditService auditService) {
        this.invoiceRepository = invoiceRepository;
        this.patientService = patientService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest req) {
        Patient patient = patientService.getEntity(req.patientId());
        Invoice invoice = new Invoice(patient);
        for (var item : req.items()) {
            invoice.addItem(new InvoiceItem(item.description(), item.category(), item.unitPrice(), item.quantity()));
        }
        invoiceRepository.save(invoice);
        auditService.record(AuditAction.BILL_UPDATE, "Invoice", invoice.getId().toString());
        return InvoiceResponse.from(invoice);
    }

    public InvoiceResponse addPayment(UUID invoiceId, AddPaymentRequest req) {
        Invoice invoice = getEntity(invoiceId);
        if (req.amount().compareTo(invoice.balanceDue()) > 0) {
            throw new ConflictException("OVERPAYMENT", "Payment amount exceeds the outstanding balance of " + invoice.balanceDue());
        }
        invoice.recordPayment(new Payment(req.amount(), req.method()));
        auditService.record(AuditAction.BILL_UPDATE, "Invoice", invoice.getId().toString());
        eventPublisher.publishEvent(new PaymentCompletedEvent(invoice.getPatient().getId(), invoice.getId(), req.amount()));
        return InvoiceResponse.from(invoice);
    }

    public void cancel(UUID invoiceId) {
        getEntity(invoiceId).cancel();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> forPatient(UUID patientId) {
        return invoiceRepository.findByPatient_IdOrderByCreatedAtDesc(patientId).stream()
                .map(InvoiceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Invoice getEntity(UUID id) {
        return invoiceRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Invoice", id));
    }

    @Transactional(readOnly = true)
    public java.math.BigDecimal revenueToday() {
        Instant start = java.time.LocalDate.now().atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        return invoiceRepository.findByCreatedAtBetween(start, Instant.now()).stream()
                .flatMap(inv -> inv.getPayments().stream())
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
