package com.vetconnect.exception;

/**
 * Exception thrown when rate limit is exceeded
 *
 * HTTP Status: 429 Too Many Requests
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}