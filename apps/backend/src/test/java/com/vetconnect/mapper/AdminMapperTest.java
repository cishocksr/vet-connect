package com.vetconnect.mapper;

import com.vetconnect.dto.admin.AdminUserDetailDTO;
import com.vetconnect.dto.admin.AdminUserListDTO;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdminMapper Tests")
class AdminMapperTest {

    private AdminMapper adminMapper;
    private User user;

    @BeforeEach
    void setUp() {
        adminMapper = new AdminMapper();

        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.MARINES)
                .role(UserRole.USER)
                .addressLine1("123 Main St")
                .addressLine2("Apt 4")
                .city("Springfield")
                .state("VA")
                .zipCode("22150")
                .isHomeless(false)
                .isActive(true)
                .profilePictureUrl("/images/profile.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should convert User to AdminUserListDTO")
    void testToListDTO_Success() {
        // Act
        AdminUserListDTO result = adminMapper.toListDTO(user);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("John Doe", result.getFullName());
        assertEquals(BranchOfService.MARINES, result.getBranchOfService());
        assertEquals(UserRole.USER, result.getRole());
        assertTrue(result.isActive());
        assertFalse(result.isHomeless());
        assertEquals("Springfield", result.getCity());
        assertEquals("VA", result.getState());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getLastLoginAt());
    }

    @Test
    @DisplayName("Should return null when converting null User to ListDTO")
    void testToListDTO_Null() {
        // Act
        AdminUserListDTO result = adminMapper.toListDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert User to AdminUserDetailDTO")
    void testToDetailDTO_Success() {
        // Act
        AdminUserDetailDTO result = adminMapper.toDetailDTO(user);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("John Doe", result.getFullName());
        assertEquals(BranchOfService.MARINES, result.getBranchOfService());
        assertEquals(UserRole.USER, result.getRole());
        assertEquals("123 Main St", result.getAddressLine1());
        assertEquals("Apt 4", result.getAddressLine2());
        assertEquals("Springfield", result.getCity());
        assertEquals("VA", result.getState());
        assertEquals("22150", result.getZipCode());
        assertFalse(result.isHomeless());
        assertTrue(result.isActive());
        assertNull(result.getSuspendedAt());
        assertNull(result.getSuspendedReason());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertNotNull(result.getLastLoginAt());
        assertEquals("/images/profile.jpg", result.getProfilePictureUrl());
    }

    @Test
    @DisplayName("Should return null when converting null User to DetailDTO")
    void testToDetailDTO_Null() {
        // Act
        AdminUserDetailDTO result = adminMapper.toDetailDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should include suspended info in DetailDTO")
    void testToDetailDTO_WithSuspendedUser() {
        // Arrange
        LocalDateTime suspendedAt = LocalDateTime.now();
        user.setSuspendedAt(suspendedAt);
        user.setSuspendedReason("Policy violation");
        user.setActive(false);

        // Act
        AdminUserDetailDTO result = adminMapper.toDetailDTO(user);

        // Assert
        assertNotNull(result);
        assertFalse(result.isActive());
        assertEquals(suspendedAt, result.getSuspendedAt());
        assertEquals("Policy violation", result.getSuspendedReason());
    }

    @Test
    @DisplayName("Should handle homeless user in ListDTO")
    void testToListDTO_HomelessUser() {
        // Arrange
        user.setHomeless(true);
        user.setAddressLine1(null);
        user.setAddressLine2(null);
        user.setCity(null);
        user.setState(null);
        user.setZipCode(null);

        // Act
        AdminUserListDTO result = adminMapper.toListDTO(user);

        // Assert
        assertNotNull(result);
        assertTrue(result.isHomeless());
        assertNull(result.getCity());
        assertNull(result.getState());
    }
}
