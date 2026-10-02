package com.medisphere.consultation.dto;

import com.medisphere.consultation.Consultation;
import java.time.Instant;
import java.util.UUID;

public record ConsultationResponse(
        UUID id, UUID patientId, UUID doctorId, String doctorName,
        String notes, String diagnosis, String followUpPlan, Instant createdAt
) {
    public static ConsultationResponse from(Consultation c) {
        return new ConsultationResponse(c.getId(), c.getPatient().getId(), c.getDoctor().getId(),
                c.getDoctor().getUser().getFullName(), c.getNotes(), c.getDiagnosis(), c.getFollowUpPlan(),
                c.getCreatedAt());
    }
}
