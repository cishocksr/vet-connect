package com.vetconnect.exception;

/**
 * Exception thrown when a requested resource is not found
 *
 * USE CASES:
 * - User not found by ID
 * - Resource not found by ID
 * - Category not found
 * - Saved resource not found
 *
 * HTTP STATUS: 404 Not Found
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier));
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}