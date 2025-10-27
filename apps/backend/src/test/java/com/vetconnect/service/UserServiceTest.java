package com.vetconnect.service;

import com.vetconnect.dto.user.UpdateUserRequest;
import com.vetconnect.dto.user.UserDTO;
import com.vetconnect.exception.ResourceNotFoundException;
import com.vetconnect.mapper.UserMapper;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .build();

        userDTO = UserDTO.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .branchOfService(BranchOfService.ARMY)
                .build();
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John Doe", result.getFullName());

        verify(userRepository).findById(userId);
        verify(userMapper).toDTO(user);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId));

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Should update user profile successfully")
    void testUpdateUserProfile_Success() {
        // Arrange
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .city("Arlington")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .branchOfService(BranchOfService.ARMY)
                .city("Arlington")
                .state("VA")
                .zipCode("20147")
                .build();

        UserDTO updatedUserDTO = UserDTO.builder()
                .id(userId)
                .firstName("Jane")
                .lastName("Smith")
                .fullName("Jane Smith")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toDTO(updatedUser)).thenReturn(updatedUserDTO);

        // Act
        UserDTO result = userService.updateUserProfile(userId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("Jane Smith", result.getFullName());

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        // Act
        UserDTO result = userService.findByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).findByEmail("test@example.com");
    }
}