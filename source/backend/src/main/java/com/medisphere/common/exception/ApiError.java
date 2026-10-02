package com.medisphere.common.exception;

import java.time.Instant;
import java.util.List;

/** Consistent error envelope returned to clients. Never carries a raw stack trace. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public ApiError(int status, String error, String message, String path) {
        this(Instant.now(), status, error, message, path, List.of());
    }

    public ApiError(int status, String error, String message, String path, List<String> details) {
        this(Instant.now(), status, error, message, path, details);
    }
}
