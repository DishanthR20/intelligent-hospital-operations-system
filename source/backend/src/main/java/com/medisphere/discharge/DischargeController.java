package com.medisphere.discharge;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/discharge")
@PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE')")
public class DischargeController {

    private final DischargeService service;

    public DischargeController(DischargeService service) {
        this.service = service;
    }

    @PostMapping("/patient/{patientId}/start")
    public DischargeRecord start(@PathVariable UUID patientId) {
        return service.start(patientId);
    }

    @PostMapping("/{id}/checklist")
    public DischargeRecord updateChecklist(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String instructions = (String) body.remove("patientInstructions");
        Map<String, Boolean> checklist = body.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> (Boolean) e.getValue()));
        return service.updateChecklist(id, checklist, instructions);
    }

    @PostMapping("/{id}/complete")
    public DischargeRecord complete(@PathVariable UUID id) {
        return service.complete(id);
    }

    @GetMapping("/patient/{patientId}")
    public List<DischargeRecord> forPatient(@PathVariable UUID patientId) {
        return service.forPatient(patientId);
    }
}
