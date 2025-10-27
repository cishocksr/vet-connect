package com.vetconnect.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception thrown for validation errors
 *
 * USE CASES:
 * - Invalid input data
 * - Business rule violations
 * - Constraint violations
 *
 * HTTP STATUS: 400 Bad Request
 */
@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public ValidationException(String field, String errorMessage) {
        super("Validation failed");
        this.errors = new HashMap<>();
        this.errors.put(field, errorMessage);
    }

    public void addError(String field, String errorMessage) {
        this.errors.put(field, errorMessage);
    }
}