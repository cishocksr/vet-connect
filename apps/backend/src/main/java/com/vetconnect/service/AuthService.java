package com.vetconnect.service;

import com.vetconnect.dto.auth.AuthResponse;
import com.vetconnect.dto.auth.LoginRequest;
import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.dto.user.UserDTO;
import com.vetconnect.exception.EmailAlreadyExistsException;
import com.vetconnect.exception.InvalidTokenException;
import com.vetconnect.exception.ResourceNotFoundException;
import com.vetconnect.mapper.UserMapper;
import com.vetconnect.model.User;
import com.vetconnect.repository.UserRepository;
import com.vetconnect.security.JwtTokenProvider;
import com.vetconnect.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for authentication operations
 *
 * RESPONSIBILITIES:
 * - User registration
 * - User login
 * - Token generation
 * - Token refresh
 * - Token invalidation (logout)
 * - Password validation
 *
 * SECURITY FLOW:
 * 1. Register: Hash password → Save user → Generate tokens (with version)
 * 2. Login: Verify credentials → Generate tokens (with version)
 * 3. Refresh: Validate refresh token → Generate new access token (with version)
 * 4. Logout: Blacklist token → Clear context
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final InputSanitizer inputSanitizer;

    /**
     * Register new user
     *
     * PROCESS:
     * 1. Validate email doesn't exist
     * 2. Hash password
     * 3. Create user entity with token version = 1
     * 4. Save to database
     * 5. Generate JWT tokens (including token version)
     * 6. Return auth response with tokens and user info
     *
     * @param registerRequest Registration details
     * @return AuthResponse with tokens and user info
     * @throws EmailAlreadyExistsException if email already exists
     */

    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getEmail());

        // 1. Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException(registerRequest.getEmail());
        }

        // 2. Hash password
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // 3. Sanitize all user inputs to prevent XSS
        String sanitizedFirstName = inputSanitizer.sanitizeHtml(registerRequest.getFirstName());
        String sanitizedLastName = inputSanitizer.sanitizeHtml(registerRequest.getLastName());
        String sanitizedAddressLine1 = registerRequest.getAddressLine1() != null
                ? inputSanitizer.sanitizeHtml(registerRequest.getAddressLine1())
                : null;
        String sanitizedAddressLine2 = registerRequest.getAddressLine2() != null
                ? inputSanitizer.sanitizeHtml(registerRequest.getAddressLine2())
                : null;
        String sanitizedCity = registerRequest.getCity() != null
                ? inputSanitizer.sanitizeHtml(registerRequest.getCity())
                : null;
        String sanitizedZipCode = registerRequest.getZipCode() != null
                ? inputSanitizer.sanitizeHtml(registerRequest.getZipCode())
                : null;

        // 4. Create user entity with sanitized inputs and initial token version
        User user = User.builder()
                .email(registerRequest.getEmail())  // Email validation handled by @Email annotation
                .passwordHash(hashedPassword)
                .firstName(sanitizedFirstName)  // Use sanitized value
                .lastName(sanitizedLastName)    // Use sanitized value
                .branchOfService(registerRequest.getBranchOfService())
                .addressLine1(sanitizedAddressLine1)  // Use sanitized value
                .addressLine2(sanitizedAddressLine2)  // Use sanitized value
                .city(sanitizedCity)           // Use sanitized value
                .state(registerRequest.getState())  // State is validated enum/pattern
                .zipCode(sanitizedZipCode)     // Use sanitized value
                .isHomeless(registerRequest.isHomeless())
                .isDeleted(false)
                .tokenVersion(1)
                .build();

        // 5. Save user
        User savedUser = userRepository.save(user);
        log.info("Successfully registered user: {}", savedUser.getEmail());

        // Rest of method unchanged...
        String accessToken = tokenProvider.generateTokenFromUserId(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getTokenVersion()
        );
        String refreshToken = tokenProvider.generateRefreshToken(
                savedUser.getId(),
                savedUser.getEmail()
        );

        UserDTO userDTO = userMapper.toDTO(savedUser);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(calculateExpiresIn(accessToken))
                .user(userDTO)
                .build();
    }

    /**
     * Login user
     *
     * PROCESS:
     * 1. Authenticate credentials (Spring Security does this)
     * 2. Get user from database
     * 3. Update last login timestamp
     * 4. Generate JWT tokens (including token version)
     * 5. Return auth response
     *
     * @param loginRequest Login credentials
     * @return AuthResponse with tokens and user info
     * @throws BadCredentialsException if credentials invalid
     */
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("User login attempt: {}", loginRequest.getEmail());

        try {
            // 1. Authenticate credentials
            // This will throw BadCredentialsException if invalid
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // 2. Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Get user from database
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 4. Update last login timestamp
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // 5. Generate tokens WITH TOKEN VERSION
            String accessToken = tokenProvider.generateTokenFromUserId(
                    user.getId(),
                    user.getEmail(),
                    user.getTokenVersion()  // ADD TOKEN VERSION
            );
            String refreshToken = tokenProvider.generateRefreshToken(
                    user.getId(),
                    user.getEmail()
            );

            log.info("User successfully logged in: {}", user.getEmail());

            // 6. Build response
            UserDTO userDTO = userMapper.toDTO(user);

            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(calculateExpiresIn(accessToken))
                    .user(userDTO)
                    .build();

        } catch (BadCredentialsException ex) {
            log.error("Failed login attempt for: {}", loginRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
    }

    /**
     * Get current user by ID
     *
     * @param userId User ID from JWT token
     * @return UserDTO
     */
    public UserDTO getCurrentUser(UUID userId) {
        log.debug("Getting user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toDTO(user);
    }

    /**
     * Refresh access token
     *
     * PROCESS:
     * 1. Validate refresh token
     * 2. Extract user ID from token
     * 3. Verify user still exists
     * 4. Generate new access token (with current token version)
     * 5. Return new tokens
     *
     * SECURITY NOTE:
     * We use the CURRENT token version from the database, not from the refresh token.
     * This ensures that if the token version was incremented (password change, etc.),
     * the refresh token also becomes invalid.
     *
     * @param refreshToken The refresh token
     * @return AuthResponse with new tokens
     * @throws RuntimeException if refresh token invalid or expired
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Refreshing access token");

        // 1. Validate refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // 2. Extract user ID AND token version from refresh token
        UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
        String email = tokenProvider.getEmailFromToken(refreshToken);
        Integer refreshTokenVersion = tokenProvider.getTokenVersionFromToken(refreshToken);  // ADD THIS

        // 3. Verify user still exists and get CURRENT token version
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. SECURITY CHECK: Validate token version
        // If user changed password, token version was incremented
        // Old refresh tokens should not work
        if (!refreshTokenVersion.equals(user.getTokenVersion())) {
            log.warn("Refresh token version mismatch for user: {}. Token version: {}, Current version: {}",
                    userId, refreshTokenVersion, user.getTokenVersion());
            throw new InvalidTokenException("Refresh token is no longer valid. Please login again.");
        }

        // 5. Generate new tokens WITH CURRENT TOKEN VERSION FROM DATABASE
        String newAccessToken = tokenProvider.generateTokenFromUserId(
                user.getId(),
                user.getEmail(),
                user.getTokenVersion()
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(
                user.getId(),
                user.getEmail()
        );

        log.info("Successfully refreshed token for user: {}", user.getEmail());

        // 5. Build response
        UserDTO userDTO = userMapper.toDTO(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(calculateExpiresIn(newAccessToken))
                .user(userDTO)
                .build();
    }

    /**
     * Validate token
     *
     * @param token JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    /**
     * Get user ID from token
     *
     * @param token JWT token
     * @return User's UUID
     */
    public UUID getUserIdFromToken(String token) {
        return tokenProvider.getUserIdFromToken(token);
    }

    /**
     * Logout (invalidate token)
     *
     * SECURITY IMPLEMENTATION:
     * - Blacklists current token in Redis
     * - Token becomes invalid server-side immediately
     * - Redis auto-removes token after natural expiration (memory efficient)
     * - Clears Spring Security context
     *
     * @param token JWT token to invalidate
     */
    public void logout(String token) {
        try {
            // Get token expiration time
            long expirationMs = tokenProvider.getTimeUntilExpiration(token);

            // Add token to blacklist with TTL matching expiration
            tokenBlacklistService.blacklistToken(token, expirationMs);

            // Clear security context
            SecurityContextHolder.clearContext();

            log.info("User logged out successfully, token blacklisted");
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            // Still clear context even if blacklist fails
            SecurityContextHolder.clearContext();
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }

    // ========== HELPER METHODS ==========

    /**
     * Calculate expires in (seconds until token expires)
     *
     * @param token JWT token
     * @return Seconds until expiration
     */
    private Long calculateExpiresIn(String token) {
        long millisUntilExpiration = tokenProvider.getTimeUntilExpiration(token);
        return millisUntilExpiration / 1000;  // Convert to seconds
    }
}