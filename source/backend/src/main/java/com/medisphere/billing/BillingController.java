package com.medisphere.billing;

import com.medisphere.billing.dto.AddPaymentRequest;
import com.medisphere.billing.dto.CreateInvoiceRequest;
import com.medisphere.billing.dto.InvoiceResponse;
import com.medisphere.patient.PatientService;
import com.medisphere.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final BillingService billingService;
    private final PatientService patientService;

    public BillingController(BillingService billingService, PatientService patientService) {
        this.billingService = billingService;
        this.patientService = patientService;
    }

    @PostMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PHARMACIST')")
    public InvoiceResponse createInvoice(@Valid @RequestBody CreateInvoiceRequest req) {
        return billingService.createInvoice(req);
    }

    @PostMapping("/invoices/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public InvoiceResponse addPayment(@PathVariable UUID id, @Valid @RequestBody AddPaymentRequest req) {
        return billingService.addPayment(id, req);
    }

    @PostMapping("/invoices/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public void cancel(@PathVariable UUID id) {
        billingService.cancel(id);
    }

    @GetMapping("/invoices/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public List<InvoiceResponse> forPatient(@PathVariable UUID patientId) {
        return billingService.forPatient(patientId);
    }

    @GetMapping("/invoices/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<InvoiceResponse> mine() {
        return billingService.forPatient(patientService.getByUserId(CurrentUser.get().getId()).getId());
    }
}
