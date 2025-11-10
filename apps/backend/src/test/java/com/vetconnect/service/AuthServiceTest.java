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
import com.vetconnect.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private InputSanitizer inputSanitizer;

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
        when(inputSanitizer.sanitizeHtml(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateTokenFromUserId(any(UUID.class), anyString(), anyInt()))
                .thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(UUID.class), anyString()))
                .thenReturn("refresh-token");
        when(tokenProvider.getTimeUntilExpiration(anyString())).thenReturn(3600000L);
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
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
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
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found during login")
    void testLogin_UserNotFound() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));

        // Verify authentication was attempted
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }


    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshToken_Success() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Integer tokenVersion = 0;

        // Set token version on user
        user.setTokenVersion(tokenVersion);

        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(refreshToken)).thenReturn(userId);
        when(tokenProvider.getEmailFromToken(refreshToken)).thenReturn(email);
        when(tokenProvider.getTokenVersionFromToken(refreshToken)).thenReturn(tokenVersion);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenProvider.generateTokenFromUserId(any(UUID.class), anyString(), anyInt())).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(any(UUID.class), anyString())).thenReturn("new-refresh-token");
        when(tokenProvider.getTimeUntilExpiration(anyString())).thenReturn(3600000L);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        AuthResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    @DisplayName("Should sanitize user input fields before user creation")
    void testRegister_SanitizesInputFields() {
        // Arrange - Create request with potentially malicious inputs
        RegisterRequest maliciousRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .firstName("<script>alert('xss')</script>John")
                .lastName("Doe<img src=x onerror=alert('xss')>")
                .branchOfService(BranchOfService.ARMY)
                .addressLine1("123 Main St<script>evil()</script>")
                .addressLine2("Apt 4<b>bold</b>")
                .city("Ashburn<script>")
                .zipCode("20147<script>")
                .state("VA")
                .isHomeless(false)
                .build();

        // Mock sanitizer to return sanitized values
        when(inputSanitizer.sanitizeHtml("<script>alert('xss')</script>John"))
                .thenReturn("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;John");
        when(inputSanitizer.sanitizeHtml("Doe<img src=x onerror=alert('xss')>"))
                .thenReturn("Doe&lt;img src=x onerror=alert(&#39;xss&#39;)&gt;");
        when(inputSanitizer.sanitizeHtml("123 Main St<script>evil()</script>"))
                .thenReturn("123 Main St&lt;script&gt;evil()&lt;/script&gt;");
        when(inputSanitizer.sanitizeHtml("Apt 4<b>bold</b>"))
                .thenReturn("Apt 4&lt;b&gt;bold&lt;/b&gt;");
        when(inputSanitizer.sanitizeHtml("Ashburn<script>"))
                .thenReturn("Ashburn&lt;script&gt;");
        when(inputSanitizer.sanitizeHtml("20147<script>"))
                .thenReturn("20147&lt;script&gt;");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateTokenFromUserId(any(UUID.class), anyString(), anyInt()))
                .thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(UUID.class), anyString()))
                .thenReturn("refresh-token");
        when(tokenProvider.getTimeUntilExpiration(anyString())).thenReturn(3600000L);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        AuthResponse response = authService.register(maliciousRequest);

        // Assert
        assertNotNull(response);

        // Verify that sanitizeHtml was called for each text field
        verify(inputSanitizer).sanitizeHtml("<script>alert('xss')</script>John");
        verify(inputSanitizer).sanitizeHtml("Doe<img src=x onerror=alert('xss')>");
        verify(inputSanitizer).sanitizeHtml("123 Main St<script>evil()</script>");
        verify(inputSanitizer).sanitizeHtml("Apt 4<b>bold</b>");
        verify(inputSanitizer).sanitizeHtml("Ashburn<script>");
        verify(inputSanitizer).sanitizeHtml("20147<script>");

        // Verify user was saved (with sanitized values)
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should sanitize nullable address fields when provided")
    void testRegister_SanitizesNullableFields() {
        // Arrange - Request with all optional address fields present
        when(inputSanitizer.sanitizeHtml(anyString())).thenAnswer(invocation ->
                invocation.getArgument(0).toString().replace("<", "&lt;"));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateTokenFromUserId(any(UUID.class), anyString(), anyInt()))
                .thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(UUID.class), anyString()))
                .thenReturn("refresh-token");
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);
        when(tokenProvider.getTimeUntilExpiration(anyString())).thenReturn(3600000L);

        // Act
        authService.register(registerRequest);

        // Assert - Verify sanitization was called for all non-null fields
        verify(inputSanitizer).sanitizeHtml(registerRequest.getFirstName());
        verify(inputSanitizer).sanitizeHtml(registerRequest.getLastName());
        // Only verify addressLine1 if it's not null in the test data
        if (registerRequest.getAddressLine1() != null) {
            verify(inputSanitizer).sanitizeHtml(registerRequest.getAddressLine1());
        }
        if (registerRequest.getCity() != null) {
            verify(inputSanitizer).sanitizeHtml(registerRequest.getCity());
        }
        if (registerRequest.getZipCode() != null) {
            verify(inputSanitizer).sanitizeHtml(registerRequest.getZipCode());
        }
    }

    @Test
    @DisplayName("Should handle null optional fields without sanitization")
    void testRegister_HandlesNullOptionalFields() {
        // Arrange - Request without optional address fields
        RegisterRequest minimalRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .state("VA")
                .isHomeless(false)
                .build();

        when(inputSanitizer.sanitizeHtml("John")).thenReturn("John");
        when(inputSanitizer.sanitizeHtml("Doe")).thenReturn("Doe");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateTokenFromUserId(any(UUID.class), anyString(), anyInt()))
                .thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(UUID.class), anyString()))
                .thenReturn("refresh-token");
        when(tokenProvider.getTimeUntilExpiration(anyString())).thenReturn(3600000L);
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        // Act
        authService.register(minimalRequest);

        // Assert - Verify sanitization was only called for non-null fields
        verify(inputSanitizer).sanitizeHtml("John");
        verify(inputSanitizer).sanitizeHtml("Doe");
        verify(inputSanitizer, times(2)).sanitizeHtml(anyString()); // Only firstName and lastName
    }
}
