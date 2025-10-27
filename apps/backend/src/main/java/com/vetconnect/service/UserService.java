package com.vetconnect.service;

import com.vetconnect.dto.user.UpdateAddressRequest;
import com.vetconnect.dto.user.UpdatePasswordRequest;
import com.vetconnect.dto.user.UpdateUserRequest;
import com.vetconnect.dto.user.UserDTO;
import com.vetconnect.dto.user.UserProfileDTO;
import com.vetconnect.mapper.UserMapper;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for user management operations
 *
 * RESPONSIBILITIES:
 * - User CRUD operations
 * - Profile management
 * - Password updates
 * - Address management
 * - User search and filtering
 *
 * TRANSACTION MANAGEMENT:
 * - @Transactional on methods that modify data
 * - Read-only transactions for queries
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)  // Default to read-only transactions
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ========== READ OPERATIONS ==========

    /**
     * Get user by ID
     *
     * @param userId User's UUID
     * @return UserDTO with complete user information
     * @throws RuntimeException if user not found
     */
    public UserDTO getUserById(UUID userId) {
        log.debug("Fetching user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return userMapper.toDTO(user);
    }

    /**
     * Get user by email (used for authentication)
     *
     * @param email User's email address
     * @return User entity
     * @throws RuntimeException if user not found
     */
    public User getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Get public profile by user ID
     *
     * USE CASE: When viewing another user's profile
     *
     * @param userId User's UUID
     * @return UserProfileDTO with public information only
     * @throws RuntimeException if user not found
     */
    public UserProfileDTO getUserProfile(UUID userId) {
        log.debug("Fetching user profile for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return userMapper.toProfileDTO(user);
    }

    /**
     * Check if email already exists
     *
     * USE CASE: Registration validation
     *
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get all users by branch of service
     *
     * USE CASE: Admin reports, analytics
     *
     * @param branch Military branch
     * @return List of UserProfileDTOs
     */
    public List<UserProfileDTO> getUsersByBranch(BranchOfService branch) {
        log.debug("Fetching users by branch: {}", branch);

        List<User> users = userRepository.findByBranchOfService(branch);

        return users.stream()
                .map(userMapper::toProfileDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all homeless users
     *
     * USE CASE: Targeted outreach, resource allocation
     *
     * @return List of UserProfileDTOs for homeless users
     */
    public List<UserProfileDTO> getHomelessUsers() {
        log.debug("Fetching homeless users");

        List<User> users = userRepository.findByIsHomelessTrue();

        return users.stream()
                .map(userMapper::toProfileDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get users by state
     *
     * USE CASE: Location-based resource recommendations
     *
     * @param state Two-letter state code
     * @return List of UserProfileDTOs
     */
    public List<UserProfileDTO> getUsersByState(String state) {
        log.debug("Fetching users by state: {}", state);

        List<User> users = userRepository.findByState(state);

        return users.stream()
                .map(userMapper::toProfileDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search users by name (first or last name)
     *
     * USE CASE: Admin user search
     *
     * @param name Search term
     * @return List of matching UserProfileDTOs
     */
    public List<UserProfileDTO> searchUsersByName(String name) {
        log.debug("Searching users by name: {}", name);

        List<User> users = userRepository.searchByName(name);

        return users.stream()
                .map(userMapper::toProfileDTO)
                .collect(Collectors.toList());
    }

    // ========== WRITE OPERATIONS ==========

    /**
     * Update user profile
     *
     * IMPORTANT:
     * - Only updates non-null fields
     * - Cannot update email or password here
     * - User must be authenticated (userId from JWT)
     *
     * @param userId User's UUID (from JWT token)
     * @param updateRequest Update request with new values
     * @return Updated UserDTO
     * @throws RuntimeException if user not found or validation fails
     */
    @Transactional
    public UserDTO updateUserProfile(UUID userId, UpdateUserRequest updateRequest) {
        log.info("Updating profile for user: {}", userId);

        // Validate request has updates
        if (!updateRequest.hasAnyUpdates()) {
            throw new RuntimeException("No updates provided");
        }

        // Get existing user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Apply updates
        userMapper.updateEntityFromDTO(user, updateRequest);

        // Save and return
        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile for user: {}", userId);

        return userMapper.toDTO(updatedUser);
    }

    /**
     * Update user address
     *
     * USE CASE:
     * - Veteran gets housing (updating from homeless to housed)
     * - Moving to new location
     * - Updating incomplete address
     *
     * @param userId User's UUID
     * @param addressRequest New address information
     * @return Updated UserDTO
     * @throws RuntimeException if user not found
     */
    @Transactional
    public UserDTO updateUserAddress(UUID userId, UpdateAddressRequest addressRequest) {
        log.info("Updating address for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Apply address updates
        userMapper.updateAddressFromDTO(user, addressRequest);

        // Save and return
        User updatedUser = userRepository.save(user);
        log.info("Successfully updated address for user: {}", userId);

        return userMapper.toDTO(updatedUser);
    }

    /**
     * Update user password
     *
     * SECURITY:
     * - Requires current password verification
     * - New password must be different from current
     * - New password must match confirmation
     * - Password is hashed before storage
     *
     * @param userId User's UUID
     * @param passwordRequest Password update request
     * @throws RuntimeException if validation fails
     */
    @Transactional
    public void updatePassword(UUID userId, UpdatePasswordRequest passwordRequest) {
        log.info("Updating password for user: {}", userId);

        // Validate passwords match
        if (!passwordRequest.passwordsMatch()) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        // Validate new password is different
        if (!passwordRequest.isNewPasswordDifferent()) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(passwordRequest.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Hash and save new password
        String hashedPassword = passwordEncoder.encode(passwordRequest.getNewPassword());
        user.setPasswordHash(hashedPassword);
        userRepository.save(user);

        log.info("Successfully updated password for user: {}", userId);

        // TODO: Invalidate all existing JWT tokens for this user
        // This forces re-login with new password
    }

    /**
     * Delete user account
     *
     * IMPORTANT: This is a soft delete in production
     * Consider implementing:
     * - Soft delete flag (is_deleted = true)
     * - Anonymization of personal data
     * - Retention period before permanent deletion
     *
     * @param userId User's UUID
     * @throws RuntimeException if user not found
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.warn("Deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // TODO: Implement soft delete instead of hard delete
        // user.setDeleted(true);
        // user.setDeletedAt(LocalDateTime.now());

        userRepository.delete(user);
        log.info("Successfully deleted user: {}", userId);
    }

    // ========== HELPER METHODS ==========

    /**
     * Get User entity by ID (internal use only)
     *
     * @param userId User's UUID
     * @return User entity
     * @throws RuntimeException if user not found
     */
    protected User getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }
}