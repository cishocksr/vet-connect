package com.vetconnect.mapper;

import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.dto.user.UpdateAddressRequest;
import com.vetconnect.dto.user.UpdateUserRequest;
import com.vetconnect.dto.user.UserDTO;
import com.vetconnect.dto.user.UserProfileDTO;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;
    private User user;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();

        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .addressLine1("123 Main St")
                .addressLine2("Apt 4")
                .city("Springfield")
                .state("VA")
                .zipCode("22150")
                .isHomeless(false)
                .profilePictureUrl("/images/profile.jpg")
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should convert User to UserDTO")
    void testToDTO_Success() {
        // Act
        UserDTO result = userMapper.toDTO(user);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("John Doe", result.getFullName());
        assertEquals(BranchOfService.ARMY, result.getBranchOfService());
        assertEquals("Army", result.getBranchDisplayName());
        assertEquals("123 Main St", result.getAddressLine1());
        assertEquals("Apt 4", result.getAddressLine2());
        assertEquals("Springfield", result.getCity());
        assertEquals("VA", result.getState());
        assertEquals("22150", result.getZipCode());
        assertFalse(result.isHomeless());
        assertTrue(result.isHasCompleteAddress());
        assertEquals("/images/profile.jpg", result.getProfilePictureUrl());
        assertEquals(UserRole.USER, result.getRole());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("Should return null when User is null")
    void testToDTO_Null() {
        // Act
        UserDTO result = userMapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert User to UserProfileDTO")
    void testToProfileDTO_Success() {
        // Act
        UserProfileDTO result = userMapper.toProfileDTO(user);

        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("John Doe", result.getFullName());
        assertEquals(BranchOfService.ARMY, result.getBranchOfService());
        assertEquals("Army", result.getBranchDisplayName());
        assertEquals("/images/profile.jpg", result.getProfilePictureUrl());
        assertEquals("Springfield", result.getCity());
        assertEquals("VA", result.getState());
    }

    @Test
    @DisplayName("Should return null when converting null User to ProfileDTO")
    void testToProfileDTO_Null() {
        // Act
        UserProfileDTO result = userMapper.toProfileDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should create User entity from RegisterRequest")
    void testToEntity_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setBranchOfService(BranchOfService.NAVY);
        request.setAddressLine1("456 Oak Ave");
        request.setAddressLine2("Suite 10");
        request.setCity("Richmond");
        request.setState("VA");
        request.setZipCode("23220");
        request.setHomeless(false);

        // Act
        User result = userMapper.toEntity("newuser@example.com", "hashedPassword", request);

        // Assert
        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals(BranchOfService.NAVY, result.getBranchOfService());
        assertEquals("456 Oak Ave", result.getAddressLine1());
        assertEquals("Suite 10", result.getAddressLine2());
        assertEquals("Richmond", result.getCity());
        assertEquals("VA", result.getState());
        assertEquals("23220", result.getZipCode());
        assertFalse(result.isHomeless());
    }

    @Test
    @DisplayName("Should update User entity from UpdateUserRequest")
    void testUpdateEntityFromDTO_Success() {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Johnny");
        updateRequest.setLastName("Doeson");
        updateRequest.setBranchOfService(BranchOfService.AIR_FORCE);
        updateRequest.setAddressLine1("789 New St");
        updateRequest.setCity("Arlington");
        updateRequest.setState("VA");
        updateRequest.setZipCode("22201");
        updateRequest.setIsHomeless(false);

        // Act
        userMapper.updateEntityFromDTO(user, updateRequest);

        // Assert
        assertEquals("Johnny", user.getFirstName());
        assertEquals("Doeson", user.getLastName());
        assertEquals(BranchOfService.AIR_FORCE, user.getBranchOfService());
        assertEquals("789 New St", user.getAddressLine1());
        assertEquals("Arlington", user.getCity());
        assertEquals("VA", user.getState());
        assertEquals("22201", user.getZipCode());
        assertFalse(user.isHomeless());
    }

    @Test
    @DisplayName("Should handle partial update from UpdateUserRequest")
    void testUpdateEntityFromDTO_PartialUpdate() {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setFirstName("Johnny");
        // Only updating first name, leaving everything else

        // Act
        userMapper.updateEntityFromDTO(user, updateRequest);

        // Assert
        assertEquals("Johnny", user.getFirstName());
        assertEquals("Doe", user.getLastName()); // Unchanged
        assertEquals(BranchOfService.ARMY, user.getBranchOfService()); // Unchanged
    }

    @Test
    @DisplayName("Should handle null UpdateUserRequest")
    void testUpdateEntityFromDTO_NullRequest() {
        // Arrange
        String originalFirstName = user.getFirstName();

        // Act
        userMapper.updateEntityFromDTO(user, null);

        // Assert
        assertEquals(originalFirstName, user.getFirstName()); // Unchanged
    }

    @Test
    @DisplayName("Should update User address from UpdateAddressRequest")
    void testUpdateAddressFromDTO_Success() {
        // Arrange
        UpdateAddressRequest addressRequest = new UpdateAddressRequest();
        addressRequest.setAddressLine1("999 New Address");
        addressRequest.setAddressLine2("Unit B");
        addressRequest.setCity("Alexandria");
        addressRequest.setState("VA");
        addressRequest.setZipCode("22301");
        addressRequest.setIsHomeless(false);

        // Act
        userMapper.updateAddressFromDTO(user, addressRequest);

        // Assert
        assertEquals("999 New Address", user.getAddressLine1());
        assertEquals("Unit B", user.getAddressLine2());
        assertEquals("Alexandria", user.getCity());
        assertEquals("VA", user.getState());
        assertEquals("22301", user.getZipCode());
        assertFalse(user.isHomeless());
    }

    @Test
    @DisplayName("Should handle null UpdateAddressRequest")
    void testUpdateAddressFromDTO_NullRequest() {
        // Arrange
        String originalAddress = user.getAddressLine1();

        // Act
        userMapper.updateAddressFromDTO(user, null);

        // Assert
        assertEquals(originalAddress, user.getAddressLine1()); // Unchanged
    }

    @Test
    @DisplayName("Should handle UpdateAddressRequest with null homeless status")
    void testUpdateAddressFromDTO_NullHomelessStatus() {
        // Arrange
        UpdateAddressRequest addressRequest = new UpdateAddressRequest();
        addressRequest.setAddressLine1("555 Test St");
        addressRequest.setCity("Fairfax");
        addressRequest.setState("VA");
        addressRequest.setZipCode("22030");
        addressRequest.setIsHomeless(null); // Null homeless status

        boolean originalHomelessStatus = user.isHomeless();

        // Act
        userMapper.updateAddressFromDTO(user, addressRequest);

        // Assert
        assertEquals("555 Test St", user.getAddressLine1());
        assertEquals(originalHomelessStatus, user.isHomeless()); // Should remain unchanged
    }
}
