package com.medisphere.patient;

import com.medisphere.patient.dto.PatientResponse;
import com.medisphere.patient.dto.UpdatePatientRequest;
import com.medisphere.security.CurrentUser;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    public Page<PatientResponse> search(@RequestParam(required = false) String q, Pageable pageable) {
        return patientService.search(q, pageable);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public PatientResponse me() {
        return patientService.getResponseByUserId(CurrentUser.get().getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST','PATIENT')")
    public PatientResponse get(@PathVariable UUID id) {
        requireOwnRecordIfPatient(id);
        return patientService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public PatientResponse update(@PathVariable UUID id, @RequestBody UpdatePatientRequest req) {
        requireOwnRecordIfPatient(id);
        return patientService.update(id, req);
    }

    /** A patient can only ever look up or edit their own record, never another patient's by guessing an id. */
    private void requireOwnRecordIfPatient(UUID patientId) {
        var user = CurrentUser.get();
        if (user.getRole() == com.medisphere.user.Role.PATIENT) {
            Patient own = patientService.getByUserId(user.getId());
            if (!own.getId().equals(patientId)) {
                throw new org.springframework.security.access.AccessDeniedException("Patients may only access their own record");
            }
        }
    }
}
