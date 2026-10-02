package com.medisphere.department.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
        @NotBlank String name,
        @Min(1) int capacity,
        String description,
        Integer defaultConsultationMinutes
) {
}
