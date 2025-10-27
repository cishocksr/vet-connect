package com.vetconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider
 */
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private UUID userId;
    private String email;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();

        // Set test properties using reflection
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "test-secret-key-for-testing-must-be-at-least-256-bits-long-to-work-properly");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 3600000L); // 1 hour
        ReflectionTestUtils.setField(tokenProvider, "jwtRefreshExpirationMs", 7200000L); // 2 hours

        userId = UUID.randomUUID();
        email = "test@example.com";
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void testGenerateToken() {
        // Act
        String token = tokenProvider.generateTokenFromUserId(userId, email);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void testGetUserIdFromToken() {
        // Arrange
        String token = tokenProvider.generateTokenFromUserId(userId, email);

        // Act
        UUID extractedUserId = tokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Should extract email from token")
    void testGetEmailFromToken() {
        // Arrange
        String token = tokenProvider.generateTokenFromUserId(userId, email);

        // Act
        String extractedEmail = tokenProvider.getEmailFromToken(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Should validate valid token")
    void testValidateToken_Valid() {
        // Arrange
        String token = tokenProvider.generateTokenFromUserId(userId, email);

        // Act
        boolean isValid = tokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should reject invalid token")
    void testValidateToken_Invalid() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = tokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should generate refresh token")
    void testGenerateRefreshToken() {
        // Act
        String refreshToken = tokenProvider.generateRefreshToken(userId, email);

        // Assert
        assertNotNull(refreshToken);
        assertTrue(refreshToken.length() > 0);
    }

    @Test
    @DisplayName("Should check if token is expired")
    void testIsTokenExpired() {
        // Arrange
        String token = tokenProvider.generateTokenFromUserId(userId, email);

        // Act
        boolean isExpired = tokenProvider.isTokenExpired(token);

        // Assert
        assertFalse(isExpired); // Token just created, should not be expired
    }

    @Test
    @DisplayName("Should get expiration date from token")
    void testGetExpirationFromToken() {
        // Arrange
        String token = tokenProvider.generateTokenFromUserId(userId, email);

        // Act
        Date expiration = tokenProvider.getExpirationFromToken(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should expire in the future
    }

    @Test
    @DisplayName("Should calculate time until expiration")
    void testGetTimeUntilExpiration() {
        // Arrange
        String token = tokenProvider.generateTokenFromUserId(userId, email);

        // Act
        long timeUntilExpiration = tokenProvider.getTimeUntilExpiration(token);

        // Assert
        assertTrue(timeUntilExpiration > 0);
        assertTrue(timeUntilExpiration <= 3600000); // Less than or equal to 1 hour
    }
}