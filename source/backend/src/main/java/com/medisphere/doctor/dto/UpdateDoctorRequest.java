package com.medisphere.doctor.dto;

import java.util.UUID;

public record UpdateDoctorRequest(
        UUID departmentId, String specialization, String qualification,
        Integer experienceYears, Integer consultationMinutes, Boolean active
) {
}
