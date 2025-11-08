package com.vetconnect.service;

import com.vetconnect.dto.auth.AuthResponse;
import com.vetconnect.dto.auth.LoginRequest;
import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.dto.user.UserDTO;
import com.vetconnect.exception.EmailAlreadyExistsException;
import com.vetconnect.exception.InvalidCredentialsException;
import com.vetconnect.mapper.UserMapper;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.repository.UserRepository;
import com.vetconnect.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 *
 * Tests authentication business logic including:
 * - User registration
 * - User login
 * - Token generation
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isHomeless(false)
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isHomeless(false)
                .build();

        userDTO = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .branchOfService(user.getBranchOfService())
                .build();
    }

    @Test
    @DisplayName("Should successfully register new user")
    void testRegister_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateTokenFromUserId(any(UUID.class), anyString(), anyInt()))
                .thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(UUID.class), anyString()))
                .thenReturn("refresh-token");
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());

        // Verify interactions
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).save(any(User.class));
        verify(tokenProvider).generateTokenFromUserId(any(UUID.class), eq("test@example.com"), anyInt());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.register(registerRequest));

        // Verify that save was never called
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login user with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenProvider.generateTokenFromUserId(any(UUID.class), anyString(), anyInt()))
                .thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(UUID.class), anyString()))
                .thenReturn("refresh-token");
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertNotNull(response.getUser());

        // Verify
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("Password123!", "hashedPassword");
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));

        // Verify password was never checked
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(loginRequest));

        // Verify token was never generated
        verify(tokenProvider, never()).generateTokenFromUserId(any(), anyString(), anyInt());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshToken_Success() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(tokenProvider.getEmailFromToken(refreshToken)).thenReturn(email);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenProvider.generateTokenFromUserId(userId, email, anyInt())).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(userId, email)).thenReturn("new-refresh-token");
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        AuthResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }
}