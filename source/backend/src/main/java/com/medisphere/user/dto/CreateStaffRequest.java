package com.medisphere.user.dto;

import com.medisphere.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Admin-only staff onboarding. When role is DOCTOR the department/specialization
 * fields are required; for the other staff roles no extra profile exists yet,
 * so a bare User account is sufficient.
 */
public record CreateStaffRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String phone,
        @NotNull Role role,
        UUID departmentId,
        String specialization,
        String qualification,
        Integer experienceYears
) {
}
