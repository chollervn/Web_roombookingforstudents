package com.ecom.exception;

import lombok.Getter;

/**
 * Custom exception for business logic violations
 * Follows Single Responsibility Principle - only handles business exceptions
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final String userMessage;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.userMessage = message;
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.userMessage = message;
    }

    /**
     * Factory methods for common business exceptions
     */
    public static BusinessException notFound(String entity, Integer id) {
        return new BusinessException("NOT_FOUND",
                String.format("%s with id %d not found", entity, id));
    }

    public static BusinessException unauthorized(String action) {
        return new BusinessException("UNAUTHORIZED",
                String.format("Unauthorized to perform: %s", action));
    }

    public static BusinessException invalidState(String message) {
        return new BusinessException("INVALID_STATE", message);
    }

    public static BusinessException validationFailed(String message) {
        return new BusinessException("VALIDATION_FAILED", message);
    }
}
