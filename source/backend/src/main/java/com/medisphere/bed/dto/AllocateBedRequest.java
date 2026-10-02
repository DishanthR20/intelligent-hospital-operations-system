package com.medisphere.bed.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AllocateBedRequest(@NotNull UUID patientId, boolean needsIcu, boolean needsIsolation, String genderPolicy) {
}
