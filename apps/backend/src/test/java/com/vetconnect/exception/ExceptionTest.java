package com.vetconnect.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all custom exception classes
 * These are simple tests since exceptions are just data containers
 */
class ExceptionsTest {

    // ========== ResourceNotFoundException ==========

    @Test
    void resourceNotFoundException_WithMessage_ShouldCreateException() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void resourceNotFoundException_WithTypeAndIdentifier_ShouldFormatMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("User", "123");

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("123"));
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void resourceNotFoundException_WithMessageAndCause_ShouldIncludeBoth() {
        Throwable cause = new RuntimeException("Original error");
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource error", cause);

        assertNotNull(exception);
        assertEquals("Resource error", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    // ========== InvalidCredentialsException ==========

    @Test
    void invalidCredentialsException_ShouldCreateException() {
        String message = "Invalid username or password";
        InvalidCredentialsException exception = new InvalidCredentialsException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    // ========== InvalidTokenException ==========

    @Test
    void invalidTokenException_ShouldCreateException() {
        String message = "Token expired";
        InvalidTokenException exception = new InvalidTokenException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    // ========== UnauthorizedException ==========

    @Test
    void unauthorizedException_ShouldCreateException() {
        String message = "Access denied";
        UnauthorizedException exception = new UnauthorizedException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    // ========== ValidationException ==========

    @Test
    void validationException_WithMessage_ShouldCreateException() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
        assertNotNull(exception.getErrors());
        assertTrue(exception.getErrors().isEmpty());
    }

    @Test
    void validationException_WithMessageAndErrors_ShouldIncludeBoth() {
        Map<String, String> errors = new HashMap<>();
        errors.put("email", "Email is invalid");
        errors.put("password", "Password too short");

        ValidationException exception = new ValidationException("Validation failed", errors);

        assertNotNull(exception);
        assertEquals("Validation failed", exception.getMessage());
        assertNotNull(exception.getErrors());
        assertEquals(2, exception.getErrors().size());
        assertTrue(exception.getErrors().containsKey("email"));
        assertTrue(exception.getErrors().containsKey("password"));
    }

    @Test
    void validationException_WithFieldAndError_ShouldCreateSingleError() {
        ValidationException exception = new ValidationException("email", "Email is required");

        assertNotNull(exception);
        assertEquals("Validation failed", exception.getMessage());
        assertNotNull(exception.getErrors());
        assertEquals(1, exception.getErrors().size());
        assertEquals("Email is required", exception.getErrors().get("email"));
    }

    // ========== EmailAlreadyExistsException ==========

    @Test
    void emailAlreadyExistsException_ShouldCreateException() {
        String email = "test@example.com";
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(email);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Email already in use"));
        assertTrue(exception.getMessage().contains(email));
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void emailAlreadyExistsException_WithMessageAndCause_ShouldIncludeBoth() {
        Throwable cause = new RuntimeException("Database error");
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("Email conflict", cause);

        assertNotNull(exception);
        assertEquals("Email conflict", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    // ========== DuplicateResourceException ==========

    @Test
    void duplicateResourceException_ShouldCreateException() {
        String message = "Resource already exists";
        DuplicateResourceException exception = new DuplicateResourceException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    // ========== RateLimitException ==========

    @Test
    void rateLimitException_ShouldCreateException() {
        String message = "Too many requests";
        RateLimitException exception = new RateLimitException(message);

        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    // ========== ErrorResponse ==========

    @Test
    void errorResponse_WithBuilder_ShouldCreateResponse() {
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .path("/api/resources/123")
                .build();

        assertNotNull(response);
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Resource not found", response.getMessage());
        assertEquals("/api/resources/123", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void errorResponse_WithCreateMethod_ShouldCreateSimpleResponse() {
        ErrorResponse response = ErrorResponse.create(
                500,
                "Internal Server Error",
                "Something went wrong",
                "/api/test"
        );

        assertNotNull(response);
        assertEquals(500, response.getStatus());
        assertEquals("Internal Server Error", response.getError());
        assertEquals("Something went wrong", response.getMessage());
        assertEquals("/api/test", response.getPath());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void errorResponse_WithValidationErrors_ShouldIncludeThem() {
        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put("email", "Email is required");
        validationErrors.put("password", "Password too short");

        ErrorResponse response = ErrorResponse.createValidation(
                "Validation failed",
                "/api/users",
                validationErrors
        );

        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Validation failed", response.getMessage());
        assertEquals("/api/users", response.getPath());
        assertNotNull(response.getValidationErrors());
        assertEquals(2, response.getValidationErrors().size());
        assertTrue(response.getValidationErrors().containsKey("email"));
        assertTrue(response.getValidationErrors().containsKey("password"));
    }

    @Test
    void errorResponse_WithDetails_ShouldIncludeThem() {
        List<String> details = List.of("Detail 1", "Detail 2");

        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Invalid input")
                .path("/api/test")
                .details(details)
                .build();

        assertNotNull(response);
        assertNotNull(response.getDetails());
        assertEquals(2, response.getDetails().size());
        assertTrue(response.getDetails().contains("Detail 1"));
    }

    @Test
    void errorResponse_WithStackTrace_ShouldIncludeIt() {
        String stackTrace = "java.lang.Exception: Test\n\tat com.example.Test.method()";

        ErrorResponse response = ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("Error occurred")
                .path("/api/test")
                .stackTrace(stackTrace)
                .build();

        assertNotNull(response);
        assertNotNull(response.getStackTrace());
        assertTrue(response.getStackTrace().contains("java.lang.Exception"));
    }

    @Test
    void errorResponse_DefaultTimestamp_ShouldBeNow() {
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Test")
                .path("/api/test")
                .build();

        assertNotNull(response.getTimestamp());
        // Timestamp should be close to now (within 1 second)
        assertTrue(response.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void errorResponse_WithNoArgsConstructor_ShouldWork() {
        ErrorResponse response = new ErrorResponse();
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage("Test");
        response.setPath("/api/test");

        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Test", response.getMessage());
        assertEquals("/api/test", response.getPath());
    }

    @Test
    void errorResponse_WithAllArgsConstructor_ShouldWork() {
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> validationErrors = new HashMap<>();
        validationErrors.put("field", "error");

        ErrorResponse response = new ErrorResponse(
                timestamp,
                404,
                "Not Found",
                "Message",
                "/path",
                validationErrors,
                null,
                null
        );

        assertEquals(timestamp, response.getTimestamp());
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertNotNull(response.getValidationErrors());
    }

    @Test
    void errorResponse_NullFields_ShouldBeExcludedFromJson() {
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Test")
                .path("/api/test")
                // Not setting validationErrors, details, stackTrace
                .build();

        assertNull(response.getValidationErrors());
        assertNull(response.getDetails());
        assertNull(response.getStackTrace());
    }
}