package com.medisphere.queue.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddToQueueRequest(
        @NotNull UUID patientId,
        @NotNull UUID departmentId,
        boolean emergency,
        int clinicalRisk
) {
}
