package com.medisphere.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record RegisterPatientRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String phone,
        @Past LocalDate dateOfBirth,
        @NotBlank String gender,
        String bloodGroup,
        String address,
        String emergencyContactName,
        String emergencyContactPhone
) {
}
