package com.medisphere.laboratory.dto;

import com.medisphere.laboratory.LabOrder;
import com.medisphere.laboratory.LabTestCatalog;
import java.util.UUID;

public record LabOrderResponse(
        UUID id, PersonRef patient, PersonRef orderingDoctor, LabTestCatalog test,
        String status, String sampleId, Double resultValue, String resultNotes, boolean critical
) {
    public record PersonRef(UUID id, String fullName) {
    }

    public static LabOrderResponse from(LabOrder o) {
        return new LabOrderResponse(o.getId(),
                new PersonRef(o.getPatient().getId(), o.getPatient().getUser().getFullName()),
                new PersonRef(o.getOrderingDoctor().getId(), o.getOrderingDoctor().getUser().getFullName()),
                o.getTest(), o.getStatus().name(), o.getSampleId(), o.getResultValue(), o.getResultNotes(), o.isCritical());
    }
}
