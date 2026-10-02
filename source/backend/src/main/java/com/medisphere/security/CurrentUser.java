package com.medisphere.security;

import com.medisphere.user.User;
import org.springframework.security.core.context.SecurityContextHolder;

/** Small helper to read the authenticated {@link User} out of the security context. */
public final class CurrentUser {
    private CurrentUser() {
    }

    public static User get() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
