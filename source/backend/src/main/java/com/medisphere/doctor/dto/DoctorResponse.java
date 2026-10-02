package com.medisphere.doctor.dto;

import com.medisphere.doctor.Doctor;
import java.util.UUID;

public record DoctorResponse(
        UUID id, UUID userId, String fullName, String email, UUID departmentId, String departmentName,
        String specialization, String qualification, int experienceYears, int consultationMinutes,
        double rating, boolean active
) {
    public static DoctorResponse from(Doctor d) {
        return new DoctorResponse(d.getId(), d.getUser().getId(), d.getUser().getFullName(), d.getUser().getEmail(),
                d.getDepartment().getId(), d.getDepartment().getName(), d.getSpecialization(), d.getQualification(),
                d.getExperienceYears(), d.getConsultationMinutes(), d.getRating(), d.isActive());
    }
}
