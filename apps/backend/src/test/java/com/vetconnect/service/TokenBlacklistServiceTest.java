package com.vetconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService Tests")
class TokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private TokenBlacklistService tokenBlacklistService;

    private static final String TEST_TOKEN = "test.jwt.token";
    private static final long EXPIRATION_MS = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(redisTemplate);
        // Don't stub here - stub in each test as needed
    }

    @Test
    @DisplayName("Should blacklist token successfully")
    void testBlacklistToken_Success() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        tokenBlacklistService.blacklistToken(TEST_TOKEN, EXPIRATION_MS);

        // Assert
        verify(valueOperations).set(
                eq("blacklist:token:" + TEST_TOKEN),
                eq("blacklisted"),
                eq(Duration.ofMillis(EXPIRATION_MS))
        );
    }

    @Test
    @DisplayName("Should handle blacklist token failure gracefully")
    void testBlacklistToken_Failure() {
        // Arrange
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> tokenBlacklistService.blacklistToken(TEST_TOKEN, EXPIRATION_MS));
    }

    @Test
    @DisplayName("Should check if token is blacklisted - returns true")
    void testIsTokenBlacklisted_ReturnsTrue() {
        // Arrange
        when(redisTemplate.hasKey("blacklist:token:" + TEST_TOKEN)).thenReturn(true);

        // Act
        boolean result = tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);

        // Assert
        assertTrue(result);
        verify(redisTemplate).hasKey("blacklist:token:" + TEST_TOKEN);
    }

    @Test
    @DisplayName("Should check if token is blacklisted - returns false")
    void testIsTokenBlacklisted_ReturnsFalse() {
        // Arrange
        when(redisTemplate.hasKey("blacklist:token:" + TEST_TOKEN)).thenReturn(false);

        // Act
        boolean result = tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);

        // Assert
        assertFalse(result);
        verify(redisTemplate).hasKey("blacklist:token:" + TEST_TOKEN);
    }

    @Test
    @DisplayName("Should handle check blacklist failure gracefully")
    void testIsTokenBlacklisted_Failure() {
        // Arrange
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        // Act
        boolean result = tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);

        // Assert
        assertFalse(result); // Should return false on error
    }

    @Test
    @DisplayName("Should remove token from blacklist successfully")
    void testRemoveFromBlacklist_Success() {
        // Act
        tokenBlacklistService.removeFromBlacklist(TEST_TOKEN);

        // Assert
        verify(redisTemplate).delete("blacklist:token:" + TEST_TOKEN);
    }

    @Test
    @DisplayName("Should handle remove from blacklist failure gracefully")
    void testRemoveFromBlacklist_Failure() {
        // Arrange
        when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> tokenBlacklistService.removeFromBlacklist(TEST_TOKEN));
    }

    @Test
    @DisplayName("Should blacklist all user tokens successfully")
    void testBlacklistAllUserTokens_Success() {
        // Arrange
        String userId = "user123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        tokenBlacklistService.blacklistAllUserTokens(userId, EXPIRATION_MS);

        // Assert
        verify(valueOperations).set(
                eq("blacklist:token:user:" + userId),
                eq("all_tokens_blacklisted"),
                eq(Duration.ofMillis(EXPIRATION_MS))
        );
    }

    @Test
    @DisplayName("Should handle blacklist all user tokens failure gracefully")
    void testBlacklistAllUserTokens_Failure() {
        // Arrange
        String userId = "user123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> tokenBlacklistService.blacklistAllUserTokens(userId, EXPIRATION_MS));
    }

    @Test
    @DisplayName("Should check if all user tokens are blacklisted - returns true")
    void testAreAllUserTokensBlacklisted_ReturnsTrue() {
        // Arrange
        String userId = "user123";
        when(redisTemplate.hasKey("blacklist:token:user:" + userId)).thenReturn(true);

        // Act
        boolean result = tokenBlacklistService.areAllUserTokensBlacklisted(userId);

        // Assert
        assertTrue(result);
        verify(redisTemplate).hasKey("blacklist:token:user:" + userId);
    }

    @Test
    @DisplayName("Should check if all user tokens are blacklisted - returns false")
    void testAreAllUserTokensBlacklisted_ReturnsFalse() {
        // Arrange
        String userId = "user123";
        when(redisTemplate.hasKey("blacklist:token:user:" + userId)).thenReturn(false);

        // Act
        boolean result = tokenBlacklistService.areAllUserTokensBlacklisted(userId);

        // Assert
        assertFalse(result);
        verify(redisTemplate).hasKey("blacklist:token:user:" + userId);
    }

    @Test
    @DisplayName("Should handle check all user tokens failure gracefully")
    void testAreAllUserTokensBlacklisted_Failure() {
        // Arrange
        String userId = "user123";
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis connection failed"));

        // Act
        boolean result = tokenBlacklistService.areAllUserTokensBlacklisted(userId);

        // Assert
        assertFalse(result); // Should return false on error
    }

    @Test
    @DisplayName("Should handle null RedisTemplate gracefully in constructor")
    void testNullRedisTemplate() {
        // Arrange & Act
        TokenBlacklistService serviceWithoutRedis = new TokenBlacklistService(null);

        // Assert - operations should complete without error
        assertDoesNotThrow(() -> serviceWithoutRedis.blacklistToken(TEST_TOKEN, EXPIRATION_MS));
        assertDoesNotThrow(() -> serviceWithoutRedis.removeFromBlacklist(TEST_TOKEN));
        assertFalse(serviceWithoutRedis.isTokenBlacklisted(TEST_TOKEN));
        assertFalse(serviceWithoutRedis.areAllUserTokensBlacklisted("user123"));
    }
}
