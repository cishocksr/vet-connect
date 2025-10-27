package com.vetconnect.exception;

/**
 * Exception thrown when attempting to register with an email that already exists
 *
 * USE CASE: User registration with duplicate email
 *
 * HTTP STATUS: 409 Conflict
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(String.format("Email already in use: %s", email));
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}