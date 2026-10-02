package com.medisphere.pharmacy.dto;

import com.medisphere.pharmacy.Prescription;
import com.medisphere.pharmacy.PrescriptionItem;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PrescriptionResponse(
        UUID id, UUID patientId, UUID doctorId, String doctorName, Instant createdAt, List<Item> items
) {
    public record Item(UUID id, UUID medicineId, String medicineName, String dosageInstructions, int quantity, boolean dispensed) {
        static Item from(PrescriptionItem i) {
            return new Item(i.getId(), i.getMedicine().getId(), i.getMedicine().getName(),
                    i.getDosageInstructions(), i.getQuantity(), i.isDispensed());
        }
    }

    public static PrescriptionResponse from(Prescription p) {
        return new PrescriptionResponse(p.getId(), p.getPatient().getId(), p.getDoctor().getId(),
                p.getDoctor().getUser().getFullName(), p.getCreatedAt(),
                p.getItems().stream().map(Item::from).toList());
    }
}
