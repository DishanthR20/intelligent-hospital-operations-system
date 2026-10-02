package com.medisphere.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record AddBatchRequest(
        @NotBlank String batchNumber, @NotNull @jakarta.validation.constraints.Future LocalDate expiryDate,
        String supplier, @Positive int quantity
) {
}
