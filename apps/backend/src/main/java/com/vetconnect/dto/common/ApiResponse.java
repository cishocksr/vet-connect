package com.vetconnect.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * Standard API Response wrapper
 * Used to provide consistent response structure across all endpoints
 *
 * Example success response:
 * {
 *   "success": true,
 *   "message": "User created successfully",
 *   "data": { ... user object ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * Example error response:
 * {
 *   "success": false,
 *   "message": "User not found",
 *   "error": "No user exists with ID: 123",
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Don't include null fields in JSON
public class ApiResponse<T> {

    /**
     * Indicates if the request was successful
     * true = success, false = error
     */
    private boolean success;

    /**
     * Human-readable message about the result
     * Examples: "Resource saved successfully", "Invalid credentials"
     */
    private String message;

    /**
     * The actual data payload (generic type T)
     * Can be a single object, list, or null for simple confirmations
     */
    private T data;

    /**
     * Error details (only present when success = false)
     * Provides additional context about what went wrong
     */
    private String error;

    /**
     * Timestamp when the response was generated
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ========== CONVENIENCE FACTORY METHODS ==========

    /**
     * Create a success response with data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response without data (for confirmations)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response
     */
    public static <T> ApiResponse<T> error(String message, String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a simple error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}