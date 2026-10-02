package com.medisphere.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final String errorCode;

    public ResourceNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static ResourceNotFoundException of(String entity, Object id) {
        return new ResourceNotFoundException(
                entity.toUpperCase() + "_NOT_FOUND",
                entity + " could not be found for id " + id);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
