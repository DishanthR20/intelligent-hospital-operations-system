package com.medisphere.laboratory.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OrderLabTestRequest(@NotNull UUID patientId, @NotNull UUID testId) {
}
