package com.vetconnect.dto.auth;

import com.vetconnect.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO after successful authentication
 * Contains JWT token and user information
 *
 * Response example:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIs...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 86400,
 *   "user": { ...user details... }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /**
     * JWT access token
     * Use this in Authorization header: "Bearer {token}"
     */
    private String token;

    /**
     * JWT refresh token
     * Use this to get a new access token when it expires
     */
    private String refreshToken;

    /**
     * Token type (always "Bearer" for JWT)
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token expiration time in seconds
     * Default: 86400 seconds = 24 hours
     */
    private Long expiresIn;

    /**
     * User information
     */
    private UserDTO user;
}