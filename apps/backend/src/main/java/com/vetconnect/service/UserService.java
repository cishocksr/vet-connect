package com.vetconnect.service;

import com.vetconnect.dto.user.UpdateAddressRequest;
import com.vetconnect.dto.user.UpdatePasswordRequest;
import com.vetconnect.dto.user.UpdateUserRequest;
import com.vetconnect.dto.user.UserDTO;
import com.vetconnect.dto.user.UserProfileDTO;
import com.vetconnect.exception.InvalidCredentialsException;
import com.vetconnect.exception.ResourceNotFoundException;
import com.vetconnect.exception.ValidationException;
import com.vetconnect.mapper.UserMapper;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.repository.UserRepository;
import com.vetconnect.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


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
    private final FileStorageService fileStorageService;
    private final InputSanitizer inputSanitizer;

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
     * Update user profile picture
     *
     * @param userId User's UUID
     * @param file Uploaded image file
     * @return Updated UserDTO
     */
    @Transactional
    public UserDTO updateProfilePicture(UUID userId, MultipartFile file) {
        log.info("Updating profile picture for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Delete old profile picture if exists
        if (user.getProfilePictureUrl() != null) {
            fileStorageService.deleteProfilePicture(user.getProfilePictureUrl());
        }

        // Store new profile picture
        String profilePictureUrl = fileStorageService.storeProfilePicture(file, userId);
        user.setProfilePictureUrl(profilePictureUrl);

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile picture for user: {}", userId);

        return userMapper.toDTO(updatedUser);
    }

    /**
     * Delete user profile picture
     *
     * @param userId User's UUID
     * @return Updated UserDTO
     */
    @Transactional
    public UserDTO deleteProfilePicture(UUID userId) {
        log.info("Deleting profile picture for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Delete file
        if (user.getProfilePictureUrl() != null) {
            fileStorageService.deleteProfilePicture(user.getProfilePictureUrl());
            user.setProfilePictureUrl(null);
        }

        User updatedUser = userRepository.save(user);
        log.info("Successfully deleted profile picture for user: {}", userId);

        return userMapper.toDTO(updatedUser);
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

        // Sanitize text fields before applying updates
        if (updateRequest.getFirstName() != null) {
            updateRequest.setFirstName(inputSanitizer.sanitizeHtml(updateRequest.getFirstName()));
        }
        if (updateRequest.getLastName() != null) {
            updateRequest.setLastName(inputSanitizer.sanitizeHtml(updateRequest.getLastName()));
        }

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

        // Sanitize address fields
        if (addressRequest.getAddressLine1() != null) {
            addressRequest.setAddressLine1(inputSanitizer.sanitizeHtml(addressRequest.getAddressLine1()));
        }
        if (addressRequest.getAddressLine2() != null) {
            addressRequest.setAddressLine2(inputSanitizer.sanitizeHtml(addressRequest.getAddressLine2()));
        }
        if (addressRequest.getCity() != null) {
            addressRequest.setCity(inputSanitizer.sanitizeHtml(addressRequest.getCity()));
        }
        if (addressRequest.getZipCode() != null) {
            addressRequest.setZipCode(inputSanitizer.sanitizeHtml(addressRequest.getZipCode()));
        }

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
     * Soft delete user (marks as deleted instead of removing from database)
     *
     * SECURITY:
     * - Preserves data integrity
     * - Allows data recovery if needed
     * - Maintains referential integrity with saved resources
     * - Increments token version to invalidate all tokens
     *
     * @param userId User ID to delete
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.debug("Soft deleting user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if already deleted
        if (user.getIsDeleted()) {
            log.warn("Attempted to delete already deleted user: {}", userId);
            throw new ValidationException("User is already deleted");
        }

        // Soft delete the user
        user.markAsDeleted();  // ← HERE'S WHERE WE USE IT!

        // Increment token version to invalidate all tokens
        user.incrementTokenVersion();

        userRepository.save(user);

        log.info("User soft deleted successfully: {} ({})", user.getEmail(), userId);
    }

    /**
     * Permanently delete a user (admin only - use with caution)
     * This actually removes the user from the database
     *
     * @param userId User ID to permanently delete
     */
    @Transactional
    public void permanentlyDeleteUser(UUID userId) {
        log.warn("PERMANENT DELETE requested for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);

        log.warn("User PERMANENTLY deleted: {} ({})", user.getEmail(), userId);
    }

    /**
     * Restore a soft-deleted user (admin only)
     *
     * @param userId User ID to restore
     */
    @Transactional
    public void restoreUser(UUID userId) {
        log.debug("Restoring deleted user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.getIsDeleted()) {
            throw new ValidationException("User is not deleted");
        }

        user.setIsDeleted(false);
        user.setDeletedAt(null);

        userRepository.save(user);

        log.info("User restored successfully: {} ({})", user.getEmail(), userId);
    }

    /**
     * Change user password and invalidate all tokens
     *
     * SECURITY:
     * - Increments token version to invalidate all existing tokens
     * - Forces user to re-login on all devices
     * - Prevents compromised tokens from being used
     *
     * @param userId User ID
     * @param currentPassword Current password for verification
     * @param newPassword New password
     */
    @Transactional
    public void changePassword(UUID userId, String currentPassword, String newPassword) {
        log.debug("Password change request for user: {}", userId);

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        // INCREMENT TOKEN VERSION - This invalidates all existing tokens!
        user.incrementTokenVersion();

        userRepository.save(user);

        log.info("Password changed successfully for user: {}. All tokens invalidated.", userId);
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