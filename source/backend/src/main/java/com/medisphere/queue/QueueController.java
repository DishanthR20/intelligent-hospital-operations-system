package com.medisphere.queue;

import com.medisphere.queue.dto.AddToQueueRequest;
import com.medisphere.queue.dto.QueueEntryResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/queues")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    /** Token number only — the entity itself carries lazy patient/department/doctor
     *  references that must not be serialized outside the service's transaction. */
    public record QueueAddedResponse(UUID id, String tokenNumber) {
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','NURSE')")
    public QueueAddedResponse addToQueue(@Valid @RequestBody AddToQueueRequest req) {
        QueueEntry entry = queueService.addToQueue(req.patientId(), req.departmentId(), req.emergency(), req.clinicalRisk());
        return new QueueAddedResponse(entry.getId(), entry.getTokenNumber());
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    public List<QueueEntryResponse> departmentQueue(@PathVariable UUID departmentId) {
        return queueService.getRankedQueue(departmentId);
    }

    @GetMapping("/emergency")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    public List<QueueEntryResponse> emergencyQueue() {
        return queueService.getEmergencyQueue();
    }

    @PostMapping("/{id}/clinical-risk")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE','DOCTOR')")
    public void updateClinicalRisk(@PathVariable UUID id, @RequestBody Map<String, Integer> body) {
        queueService.updateClinicalRisk(id, body.getOrDefault("clinicalRisk", 0));
    }

    @PostMapping("/{id}/assign-doctor")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE','RECEPTIONIST')")
    public void assignDoctor(@PathVariable UUID id, @RequestBody Map<String, UUID> body) {
        queueService.assignDoctor(id, body.get("doctorId"));
    }

    @PostMapping("/{id}/start-consultation")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public void startConsultation(@PathVariable UUID id) {
        queueService.startConsultation(id);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public void complete(@PathVariable UUID id) {
        queueService.complete(id);
    }
}
