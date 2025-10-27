package com.vetconnect.exception;

/**
 * Exception thrown when attempting to create a duplicate resource
 *
 * USE CASES:
 * - Saving a resource that's already saved
 * - Creating a category that already exists
 *
 * HTTP STATUS: 409 Conflict
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceType, String identifier) {
        super(String.format("%s already exists: %s", resourceType, identifier));
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}