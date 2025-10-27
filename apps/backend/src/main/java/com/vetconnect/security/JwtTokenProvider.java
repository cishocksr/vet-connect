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
 * - iat: Issued at timestamp
 * - exp: Expiration timestamp
 *
 * SECURITY NOTES:
 * - Uses HS512 algorithm
 * - Secret key must be at least 256 bits (32 characters)
 * - Tokens are signed, not encrypted (don't put sensitive data!)
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
        return generateTokenFromUserId(userDetails.getId(), userDetails.getUsername());
    }

    /**
     * Generate JWT access token from user ID and email
     *
     * USE CASE: After registration, login
     *
     * @param userId User's UUID
     * @param email User's email
     * @return JWT token string
     */
    public String generateTokenFromUserId(UUID userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .subject(userId.toString())  // User ID
                .claim("email", email)       // User email
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
     * Validate JWT token
     *
     * Checks:
     * - Signature is valid
     * - Token is not expired
     * - Token format is correct
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
     * @param token JWT token string
     * @return Milliseconds until expiration
     */
    public long getTimeUntilExpiration(String token) {
        Date expiration = getExpirationFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }
}