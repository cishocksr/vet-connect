package com.vetconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-based Rate Limiting Service
 *
 * PURPOSE: Prevent brute force attacks using distributed rate limiting
 *
 * WHY REDIS:
 * - Works across multiple server instances (horizontal scaling)
 * - Automatic expiration (TTL)
 * - Atomic operations (thread-safe)
 * - Much faster than database
 *
 * RATE LIMITS:
 * - Login: 5 attempts per minute per IP
 * - Register: 3 attempts per minute per IP
 * - Forgot Password: 2 attempts per minute per IP
 *
 * IMPLEMENTATION:
 * Uses simple Redis counter with TTL
 * More efficient than Bucket4j for most use cases
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    // Rate limit constants
    private static final int LOGIN_LIMIT = 5;
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(1);

    private static final int REGISTER_LIMIT = 3;
    private static final Duration REGISTER_WINDOW = Duration.ofMinutes(1);

    private static final int FORGOT_PASSWORD_LIMIT = 2;
    private static final Duration FORGOT_PASSWORD_WINDOW = Duration.ofMinutes(1);

    // Redis key prefixes
    private static final String LOGIN_PREFIX = "ratelimit:login:";
    private static final String REGISTER_PREFIX = "ratelimit:register:";
    private static final String FORGOT_PASSWORD_PREFIX = "ratelimit:forgot:";

    /**
     * Check if login attempt is allowed
     *
     * ALGORITHM:
     * 1. Increment counter for this IP
     * 2. If first attempt, set TTL
     * 3. Check if counter exceeds limit
     *
     * @param ipAddress Client IP address
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean allowLoginAttempt(String ipAddress) {
        return isAllowed(LOGIN_PREFIX + ipAddress, LOGIN_LIMIT, LOGIN_WINDOW, "login");
    }

    /**
     * Check if registration attempt is allowed
     *
     * @param ipAddress Client IP address
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean allowRegisterAttempt(String ipAddress) {
        return isAllowed(REGISTER_PREFIX + ipAddress, REGISTER_LIMIT, REGISTER_WINDOW, "registration");
    }

    /**
     * Check if forgot password attempt is allowed
     *
     * @param ipAddress Client IP address
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean allowForgotPasswordAttempt(String ipAddress) {
        return isAllowed(FORGOT_PASSWORD_PREFIX + ipAddress, FORGOT_PASSWORD_LIMIT, FORGOT_PASSWORD_WINDOW, "forgot password");
    }

    /**
     * Get remaining login attempts for an IP
     *
     * @param ipAddress Client IP address
     * @return Number of remaining attempts (0 if limit reached)
     */
    public long getRemainingLoginAttempts(String ipAddress) {
        return getRemainingAttempts(LOGIN_PREFIX + ipAddress, LOGIN_LIMIT);
    }

    /**
     * Clear rate limit for specific IP (admin/testing use)
     *
     * @param ipAddress IP address to clear
     */
    public void clearLimitForIp(String ipAddress) {
        redisTemplate.delete(LOGIN_PREFIX + ipAddress);
        redisTemplate.delete(REGISTER_PREFIX + ipAddress);
        redisTemplate.delete(FORGOT_PASSWORD_PREFIX + ipAddress);
        log.info("Rate limits cleared for IP: {}", ipAddress);
    }

    /**
     * Generic rate limiting check
     *
     * REDIS OPERATIONS:
     * 1. INCR key - Increment counter atomically
     * 2. EXPIRE key ttl - Set expiration if first request
     * 3. Return current count
     *
     * @param key Redis key for this rate limit
     * @param limit Maximum attempts allowed
     * @param window Time window for limit
     * @param operation Operation name for logging
     * @return true if allowed, false if limit exceeded
     */
    private boolean isAllowed(String key, int limit, Duration window, String operation) {
        try {
            // Increment counter (atomic operation)
            Long attempts = redisTemplate.opsForValue().increment(key);

            if (attempts == null) {
                log.error("Redis increment returned null for key: {}", key);
                return true; // Fail open - allow request if Redis fails
            }

            // If this is the first attempt, set TTL
            if (attempts == 1) {
                redisTemplate.expire(key, window);
            }

            // Check if limit exceeded
            boolean allowed = attempts <= limit;

            if (!allowed) {
                log.warn("Rate limit exceeded for {} from key: {} (attempts: {}/{})",
                        operation, key, attempts, limit);
            } else {
                log.debug("Rate limit check for {}: {}/{} attempts",
                        operation, attempts, limit);
            }

            return allowed;

        } catch (Exception e) {
            log.error("Redis error during rate limit check: {}", e.getMessage());
            // Fail open - allow request if Redis is down
            // This prevents Redis outages from blocking all users
            return true;
        }
    }

    /**
     * Get remaining attempts for a key
     *
     * @param key Redis key
     * @param limit Maximum limit
     * @return Remaining attempts (0 if limit reached)
     */
    private long getRemainingAttempts(String key, int limit) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return limit; // No attempts yet
            }

            long attempts = Long.parseLong(value);
            long remaining = limit - attempts;
            return Math.max(0, remaining); // Don't return negative

        } catch (Exception e) {
            log.error("Redis error getting remaining attempts: {}", e.getMessage());
            return limit; // Fail open
        }
    }

    /**
     * Get time until rate limit resets
     *
     * @param ipAddress IP address
     * @return Seconds until reset (0 if no limit active)
     */
    public long getTimeUntilReset(String ipAddress) {
        try {
            String key = LOGIN_PREFIX + ipAddress;
            Long ttl = redisTemplate.getExpire(key);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            log.error("Redis error getting TTL: {}", e.getMessage());
            return 0;
        }
    }
}