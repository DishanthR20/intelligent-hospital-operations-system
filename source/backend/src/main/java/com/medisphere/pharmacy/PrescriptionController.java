package com.medisphere.pharmacy;

import com.medisphere.patient.PatientService;
import com.medisphere.pharmacy.dto.CreatePrescriptionRequest;
import com.medisphere.pharmacy.dto.PendingPrescriptionItemResponse;
import com.medisphere.pharmacy.dto.PrescriptionResponse;
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
@RequestMapping("/api/v1/pharmacy/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PatientService patientService;

    public PrescriptionController(PrescriptionService prescriptionService, PatientService patientService) {
        this.prescriptionService = prescriptionService;
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public PrescriptionResponse create(@Valid @RequestBody CreatePrescriptionRequest req) {
        return prescriptionService.create(req);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','PHARMACIST','PATIENT')")
    public List<PrescriptionResponse> forPatient(@PathVariable UUID patientId) {
        return prescriptionService.forPatient(patientId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<PrescriptionResponse> mine() {
        return prescriptionService.forPatient(patientService.getByUserId(CurrentUser.get().getId()).getId());
    }

    @GetMapping("/pending-items")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public List<PendingPrescriptionItemResponse> pendingItems() {
        return prescriptionService.pendingItems();
    }

    @PostMapping("/items/{itemId}/dispense")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public void dispense(@PathVariable UUID itemId) {
        prescriptionService.dispense(itemId);
    }
}
