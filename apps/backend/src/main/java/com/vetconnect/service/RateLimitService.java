package com.vetconnect.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Service using Bucket4j
 *
 * PURPOSE: Prevent brute force attacks on authentication endpoints
 *
 * HOW IT WORKS:
 * - Token Bucket Algorithm: Each IP address gets a "bucket" of tokens
 * - Each request consumes 1 token
 * - Tokens refill over time (10 tokens per minute)
 * - If bucket is empty, request is rejected
 *
 * CONFIGURATION:
 * - Login: 5 attempts per minute per IP
 * - Register: 3 attempts per minute per IP
 * - Forgot Password: 2 attempts per minute per IP
 *
 * LEARNING NOTE:
 * Token Bucket is better than simple counters because:
 * - Allows bursts (all 5 tokens at once if needed)
 * - Smooth refill (not reset at fixed intervals)
 * - More user-friendly for legitimate users
 */
/**
 * DEPRECATED: Use RedisRateLimitService instead
 *
 * This in-memory implementation does NOT work with multiple server instances.
 * Kept for backwards compatibility and local development only.
 *
 * @deprecated Use {@link RedisRateLimitService} for production deployments
 */
@Service
@Slf4j
@Deprecated
public class RateLimitService {

    // Store buckets in memory (per IP address)
    // In production, consider Redis for distributed rate limiting
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> forgotPasswordBuckets = new ConcurrentHashMap<>();

    // Rate limit configurations
    private static final int LOGIN_LIMIT = 5;           // 5 attempts
    private static final Duration LOGIN_REFILL = Duration.ofMinutes(1);  // per minute

    private static final int REGISTER_LIMIT = 3;        // 3 attempts
    private static final Duration REGISTER_REFILL = Duration.ofMinutes(1);

    private static final int FORGOT_PASSWORD_LIMIT = 2; // 2 attempts
    private static final Duration FORGOT_PASSWORD_REFILL = Duration.ofMinutes(1);

    /**
     * Check if login request is allowed
     *
     * @param ipAddress Client IP address
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowLoginAttempt(String ipAddress) {
        Bucket bucket = loginBuckets.computeIfAbsent(ipAddress, k -> createLoginBucket());
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for login attempt from IP: {}", ipAddress);
        }

        return allowed;
    }

    /**
     * Check if registration request is allowed
     *
     * @param ipAddress Client IP address
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowRegisterAttempt(String ipAddress) {
        Bucket bucket = registerBuckets.computeIfAbsent(ipAddress, k -> createRegisterBucket());
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for registration attempt from IP: {}", ipAddress);
        }

        return allowed;
    }

    /**
     * Check if forgot password request is allowed
     *
     * @param ipAddress Client IP address
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowForgotPasswordAttempt(String ipAddress) {
        Bucket bucket = forgotPasswordBuckets.computeIfAbsent(ipAddress, k -> createForgotPasswordBucket());
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for forgot password attempt from IP: {}", ipAddress);
        }

        return allowed;
    }

    /**
     * Get remaining login attempts for an IP
     * Useful for showing "X attempts remaining" to users
     *
     * @param ipAddress Client IP address
     * @return Number of remaining attempts
     */
    public long getRemainingLoginAttempts(String ipAddress) {
        Bucket bucket = loginBuckets.get(ipAddress);
        if (bucket == null) {
            return LOGIN_LIMIT;
        }
        return bucket.getAvailableTokens();
    }

    /**
     * Create bucket for login rate limiting
     *
     * Bandwidth configuration:
     * - Capacity: 5 tokens (5 login attempts)
     * - Refill: 5 tokens every 1 minute
     * - Greedy refill: All tokens refilled at once after duration
     */
    private Bucket createLoginBucket() {
        Bandwidth limit = Bandwidth.classic(LOGIN_LIMIT, Refill.greedy(LOGIN_LIMIT, LOGIN_REFILL));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create bucket for registration rate limiting
     *
     * More restrictive than login (3 vs 5 attempts)
     * Registration is more expensive (password hashing, DB writes)
     */
    private Bucket createRegisterBucket() {
        Bandwidth limit = Bandwidth.classic(REGISTER_LIMIT, Refill.greedy(REGISTER_LIMIT, REGISTER_REFILL));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create bucket for forgot password rate limiting
     *
     * Most restrictive (2 attempts per minute)
     * Prevents password reset spam attacks
     */
    private Bucket createForgotPasswordBucket() {
        Bandwidth limit = Bandwidth.classic(FORGOT_PASSWORD_LIMIT, Refill.greedy(FORGOT_PASSWORD_LIMIT, FORGOT_PASSWORD_REFILL));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Clear all rate limit buckets (useful for testing)
     * In production, you might want to clear old buckets periodically
     */
    public void clearAllBuckets() {
        loginBuckets.clear();
        registerBuckets.clear();
        forgotPasswordBuckets.clear();
        log.info("All rate limit buckets cleared");
    }

    /**
     * Remove rate limit for specific IP (useful for admin/testing)
     *
     * @param ipAddress IP address to remove
     */
    public void removeLimitForIp(String ipAddress) {
        loginBuckets.remove(ipAddress);
        registerBuckets.remove(ipAddress);
        forgotPasswordBuckets.remove(ipAddress);
        log.info("Rate limits removed for IP: {}", ipAddress);
    }
}