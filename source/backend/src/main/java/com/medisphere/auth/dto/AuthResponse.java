package com.medisphere.auth.dto;

import com.medisphere.user.Role;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String fullName,
        String email,
        Role role
) {
}
