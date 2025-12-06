package com.vetconnect.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for RedisHealthCheck
 */
@ExtendWith(MockitoExtension.class)
class RedisHealthCheckTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    private RedisHealthCheck redisHealthCheck;

    @BeforeEach
    void setUp() {
        redisHealthCheck = new RedisHealthCheck(redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void onApplicationEvent_withWorkingRedis_shouldComplete() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("OK");
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> redisHealthCheck.onApplicationEvent(applicationReadyEvent));

        // Verify Redis operations were called
        verify(valueOperations).set(anyString(), eq("OK"), eq(5L), eq(TimeUnit.SECONDS));
        verify(valueOperations).get(anyString());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void onApplicationEvent_withValueMismatch_shouldThrowException() {
        // Arrange
        when(valueOperations.get(anyString())).thenReturn("WRONG");

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> redisHealthCheck.onApplicationEvent(applicationReadyEvent)
        );

        assertTrue(exception.getMessage().contains("Redis is unavailable"));
    }

    @Test
    void onApplicationEvent_withRedisException_shouldThrowException() {
        // Arrange
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Connection refused"));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> redisHealthCheck.onApplicationEvent(applicationReadyEvent)
        );

        assertTrue(exception.getMessage().contains("Redis is unavailable"));
        assertNotNull(exception.getCause());
    }
}
