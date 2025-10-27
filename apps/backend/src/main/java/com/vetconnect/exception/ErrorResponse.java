package com.vetconnect.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response
 *
 * ALL error responses from the API will follow this format
 *
 * Example response:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Resource not found with ID: 123",
 *   "path": "/api/resources/123",
 *   "validationErrors": {
 *     "email": "Email must be valid",
 *     "password": "Password must be at least 8 characters"
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Don't include null fields
public class ErrorResponse {

    /**
     * When the error occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * HTTP status code
     * Examples: 400, 401, 403, 404, 500
     */
    private int status;

    /**
     * HTTP status name
     * Examples: "Bad Request", "Unauthorized", "Not Found"
     */
    private String error;

    /**
     * Detailed error message
     * Human-readable description of what went wrong
     */
    private String message;

    /**
     * Request path that caused the error
     * Example: "/api/resources/123"
     */
    private String path;

    /**
     * Validation errors (for 400 Bad Request)
     * Map of field name → error message
     * Example: {"email": "Email is required", "password": "Password too short"}
     */
    private Map<String, String> validationErrors;

    /**
     * Additional details (optional)
     * Can be used for debugging or providing extra context
     */
    private List<String> details;

    /**
     * Stack trace (only in development)
     * NEVER send this in production!
     */
    private String stackTrace;

    // ========== BUILDER HELPERS ==========

    /**
     * Create simple error response
     */
    public static ErrorResponse create(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Create validation error response
     */
    public static ErrorResponse createValidation(String message, String path,
                                                 Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(400)
                .error("Bad Request")
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }
}