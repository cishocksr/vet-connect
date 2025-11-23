package com.vetconnect.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis Health Check
 *
 * Verifies Redis connection at application startup
 * Fails fast if Redis is unavailable
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthCheck implements ApplicationListener<ApplicationReadyEvent> {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("🔍 Checking Redis connection...");

        try {
            // Attempt to set and get a test value
            String testKey = "health-check:" + System.currentTimeMillis();
            String testValue = "OK";

            redisTemplate.opsForValue().set(testKey, testValue, 5, TimeUnit.SECONDS);
            String retrievedValue = redisTemplate.opsForValue().get(testKey);

            if (!testValue.equals(retrievedValue)) {
                throw new IllegalStateException(
                        "Redis health check failed: value mismatch"
                );
            }

            // Clean up test key
            redisTemplate.delete(testKey);

            log.info("✅ Redis connection verified successfully");

        } catch (Exception e) {
            log.error("❌ Redis connection failed: {}", e.getMessage());
            throw new IllegalStateException(
                    "Cannot start application - Redis is unavailable. " +
                            "Please ensure Redis is running and accessible.",
                    e
            );
        }
    }
}