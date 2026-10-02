package com.medisphere.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreatePrescriptionRequest(
        @NotNull UUID patientId,
        UUID consultationId,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(@NotNull UUID medicineId, @NotNull String dosageInstructions, int quantity) {
    }
}
