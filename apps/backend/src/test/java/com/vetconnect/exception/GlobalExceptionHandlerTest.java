package com.vetconnect.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFoundException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Resource not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle EmailAlreadyExistsException")
    void testHandleEmailAlreadyExistsException() {
        // Arrange
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("Email already exists");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEmailAlreadyExistsException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle DuplicateResourceException")
    void testHandleDuplicateResourceException() {
        // Arrange
        DuplicateResourceException exception = new DuplicateResourceException("Duplicate resource");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDuplicateResourceException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle InvalidCredentialsException")
    void testHandleInvalidCredentialsException() {
        // Arrange
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidCredentialsException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle InvalidTokenException")
    void testHandleInvalidTokenException() {
        // Arrange
        InvalidTokenException exception = new InvalidTokenException("Invalid token");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidTokenException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle UnauthorizedException")
    void testHandleUnauthorizedException() {
        // Arrange
        UnauthorizedException exception = new UnauthorizedException("Unauthorized access");

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnauthorizedException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    @DisplayName("Should handle ValidationException")
    void testHandleValidationException() {
        // Arrange
        Map<String, String> errors = new HashMap<>();
        errors.put("email", "Email is required");
        ValidationException exception = new ValidationException("Validation failed", errors);

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(exception, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
    }
}
