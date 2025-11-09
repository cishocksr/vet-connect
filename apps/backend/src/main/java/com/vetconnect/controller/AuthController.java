package com.vetconnect.controller;

import com.vetconnect.dto.auth.AuthResponse;
import com.vetconnect.dto.auth.LoginRequest;
import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.dto.common.ApiResponse;
import com.vetconnect.exception.RateLimitException;
import com.vetconnect.service.AuthService;
import com.vetconnect.service.RateLimitService;
import com.vetconnect.service.RedisRateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
 * - POST /api/auth/logout - Logout user (invalidate token)
 * - POST /api/auth/refresh - Refresh access token
 *
 * SECURITY FEATURES:
 * - Rate limiting on all endpoints
 * - JWT token blacklist on logout
 * - Password hashing with BCrypt
 * - Input validation
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final RedisRateLimitService rateLimitService;

    /**
     * Register a new user
     * POST /api/auth/register
     *
     * SECURITY:
     * - Rate limited to 3 attempts per minute per IP
     * - Password must meet complexity requirements
     * - Email must be unique
     *
     * @param request RegisterRequest containing user details
     * @param httpRequest HTTP request to extract IP address
     * @return AuthResponse with JWT tokens
     */
    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        // Extract client IP address for rate limiting
        String ipAddress = getClientIpAddress(httpRequest);

        // Check rate limit BEFORE processing registration
        if (!rateLimitService.allowRegisterAttempt(ipAddress)) {
            throw new RateLimitException(
                    "Too many registration attempts. Please try again in 1 minute."
            );
        }

        log.info("Registration attempt from IP: {}, email: {}", ipAddress, request.getEmail());

        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * Login user
     * POST /api/auth/login
     *
     * SECURITY:
     * - Rate limited to 5 attempts per minute per IP
     * - Prevents brute force attacks
     * - Returns JWT access token and refresh token
     *
     * @param request LoginRequest containing email and password
     * @param httpRequest HTTP request to extract IP address
     * @return AuthResponse with JWT tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // Extract client IP address for rate limiting
        String ipAddress = getClientIpAddress(httpRequest);

        // Check rate limit BEFORE processing login
        if (!rateLimitService.allowLoginAttempt(ipAddress)) {
            long remaining = rateLimitService.getRemainingLoginAttempts(ipAddress);
            throw new RateLimitException(
                    "Too many login attempts. Please try again in 1 minute. " +
                            "Remaining attempts: " + remaining
            );
        }

        log.info("Login attempt from IP: {}, email: {}", ipAddress, request.getEmail());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Logout user
     * POST /api/auth/logout
     *
     * SECURITY:
     * - Blacklists JWT token server-side
     * - Token becomes invalid immediately
     * - Prevents token reuse
     *
     * @param httpRequest HTTP request to extract JWT token
     * @return Success message
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout user and invalidate token")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        try {
            // Extract token from Authorization header
            String token = extractTokenFromRequest(httpRequest);

            if (token != null) {
                authService.logout(token);
                return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
            } else {
                // No token provided - still clear client-side
                return ResponseEntity.ok(ApiResponse.success("Logged out (no token provided)", null));
            }
        } catch (Exception e) {
            log.error("Logout error: {}", e.getMessage());
            // Even if server-side logout fails, return success
            // Client should delete token anyway
            return ResponseEntity.ok(ApiResponse.success("Logged out", null));
        }
    }

    /**
     * Refresh access token
     * POST /api/auth/refresh
     *
     * USE CASE:
     * - When access token expires (24 hours)
     * - Client sends refresh token to get new access token
     * - Refresh token valid for 7 days
     *
     * @param refreshToken Refresh token from client
     * @return New access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody String refreshToken) {

        log.debug("Token refresh attempt");

        AuthResponse response = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    /**
     * Validate token (useful for frontend)
     * GET /api/auth/validate
     *
     * Returns 200 if token is valid, 401 if invalid
     * Checked by JwtAuthenticationFilter automatically
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate Token", description = "Check if current JWT token is valid")
    public ResponseEntity<ApiResponse<Void>> validateToken() {
        // If we reach here, token is valid (passed through JwtAuthenticationFilter)
        return ResponseEntity.ok(ApiResponse.success("Token is valid", null));
    }

    /**
     * Extract JWT token from Authorization header
     *
     * Authorization header format: "Bearer <token>"
     * This method extracts the <token> part
     *
     * @param request HTTP request
     * @return JWT token string, or null if not present
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Extract client IP address from request
     * Handles proxy headers (X-Forwarded-For, X-Real-IP)
     *
     * SECURITY NOTE:
     * In production behind a proxy/load balancer, configure trusted proxies
     * to prevent IP spoofing attacks
     *
     * HOW IT WORKS:
     * 1. Check X-Forwarded-For header (set by proxies/load balancers)
     * 2. Check X-Real-IP header (alternative proxy header)
     * 3. Fall back to direct connection IP (getRemoteAddr)
     * 4. If multiple IPs in X-Forwarded-For, use the first one (original client)
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // If multiple IPs in X-Forwarded-For (client, proxy1, proxy2),
        // take the first one (original client IP)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip != null ? ip : "unknown";
    }
}