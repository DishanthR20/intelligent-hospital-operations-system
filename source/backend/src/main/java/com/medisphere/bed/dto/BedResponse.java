package com.medisphere.bed.dto;

import com.medisphere.bed.Bed;
import java.util.UUID;

public record BedResponse(UUID id, UUID wardId, String wardName, String bedNumber, String status, UUID currentPatientId) {
    public static BedResponse from(Bed bed) {
        return new BedResponse(bed.getId(), bed.getWard().getId(), bed.getWard().getName(),
                bed.getBedNumber(), bed.getStatus().name(), bed.getCurrentPatientId());
    }
}
