package com.medisphere.consultation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateConsultationRequest(
        @NotNull UUID patientId,
        UUID appointmentId,
        @NotBlank String notes,
        String diagnosis,
        String followUpPlan
) {
}
