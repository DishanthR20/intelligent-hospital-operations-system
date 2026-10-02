package com.medisphere.event;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Hospital Operations Timeline (section 43) and per-patient journey (section 18). */
@RestController
@RequestMapping("/api/v1/events")
public class HospitalEventController {

    private final HospitalEventRepository repository;

    public HospitalEventController(HospitalEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<HospitalEvent> timeline(@RequestParam(defaultValue = "100") int limit) {
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','PATIENT')")
    public List<HospitalEvent> patientJourney(@PathVariable UUID patientId) {
        return repository.findByPatientIdOrderByCreatedAtAsc(patientId);
    }
}
