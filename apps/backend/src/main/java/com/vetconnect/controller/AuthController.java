package com.vetconnect.controller;

import com.vetconnect.dto.auth.AuthResponse;
import com.vetconnect.dto.auth.LoginRequest;
import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.dto.common.ApiResponse;
import com.vetconnect.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * ENDPOINTS:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Login user
 * - POST /api/auth/refresh - Refresh access token
 * - POST /api/auth/logout - Logout user
 *
 * ALL ENDPOINTS ARE PUBLIC (no authentication required)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user
     *
     * POST /api/auth/register
     *
     * Request body:
     * {
     *   "email": "veteran@example.com",
     *   "password": "SecurePassword123!",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "branchOfService": "ARMY",
     *   "addressLine1": "123 Main St",
     *   "city": "Ashburn",
     *   "state": "VA",
     *   "zipCode": "20147",
     *   "isHomeless": false
     * }
     *
     * Response: 201 Created
     * {
     *   "success": true,
     *   "message": "User registered successfully",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzUxMiJ9...",
     *     "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     *     "tokenType": "Bearer",
     *     "expiresIn": 86400,
     *     "user": { ...user details... }
     *   }
     * }
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        log.info("Registration request for email: {}", registerRequest.getEmail());

        AuthResponse authResponse = authService.register(registerRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    /**
     * Login user
     *
     * POST /api/auth/login
     *
     * Request body:
     * {
     *   "email": "veteran@example.com",
     *   "password": "SecurePassword123!"
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Login successful",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzUxMiJ9...",
     *     "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     *     "tokenType": "Bearer",
     *     "expiresIn": 86400,
     *     "user": { ...user details... }
     *   }
     * }
     */
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        log.info("Login request for email: {}", loginRequest.getEmail());

        AuthResponse authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse)
        );
    }

    /**
     * Refresh access token
     *
     * POST /api/auth/refresh
     *
     * Request body:
     * {
     *   "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Token refreshed successfully",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzUxMiJ9...",
     *     "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
     *     "tokenType": "Bearer",
     *     "expiresIn": 86400,
     *     "user": { ...user details... }
     *   }
     * }
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestBody RefreshTokenRequest refreshTokenRequest) {

        log.info("Token refresh request");

        AuthResponse authResponse = authService.refreshToken(refreshTokenRequest.refreshToken());
        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", authResponse)
        );
    }

    /**
     * Logout user
     *
     * POST /api/auth/logout
     *
     * Note: Since we're using JWT, logout is primarily client-side
     * Client should delete the tokens
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Logged out successfully"
     * }
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout user (client-side token deletion)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Logout request");

        authService.logout();

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully")
        );
    }

    // ========== HELPER DTO ==========

    /**
     * Simple DTO for refresh token request
     */
    record RefreshTokenRequest(String refreshToken) {}
}