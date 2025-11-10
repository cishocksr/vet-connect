package com.vetconnect.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler
 *
 * Catches all exceptions thrown by controllers and services
 * Returns consistent, clean error responses
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * Automatically applies to all controllers
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // ========== CUSTOM EXCEPTIONS ==========

    /**
     * Handle ResourceNotFoundException
     *
     * HTTP STATUS: 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle EmailAlreadyExistsException
     *
     * HTTP STATUS: 409 Conflict
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        log.error("Email already exists: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle DuplicateResourceException
     *
     * HTTP STATUS: 409 Conflict
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        log.error("Duplicate resource: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle InvalidCredentialsException
     *
     * HTTP STATUS: 401 Unauthorized
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        log.error("Invalid credentials attempt");

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle InvalidTokenException
     *
     * HTTP STATUS: 401 Unauthorized
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex,
            HttpServletRequest request) {

        log.error("Invalid token: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle UnauthorizedException
     *
     * HTTP STATUS: 403 Forbidden
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {

        log.error("Unauthorized access attempt: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle ValidationException
     *
     * HTTP STATUS: 400 Bad Request
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {

        log.error("Validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.createValidation(
                ex.getMessage(),
                request.getRequestURI(),
                ex.getErrors()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // ========== SPRING VALIDATION EXCEPTIONS ==========

    /**
     * Handle MethodArgumentNotValidException
     *
     * Thrown when @Valid fails on request body
     *
     * HTTP STATUS: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.error("Validation failed: {}", ex.getMessage());

        // Extract field errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.createValidation(
                "Validation failed for request body",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle ConstraintViolationException
     *
     * Thrown when @Valid fails on path/query parameters
     *
     * HTTP STATUS: 400 Bad Request
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        log.error("Constraint violation: {}", ex.getMessage());

        // Extract constraint violations
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        ErrorResponse errorResponse = ErrorResponse.createValidation(
                "Validation failed",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle HttpMessageNotReadableException
     *
     * Thrown when request body is malformed JSON
     *
     * HTTP STATUS: 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        log.error("Malformed JSON request: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Malformed JSON request body",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle MethodArgumentTypeMismatchException
     *
     * Thrown when path/query parameter type is wrong
     * Example: Sending "abc" for UUID parameter
     *
     * HTTP STATUS: 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        log.error("Type mismatch: {}", ex.getMessage());

        String message;

        // In production, don't expose the actual value (could be sensitive)
        if ("prod".equals(activeProfile)) {
            message = String.format(
                    "Invalid value for parameter '%s'. Expected type: %s",
                    ex.getName(),
                    ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
            );
        } else {
            // In dev/test, show the value for easier debugging
            message = String.format(
                    "Invalid value '%s' for parameter '%s'. Expected type: %s",
                    ex.getValue(),
                    ex.getName(),
                    ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
            );
        }

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // ========== SPRING SECURITY EXCEPTIONS ==========

    /**
     * Handle BadCredentialsException
     *
     * Thrown by Spring Security when authentication fails
     *
     * HTTP STATUS: 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.error("Bad credentials: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Invalid email or password",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle AuthenticationException
     *
     * General Spring Security authentication exception
     *
     * HTTP STATUS: 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.error("Authentication failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Authentication failed",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle AccessDeniedException
     *
     * Thrown when user doesn't have permission
     *
     * HTTP STATUS: 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.error("Access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "You do not have permission to access this resource",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // ========== GENERIC EXCEPTIONS ==========

    /**
     * Handle all other RuntimeExceptions
     *
     * Catch-all for unexpected runtime exceptions
     *
     * HTTP STATUS: 400 Bad Request (for now)
     *
     * TODO: Analyze these and create specific exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        log.error("Runtime exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        // Include stack trace in development
        if ("dev".equals(activeProfile)) {
            errorResponse.setStackTrace(getStackTraceAsString(ex));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle all other Exceptions
     *
     * Last resort - something really unexpected happened
     *
     * HTTP STATUS: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        // Include stack trace in development
        if ("dev".equals(activeProfile)) {
            errorResponse.setStackTrace(getStackTraceAsString(ex));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle RateLimitException
     *
     * Thrown when user exceeds rate limit
     *
     * HTTP STATUS: 429 Too Many Requests
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(
            RateLimitException ex,
            HttpServletRequest request) {

        log.warn("Rate limit exceeded: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.create(
                429,  // HTTP 429 Too Many Requests
                "Too Many Requests",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(429).body(errorResponse);
    }

    // ========== HELPER METHODS ==========

    /**
     * Convert exception stack trace to string
     *
     * @param ex Exception
     * @return Stack trace as string
     */
    private String getStackTraceAsString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.toString()).append("\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}