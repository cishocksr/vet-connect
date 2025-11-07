package com.vetconnect.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider
 *
 * RESPONSIBILITIES:
 * - Generate JWT access tokens
 * - Generate JWT refresh tokens
 * - Validate tokens
 * - Extract claims from tokens
 *
 * JWT STRUCTURE:
 * Header.Payload.Signature
 *
 * Payload contains:
 * - sub: User ID (UUID)
 * - email: User email
 * - tokenVersion: Token version for bulk invalidation (SECURITY FEATURE)
 * - iat: Issued at timestamp
 * - exp: Expiration timestamp
 *
 * SECURITY NOTES:
 * - Uses HS512 algorithm
 * - Secret key must be at least 256 bits (32 characters)
 * - Tokens are signed, not encrypted (don't put sensitive data!)
 * - Token version enables instant invalidation of all user tokens
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    /**
     * Generate JWT access token from authentication
     *
     * @param authentication Spring Security authentication object
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return generateTokenFromUserId(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getTokenVersion()
        );
    }

    /**
     * Generate JWT access token from user ID, email, and token version
     *
     * USE CASE: After registration, login
     *
     * SECURITY: Token version included for bulk invalidation
     * - When user changes password, increment tokenVersion
     * - All old tokens become invalid instantly
     * - No need to blacklist each token individually
     *
     * @param userId User's UUID
     * @param email User's email
     * @param tokenVersion User's current token version
     * @return JWT token string
     */
    public String generateTokenFromUserId(UUID userId, String email, Integer tokenVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .subject(userId.toString())  // User ID
                .claim("email", email)       // User email
                .claim("tokenVersion", tokenVersion)  // Token version for security
                .issuedAt(now)              // When token was created
                .expiration(expiryDate)     // When token expires
                .signWith(key, Jwts.SIG.HS512)  // Sign with secret (UPDATED API)
                .compact();
    }

    /**
     * Generate JWT refresh token
     *
     * Refresh tokens:
     * - Have longer expiration (7 days default)
     * - Used to get new access tokens
     * - Should be stored securely on client side
     *
     * @param userId User's UUID
     * @param email User's email
     * @return JWT refresh token string
     */
    public String generateRefreshToken(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("type", "refresh")  // Mark as refresh token
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS512)  // UPDATED API
                .compact();
    }

    /**
     * Extract user ID from JWT token
     *
     * @param token JWT token string
     * @return User's UUID
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()  // UPDATED: Changed from parserBuilder()
                .verifyWith(getSigningKey())  // UPDATED API
                .build()
                .parseSignedClaims(token)  // UPDATED: Changed from parseClaimsJws
                .getPayload();  // UPDATED: Changed from getBody()

        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extract email from JWT token
     *
     * @param token JWT token string
     * @return User's email
     */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()  // UPDATED
                .verifyWith(getSigningKey())  // UPDATED
                .build()
                .parseSignedClaims(token)  // UPDATED
                .getPayload();  // UPDATED

        return claims.get("email", String.class);
    }

    /**
     * Extract token version from JWT token
     *
     * SECURITY FEATURE:
     * - Each user has a token version number in database
     * - JWT tokens include this version number
     * - During validation, we compare token version with user's current version
     * - If mismatch, token is invalid (even if not expired)
     * - This enables instant invalidation of all user tokens by incrementing version
     *
     * USE CASES:
     * - User changes password -> increment version -> all old tokens invalid
     * - Account compromised -> increment version -> force re-login
     * - Admin suspends user -> increment version -> immediate logout
     *
     * @param token JWT token string
     * @return Token version
     */
    public Integer getTokenVersionFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("tokenVersion", Integer.class);
    }

    /**
     * Validate JWT token
     *
     * Checks:
     * - Signature is valid
     * - Token is not expired
     * - Token format is correct
     *
     * NOTE: This only validates the token structure and signature.
     * Token version validation happens in JwtAuthenticationFilter.
     *
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()  // UPDATED
                    .verifyWith(getSigningKey())  // UPDATED
                    .build()
                    .parseSignedClaims(token);  // UPDATED
            return true;
        } catch (JwtException ex) {
            log.error("JWT validation error: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Check if token is expired
     *
     * @param token JWT token string
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()  // UPDATED
                    .verifyWith(getSigningKey())  // UPDATED
                    .build()
                    .parseSignedClaims(token)  // UPDATED
                    .getPayload();  // UPDATED

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        }
    }

    /**
     * Get expiration time from token
     *
     * @param token JWT token string
     * @return Expiration date
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()  // UPDATED
                .verifyWith(getSigningKey())  // UPDATED
                .build()
                .parseSignedClaims(token)  // UPDATED
                .getPayload();  // UPDATED

        return claims.getExpiration();
    }

    /**
     * Get signing key from secret
     *
     * IMPORTANT: Secret must be at least 256 bits (32 characters)
     *
     * @return SecretKey for signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Get time until token expires (in milliseconds)
     *
     * Used by token blacklist service to set TTL on blacklisted tokens
     * No need to keep blacklisted tokens in Redis after they naturally expire
     *
     * @param token JWT token string
     * @return Milliseconds until expiration
     */
    public long getTimeUntilExpiration(String token) {
        Date expiration = getExpirationFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }
}