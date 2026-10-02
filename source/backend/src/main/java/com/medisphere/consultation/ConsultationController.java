package com.medisphere.consultation;

import com.medisphere.consultation.dto.ConsultationResponse;
import com.medisphere.consultation.dto.CreateConsultationRequest;
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
@RequestMapping("/api/v1/consultations")
public class ConsultationController {

    private final ConsultationService service;
    private final PatientService patientService;

    public ConsultationController(ConsultationService service, PatientService patientService) {
        this.service = service;
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ConsultationResponse create(@Valid @RequestBody CreateConsultationRequest req) {
        return service.create(req);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','PATIENT')")
    public List<ConsultationResponse> forPatient(@PathVariable UUID patientId) {
        return service.forPatient(patientId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<ConsultationResponse> mine() {
        return service.forPatient(patientService.getByUserId(CurrentUser.get().getId()).getId());
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public List<ConsultationResponse> forDoctor(@PathVariable UUID doctorId) {
        return service.forDoctor(doctorId);
    }
}
