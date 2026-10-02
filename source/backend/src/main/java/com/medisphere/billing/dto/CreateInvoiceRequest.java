package com.medisphere.billing.dto;

import com.medisphere.billing.BillingCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateInvoiceRequest(
        @NotNull UUID patientId,
        @NotEmpty @Valid List<Item> items
) {
    public record Item(String description, BillingCategory category, BigDecimal unitPrice, int quantity) {
    }
}
