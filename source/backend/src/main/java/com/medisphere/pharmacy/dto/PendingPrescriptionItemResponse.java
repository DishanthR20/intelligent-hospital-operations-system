package com.medisphere.pharmacy.dto;

import com.medisphere.pharmacy.PrescriptionItem;
import java.util.UUID;

public record PendingPrescriptionItemResponse(
        UUID id, String patientName, UUID medicineId, String medicineName, String dosageInstructions, int quantity
) {
    public static PendingPrescriptionItemResponse from(PrescriptionItem item) {
        return new PendingPrescriptionItemResponse(item.getId(),
                item.getPrescription().getPatient().getUser().getFullName(),
                item.getMedicine().getId(), item.getMedicine().getName(),
                item.getDosageInstructions(), item.getQuantity());
    }
}
