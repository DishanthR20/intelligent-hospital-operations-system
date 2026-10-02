package com.medisphere.common.exception;

/** Thrown when an operation would violate a business invariant (e.g. a double-booked slot). */
public class ConflictException extends RuntimeException {
    private final String errorCode;

    public ConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
