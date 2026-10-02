package com.medisphere.laboratory.dto;

import jakarta.validation.constraints.NotNull;

public record EnterResultRequest(@NotNull Double value, String notes) {
}
