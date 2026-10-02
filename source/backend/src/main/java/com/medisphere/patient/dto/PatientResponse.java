package com.medisphere.patient.dto;

import com.medisphere.patient.Patient;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        UUID userId,
        String fullName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        int age,
        String gender,
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
    public static PatientResponse from(Patient p) {
        return new PatientResponse(
                p.getId(), p.getUser().getId(), p.getUser().getFullName(), p.getUser().getEmail(),
                p.getUser().getPhone(), p.getDateOfBirth(), p.getAge(), p.getGender().name(),
                p.getBloodGroup(), p.getAddress(), p.getEmergencyContactName(), p.getEmergencyContactPhone(),
                p.getInsuranceProvider(), p.getInsurancePolicyNumber(), p.getAllergies(),
                p.getChronicConditions(), p.getCurrentMedications());
    }
}
