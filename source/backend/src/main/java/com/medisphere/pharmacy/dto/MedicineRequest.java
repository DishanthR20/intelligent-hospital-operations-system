package com.medisphere.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record MedicineRequest(
        @NotBlank String name, String genericName, @NotBlank String unit,
        @NotNull @PositiveOrZero BigDecimal pricePerUnit, int reorderThreshold
) {
}
