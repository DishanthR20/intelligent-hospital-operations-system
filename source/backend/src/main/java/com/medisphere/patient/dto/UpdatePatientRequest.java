package com.medisphere.patient.dto;

import java.util.Set;

public record UpdatePatientRequest(
        String phone,
        String bloodGroup,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        String insuranceProvider,
        String insurancePolicyNumber,
        Set<String> allergies,
        Set<String> chronicConditions,
        Set<String> currentMedications
) {
}
