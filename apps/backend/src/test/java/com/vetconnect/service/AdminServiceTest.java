package com.vetconnect.service;

import com.vetconnect.dto.admin.AdminUserDetailDTO;
import com.vetconnect.dto.admin.SuspendUserRequest;
import com.vetconnect.exception.ResourceNotFoundException;
import com.vetconnect.mapper.AdminMapper;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.model.enums.UserRole;
import com.vetconnect.repository.UserRepository;
import com.vetconnect.util.InputSanitizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AdminService
 *
 * Tests admin functionality including:
 * - User suspension with sanitized reason
 * - User management operations
 * - Security validations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService Tests")
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private InputSanitizer inputSanitizer;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private AdminUserDetailDTO adminUserDetailDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .role(UserRole.USER)
                .isActive(true)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isHomeless(false)
                .tokenVersion(1)
                .build();

        adminUserDetailDTO = AdminUserDetailDTO.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.USER)
                .isActive(false)
                .build();
    }

    @Test
    @DisplayName("Should sanitize suspension reason before updating user")
    void testSuspendUser_SanitizesSuspensionReason() {
        // Arrange - Create suspend request with potentially malicious input
        String maliciousReason = "User violated terms <script>alert('xss')</script>";
        String sanitizedReason = "User violated terms &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;";

        SuspendUserRequest request = SuspendUserRequest.builder()
                .reason(maliciousReason)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inputSanitizer.sanitizeHtml(maliciousReason)).thenReturn(sanitizedReason);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminMapper.toDetailDTO(any(User.class))).thenReturn(adminUserDetailDTO);

        // Act
        AdminUserDetailDTO result = adminService.suspendUser(userId, request);

        // Assert
        assertNotNull(result);

        // Verify sanitization was called
        verify(inputSanitizer).sanitizeHtml(maliciousReason);

        // Verify user was saved with sanitized reason
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(sanitizedReason, savedUser.getSuspendedReason());
        assertFalse(savedUser.isActive());
        assertNotNull(savedUser.getSuspendedAt());
    }

    @Test
    @DisplayName("Should handle null suspension reason")
    void testSuspendUser_HandlesNullReason() {
        // Arrange - Request with null reason
        SuspendUserRequest request = SuspendUserRequest.builder()
                .reason(null)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminMapper.toDetailDTO(any(User.class))).thenReturn(adminUserDetailDTO);

        // Act
        AdminUserDetailDTO result = adminService.suspendUser(userId, request);

        // Assert
        assertNotNull(result);

        // Verify sanitization was NOT called for null value
        verify(inputSanitizer, never()).sanitizeHtml(anyString());

        // Verify user was saved with null reason
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNull(savedUser.getSuspendedReason());
        assertFalse(savedUser.isActive());
        assertNotNull(savedUser.getSuspendedAt());
    }

    @Test
    @DisplayName("Should suspend user successfully with valid reason")
    void testSuspendUser_Success() {
        // Arrange
        String reason = "Policy violation";
        SuspendUserRequest request = SuspendUserRequest.builder()
                .reason(reason)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inputSanitizer.sanitizeHtml(reason)).thenReturn(reason);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminMapper.toDetailDTO(any(User.class))).thenReturn(adminUserDetailDTO);

        // Act
        AdminUserDetailDTO result = adminService.suspendUser(userId, request);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(inputSanitizer).sanitizeHtml(reason);
        verify(userRepository).save(any(User.class));
        verify(adminMapper).toDetailDTO(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when suspending non-existent user")
    void testSuspendUser_UserNotFound() {
        // Arrange
        SuspendUserRequest request = SuspendUserRequest.builder()
                .reason("Test reason")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> adminService.suspendUser(userId, request));

        verify(userRepository).findById(userId);
        verify(inputSanitizer, never()).sanitizeHtml(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should set suspension timestamp when suspending user")
    void testSuspendUser_SetsSuspensionTimestamp() {
        // Arrange
        LocalDateTime beforeSuspension = LocalDateTime.now();

        SuspendUserRequest request = SuspendUserRequest.builder()
                .reason("Suspended for testing")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inputSanitizer.sanitizeHtml(anyString())).thenReturn("Suspended for testing");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminMapper.toDetailDTO(any(User.class))).thenReturn(adminUserDetailDTO);

        // Act
        adminService.suspendUser(userId, request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getSuspendedAt());
        assertTrue(savedUser.getSuspendedAt().isAfter(beforeSuspension) ||
                        savedUser.getSuspendedAt().isEqual(beforeSuspension),
                "Suspension timestamp should be set to current time");
    }

    @Test
    @DisplayName("Should deactivate user account when suspended")
    void testSuspendUser_DeactivatesAccount() {
        // Arrange
        assertTrue(user.isActive(), "User should start as active");

        SuspendUserRequest request = SuspendUserRequest.builder()
                .reason("Account suspended")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inputSanitizer.sanitizeHtml(anyString())).thenReturn("Account suspended");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminMapper.toDetailDTO(any(User.class))).thenReturn(adminUserDetailDTO);

        // Act
        adminService.suspendUser(userId, request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertFalse(savedUser.isActive(), "User should be deactivated after suspension");
    }

    @Test
    @DisplayName("Should sanitize complex XSS attempts in suspension reason")
    void testSuspendUser_SanitizesComplexXSS() {
        // Arrange - Various XSS attack vectors
        String xssReason = "Violation: <img src=x onerror=alert('xss')> <script>document.cookie</script>";
        String sanitizedReason = "Violation: &lt;img src=x onerror=alert(&#39;xss&#39;)&gt; &lt;script&gt;document.cookie&lt;/script&gt;";

        SuspendUserRequest request = SuspendUserRequest.builder()
                .reason(xssReason)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(inputSanitizer.sanitizeHtml(xssReason)).thenReturn(sanitizedReason);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(adminMapper.toDetailDTO(any(User.class))).thenReturn(adminUserDetailDTO);

        // Act
        adminService.suspendUser(userId, request);

        // Assert
        verify(inputSanitizer).sanitizeHtml(xssReason);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(sanitizedReason, savedUser.getSuspendedReason());
        assertFalse(savedUser.getSuspendedReason().contains("<script>"),
                "Sanitized reason should not contain script tags");
    }

    @Test
    @DisplayName("Should activate user and clear suspension data")
    void testActivateUser_ClearsSuspensionData() {
        // Arrange - Create suspended user
        user.setActive(false);
        user.setSuspendedAt(LocalDateTime.now());
        user.setSuspendedReason("Test suspension");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        AdminUserDetailDTO activeUserDTO = AdminUserDetailDTO.builder()
                .id(userId)
                .email("test@example.com")
                .isActive(true)
                .build();
        when(adminMapper.toDetailDTO(any(User.class))).thenReturn(activeUserDTO);

        // Act
        AdminUserDetailDTO result = adminService.activateUser(userId);

        // Assert
        assertNotNull(result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.isActive());
        assertNull(savedUser.getSuspendedAt());
        assertNull(savedUser.getSuspendedReason());
    }

    @Test
    @DisplayName("Should throw exception when activating non-existent user")
    void testActivateUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> adminService.activateUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
