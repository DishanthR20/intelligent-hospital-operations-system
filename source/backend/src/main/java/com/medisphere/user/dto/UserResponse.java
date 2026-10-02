package com.medisphere.user.dto;

import com.medisphere.user.Role;
import com.medisphere.user.User;
import java.util.UUID;

public record UserResponse(UUID id, String fullName, String email, String phone, Role role, boolean enabled) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(), u.isEnabled());
    }
}
