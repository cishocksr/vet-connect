package com.vetconnect.exception;

/**
 * Exception thrown when login credentials are invalid
 *
 * USE CASE: User login with wrong email or password
 *
 * HTTP STATUS: 401 Unauthorized
 *
 * SECURITY NOTE:
 * Don't specify whether email or password is wrong
 * Just say "Invalid credentials" to prevent user enumeration
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}