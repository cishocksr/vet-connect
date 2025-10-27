package com.vetconnect.exception;

/**
 * Exception thrown when user is not authorized to perform an action
 *
 * USE CASES:
 * - Trying to access another user's data
 * - Trying to update another user's saved resources
 * - Missing required permissions
 *
 * HTTP STATUS: 403 Forbidden
 *
 * DIFFERENCE FROM 401:
 * - 401 Unauthorized = Not authenticated (no valid token)
 * - 403 Forbidden = Authenticated but not authorized (don't have permission)
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}