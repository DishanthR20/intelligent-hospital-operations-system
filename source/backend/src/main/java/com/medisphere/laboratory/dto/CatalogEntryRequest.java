package com.medisphere.laboratory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CatalogEntryRequest(
        @NotBlank String name, @NotNull BigDecimal price, String unit,
        Double normalRangeLow, Double normalRangeHigh, Double criticalLow, Double criticalHigh
) {
}
