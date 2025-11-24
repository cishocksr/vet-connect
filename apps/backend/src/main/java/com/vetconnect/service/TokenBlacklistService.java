package com.vetconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Token Blacklist Service
 *
 * Purpose: Manage blacklisted JWT tokens in Redis
 * Security: Enables server-side token revocation for logout and security events
 *
 * LEARNING NOTES:
 * - Redis is an in-memory database (very fast!)
 * - We store blacklisted tokens with expiration matching JWT expiration
 * - When a token is checked, if it exists in Redis, it's invalid
 * - Tokens automatically removed from Redis after expiration (saves memory)
 * 
 * FALLBACK MODE:
 * - When Redis is not available (tests), this service operates in no-op mode
 * - All blacklist operations are skipped, tokens are never blacklisted
 * - This is acceptable for testing but NOT for production
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    public TokenBlacklistService(@Autowired(required = false) RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        if (redisTemplate == null) {
            log.warn("⚠️  TokenBlacklistService initialized without Redis - token blacklisting is DISABLED. " +
                    "This should only occur in test environments.");
        }
    }

    /**
     * Add a token to the blacklist
     *
     * @param token JWT token to blacklist
     * @param expirationMs Time until token naturally expires (in milliseconds)
     */
    public void blacklistToken(String token, long expirationMs) {
        if (redisTemplate == null) {
            log.debug("Token blacklist skipped (Redis not available)");
            return;
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            // Store token with TTL matching its natural expiration
            // After expiration, Redis automatically removes it (memory efficient!)
            redisTemplate.opsForValue().set(
                    key,
                    "blacklisted",
                    Duration.ofMillis(expirationMs)
            );
            log.info("Token blacklisted successfully");
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
            // Don't throw exception - log it but continue
            // This prevents Redis connection issues from breaking authentication
        }
    }

    /**
     * Check if a token is blacklisted
     *
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isTokenBlacklisted(String token) {
        if (redisTemplate == null) {
            return false; // No Redis, no blacklisting
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check token blacklist: {}", e.getMessage());
            // On Redis failure, assume token is NOT blacklisted
            // This prevents Redis outages from locking out all users
            return false;
        }
    }

    /**
     * Remove a token from blacklist (rarely needed, but useful for testing)
     *
     * @param token JWT token to remove from blacklist
     */
    public void removeFromBlacklist(String token) {
        if (redisTemplate == null) {
            return;
        }
        try {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.delete(key);
            log.info("Token removed from blacklist");
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist: {}", e.getMessage());
        }
    }

    /**
     * Blacklist all tokens for a specific user
     * Useful when:
     * - User changes password
     * - User account is compromised
     * - Admin suspends user
     *
     * @param userId User ID
     * @param expirationMs Expiration time in milliseconds
     */
    public void blacklistAllUserTokens(String userId, long expirationMs) {
        if (redisTemplate == null) {
            log.debug("User token blacklist skipped (Redis not available): {}", userId);
            return;
        }
        try {
            String key = BLACKLIST_PREFIX + "user:" + userId;
            redisTemplate.opsForValue().set(
                    key,
                    "all_tokens_blacklisted",
                    Duration.ofMillis(expirationMs)
            );
            log.info("All tokens blacklisted for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to blacklist all user tokens: {}", e.getMessage());
        }
    }

    /**
     * Check if all tokens for a user are blacklisted
     *
     * @param userId User ID to check
     * @return true if all user tokens are blacklisted
     */
    public boolean areAllUserTokensBlacklisted(String userId) {
        if (redisTemplate == null) {
            return false; // No Redis, no blacklisting
        }
        try {
            String key = BLACKLIST_PREFIX + "user:" + userId;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check user token blacklist: {}", e.getMessage());
            return false;
        }
    }
}