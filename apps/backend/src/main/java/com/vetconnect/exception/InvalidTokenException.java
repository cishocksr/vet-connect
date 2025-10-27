package com.vetconnect.exception;

/**
 * Exception thrown when JWT token is invalid or expired
 *
 * USE CASES:
 * - Token signature invalid
 * - Token expired
 * - Token format incorrect
 * - Refresh token invalid
 *
 * HTTP STATUS: 401 Unauthorized
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}