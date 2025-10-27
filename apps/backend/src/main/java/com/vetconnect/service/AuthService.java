package com.vetconnect.service;

import com.vetconnect.dto.auth.AuthResponse;
import com.vetconnect.dto.auth.LoginRequest;
import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.dto.user.UserDTO;
import com.vetconnect.exception.EmailAlreadyExistsException;
import com.vetconnect.mapper.UserMapper;
import com.vetconnect.model.User;
import com.vetconnect.repository.UserRepository;
import com.vetconnect.security.JwtTokenProvider;
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

/**
 * Service for authentication operations
 *
 * RESPONSIBILITIES:
 * - User registration
 * - User login
 * - Token generation
 * - Token refresh
 * - Password validation
 *
 * SECURITY FLOW:
 * 1. Register: Hash password → Save user → Generate tokens
 * 2. Login: Verify credentials → Generate tokens
 * 3. Refresh: Validate refresh token → Generate new access token
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

    /**
     * Register new user
     *
     * PROCESS:
     * 1. Validate email doesn't exist
     * 2. Hash password
     * 3. Create user entity
     * 4. Save to database
     * 5. Generate JWT tokens
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
            throw new EmailAlreadyExistsException(registerRequest.getEmail());  // FIXED
        }

        // 2. Hash password
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // 3. Create user entity
        User user = User.builder()
                .email(registerRequest.getEmail())
                .passwordHash(hashedPassword)
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .branchOfService(registerRequest.getBranchOfService())
                .addressLine1(registerRequest.getAddressLine1())
                .addressLine2(registerRequest.getAddressLine2())
                .city(registerRequest.getCity())
                .state(registerRequest.getState())
                .zipCode(registerRequest.getZipCode())
                .isHomeless(registerRequest.isHomeless())
                .build();

        // 4. Save user
        User savedUser = userRepository.save(user);
        log.info("Successfully registered user: {}", savedUser.getEmail());

        // 5. Generate tokens
        String accessToken = tokenProvider.generateTokenFromUserId(
                savedUser.getId(),
                savedUser.getEmail()
        );
        String refreshToken = tokenProvider.generateRefreshToken(
                savedUser.getId(),
                savedUser.getEmail()
        );

        // 6. Build response
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
     * 3. Generate JWT tokens
     * 4. Return auth response
     *
     * @param loginRequest Login credentials
     * @return AuthResponse with tokens and user info
     * @throws BadCredentialsException if credentials invalid
     */
    @Transactional(readOnly = true)
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

            // 4. Generate tokens
            String accessToken = tokenProvider.generateTokenFromUserId(
                    user.getId(),
                    user.getEmail()
            );
            String refreshToken = tokenProvider.generateRefreshToken(
                    user.getId(),
                    user.getEmail()
            );

            log.info("User successfully logged in: {}", user.getEmail());

            // 5. Build response
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
     * Refresh access token
     *
     * PROCESS:
     * 1. Validate refresh token
     * 2. Extract user ID from token
     * 3. Verify user still exists
     * 4. Generate new access token
     * 5. Return new tokens
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

        // 2. Extract user ID
        java.util.UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
        String email = tokenProvider.getEmailFromToken(refreshToken);

        // 3. Verify user still exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Generate new tokens
        String newAccessToken = tokenProvider.generateTokenFromUserId(
                user.getId(),
                user.getEmail()
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
    public java.util.UUID getUserIdFromToken(String token) {
        return tokenProvider.getUserIdFromToken(token);
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

    /**
     * Logout (invalidate token)
     *
     * NOTE: JWT tokens can't be invalidated server-side unless you maintain a blacklist
     * For now, client should just delete the token
     *
     * FUTURE ENHANCEMENT:
     * - Maintain token blacklist in Redis
     * - Add token version to user entity
     * - Invalidate all tokens when password changed
     */
    public void logout() {
        SecurityContextHolder.clearContext();
        log.debug("User logged out (context cleared)");

        // TODO: Implement token blacklist if needed
    }
}