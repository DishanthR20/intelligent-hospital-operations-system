package com.medisphere.pharmacy;

import java.util.UUID;

public record MedicineDemandReport(
        UUID medicineId, String medicineName, int currentStock, double averageDailyUsage,
        Double estimatedRemainingDays, ReorderStatus status, String explanation
) {
}
