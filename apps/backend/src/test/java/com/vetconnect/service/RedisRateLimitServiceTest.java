package com.vetconnect.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisRateLimitService
 *
 * Tests rate limiting functionality including:
 * - Accurate enforcement of rate limits for login attempts
 * - Correctly clearing rate limits for a given IP address
 * - Rate limit calculations and Redis operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisRateLimitService Tests")
class RedisRateLimitServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisRateLimitService rateLimitService;

    private static final String TEST_IP = "192.168.1.100";
    private static final String LOGIN_KEY = "ratelimit:login:" + TEST_IP;
    private static final String REGISTER_KEY = "ratelimit:register:" + TEST_IP;
    private static final String FORGOT_PASSWORD_KEY = "ratelimit:forgot:" + TEST_IP;
    private static final int LOGIN_LIMIT = 5;

    @BeforeEach
    void setUp() {
        // Setup ValueOperations mock
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should allow login attempt when under rate limit")
    void testAllowLoginAttempt_UnderLimit() {
        // Arrange - First attempt (counter = 1)
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(1L);
        when(redisTemplate.expire(eq(LOGIN_KEY), any(Duration.class))).thenReturn(true);

        // Act
        boolean allowed = rateLimitService.allowLoginAttempt(TEST_IP);

        // Assert
        assertTrue(allowed, "First login attempt should be allowed");
        verify(valueOperations).increment(LOGIN_KEY);
        verify(redisTemplate).expire(eq(LOGIN_KEY), any(Duration.class));
    }

    @Test
    @DisplayName("Should allow login attempts up to the limit")
    void testAllowLoginAttempt_AtLimit() {
        // Arrange - Fifth attempt (at limit)
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(5L);

        // Act
        boolean allowed = rateLimitService.allowLoginAttempt(TEST_IP);

        // Assert
        assertTrue(allowed, "Login attempt at limit (5/5) should be allowed");
        verify(valueOperations).increment(LOGIN_KEY);
    }

    @Test
    @DisplayName("Should deny login attempt when rate limit exceeded")
    void testAllowLoginAttempt_ExceedsLimit() {
        // Arrange - Sixth attempt (exceeds limit)
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(6L);

        // Act
        boolean allowed = rateLimitService.allowLoginAttempt(TEST_IP);

        // Assert
        assertFalse(allowed, "Login attempt exceeding limit (6/5) should be denied");
        verify(valueOperations).increment(LOGIN_KEY);
    }

    @Test
    @DisplayName("Should accurately enforce rate limit across multiple attempts")
    void testAllowLoginAttempt_AccurateEnforcement() {
        // Test multiple sequential attempts to verify accurate counting
        
        // Attempts 1-5: Should be allowed
        for (int i = 1; i <= LOGIN_LIMIT; i++) {
            when(valueOperations.increment(LOGIN_KEY)).thenReturn((long) i);
            boolean allowed = rateLimitService.allowLoginAttempt(TEST_IP);
            assertTrue(allowed, "Attempt " + i + " should be allowed");
        }

        // Attempts 6-8: Should be denied
        for (int i = LOGIN_LIMIT + 1; i <= LOGIN_LIMIT + 3; i++) {
            when(valueOperations.increment(LOGIN_KEY)).thenReturn((long) i);
            boolean allowed = rateLimitService.allowLoginAttempt(TEST_IP);
            assertFalse(allowed, "Attempt " + i + " should be denied");
        }

        // Verify increment was called correct number of times
        verify(valueOperations, times(LOGIN_LIMIT + 3)).increment(LOGIN_KEY);
    }

    @Test
    @DisplayName("Should set TTL on first login attempt")
    void testAllowLoginAttempt_SetsTTLOnFirstAttempt() {
        // Arrange - First attempt
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(1L);
        when(redisTemplate.expire(eq(LOGIN_KEY), any(Duration.class))).thenReturn(true);

        // Act
        rateLimitService.allowLoginAttempt(TEST_IP);

        // Assert
        verify(redisTemplate).expire(eq(LOGIN_KEY), any(Duration.class));
    }

    @Test
    @DisplayName("Should not set TTL on subsequent login attempts")
    void testAllowLoginAttempt_DoesNotSetTTLOnSubsequentAttempts() {
        // Arrange - Third attempt (not first)
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(3L);

        // Act
        rateLimitService.allowLoginAttempt(TEST_IP);

        // Assert - expire should NOT be called since it's not the first attempt
        verify(redisTemplate, never()).expire(eq(LOGIN_KEY), any(Duration.class));
    }

    @Test
    @DisplayName("Should correctly clear all rate limits for given IP address")
    void testClearLimitForIp_ClearsAllLimits() {
        // Reset to avoid unnecessary stubbing from setUp
        reset(redisTemplate);

        // Act
        rateLimitService.clearLimitForIp(TEST_IP);

        // Assert - All three keys should be deleted
        verify(redisTemplate).delete(LOGIN_KEY);
        verify(redisTemplate).delete(REGISTER_KEY);
        verify(redisTemplate).delete(FORGOT_PASSWORD_KEY);
    }

    @Test
    @DisplayName("Should allow login after clearing rate limit for IP")
    void testClearLimitForIp_AllowsNewAttempts() {
        // Arrange - First exceed the limit
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(6L);
        boolean deniedBeforeClear = rateLimitService.allowLoginAttempt(TEST_IP);
        assertFalse(deniedBeforeClear, "Should be denied before clearing");

        // Clear the limits
        rateLimitService.clearLimitForIp(TEST_IP);

        // After clearing, simulate first attempt again
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(1L);
        when(redisTemplate.expire(eq(LOGIN_KEY), any(Duration.class))).thenReturn(true);

        // Act
        boolean allowedAfterClear = rateLimitService.allowLoginAttempt(TEST_IP);

        // Assert
        assertTrue(allowedAfterClear, "Should be allowed after clearing rate limit");
    }

    @Test
    @DisplayName("Should get correct remaining login attempts")
    void testGetRemainingLoginAttempts() {
        // Arrange - 3 attempts made, 2 remaining
        when(valueOperations.get(LOGIN_KEY)).thenReturn("3");

        // Act
        long remaining = rateLimitService.getRemainingLoginAttempts(TEST_IP);

        // Assert
        assertEquals(2L, remaining, "Should have 2 remaining attempts (5-3)");
        verify(valueOperations).get(LOGIN_KEY);
    }

    @Test
    @DisplayName("Should return full limit when no attempts made")
    void testGetRemainingLoginAttempts_NoAttempts() {
        // Arrange - No attempts yet (key doesn't exist)
        when(valueOperations.get(LOGIN_KEY)).thenReturn(null);

        // Act
        long remaining = rateLimitService.getRemainingLoginAttempts(TEST_IP);

        // Assert
        assertEquals(LOGIN_LIMIT, remaining, "Should have full limit (5) when no attempts made");
    }

    @Test
    @DisplayName("Should return zero when rate limit exceeded")
    void testGetRemainingLoginAttempts_LimitExceeded() {
        // Arrange - 6 attempts made (exceeded limit)
        when(valueOperations.get(LOGIN_KEY)).thenReturn("6");

        // Act
        long remaining = rateLimitService.getRemainingLoginAttempts(TEST_IP);

        // Assert
        assertEquals(0L, remaining, "Should return 0 when limit exceeded, not negative");
    }

    @Test
    @DisplayName("Should fail open when Redis returns null on increment")
    void testAllowLoginAttempt_FailsOpenOnNullIncrement() {
        // Arrange - Simulate Redis error
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(null);

        // Act
        boolean allowed = rateLimitService.allowLoginAttempt(TEST_IP);

        // Assert - Should fail open (allow request) when Redis fails
        assertTrue(allowed, "Should fail open and allow request when Redis returns null");
    }

    @Test
    @DisplayName("Should fail open when Redis throws exception")
    void testAllowLoginAttempt_FailsOpenOnException() {
        // Arrange - Simulate Redis connection error
        when(valueOperations.increment(LOGIN_KEY)).thenThrow(new RuntimeException("Redis connection failed"));

        // Act
        boolean allowed = rateLimitService.allowLoginAttempt(TEST_IP);

        // Assert - Should fail open (allow request) when Redis fails
        assertTrue(allowed, "Should fail open and allow request when Redis throws exception");
    }

    @Test
    @DisplayName("Should get time until reset from Redis TTL")
    void testGetTimeUntilReset() {
        // Arrange - 45 seconds remaining
        reset(redisTemplate);  // Reset to avoid unnecessary stubbing from setUp
        when(redisTemplate.getExpire(LOGIN_KEY)).thenReturn(45L);

        // Act
        long timeUntilReset = rateLimitService.getTimeUntilReset(TEST_IP);

        // Assert
        assertEquals(45L, timeUntilReset, "Should return TTL from Redis");
        verify(redisTemplate).getExpire(LOGIN_KEY);
    }

    @Test
    @DisplayName("Should return zero when no rate limit is active")
    void testGetTimeUntilReset_NoActiveLimit() {
        // Arrange - No TTL (key doesn't exist or expired)
        reset(redisTemplate);  // Reset to avoid unnecessary stubbing from setUp
        when(redisTemplate.getExpire(LOGIN_KEY)).thenReturn(-2L);

        // Act
        long timeUntilReset = rateLimitService.getTimeUntilReset(TEST_IP);

        // Assert
        assertEquals(0L, timeUntilReset, "Should return 0 when no active rate limit");
    }

    @Test
    @DisplayName("Should enforce different limits for different operations")
    void testDifferentOperations_DifferentLimits() {
        // Arrange
        when(valueOperations.increment(LOGIN_KEY)).thenReturn(1L);
        when(valueOperations.increment(REGISTER_KEY)).thenReturn(1L);
        when(valueOperations.increment(FORGOT_PASSWORD_KEY)).thenReturn(1L);
        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);

        // Act
        boolean loginAllowed = rateLimitService.allowLoginAttempt(TEST_IP);
        boolean registerAllowed = rateLimitService.allowRegisterAttempt(TEST_IP);
        boolean forgotPasswordAllowed = rateLimitService.allowForgotPasswordAttempt(TEST_IP);

        // Assert - All should be allowed (different keys)
        assertTrue(loginAllowed, "Login should be allowed");
        assertTrue(registerAllowed, "Register should be allowed");
        assertTrue(forgotPasswordAllowed, "Forgot password should be allowed");

        // Verify different keys were used
        verify(valueOperations).increment(LOGIN_KEY);
        verify(valueOperations).increment(REGISTER_KEY);
        verify(valueOperations).increment(FORGOT_PASSWORD_KEY);
    }
}
