package com.vetconnect.mapper;

import com.vetconnect.dto.user.UserDTO;
import com.vetconnect.dto.user.UserProfileDTO;
import com.vetconnect.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for User entity and User DTOs
 *
 * CONVERSION METHODS:
 * - toDTO() - Full user info (for authenticated user)
 * - toProfileDTO() - Public profile (for other users)
 * - updateEntityFromDTO() - Apply updates from UpdateUserRequest
 *
 * WHY USE MAPPERS?
 * 1. Single source of truth for conversions
 * 2. Easy to maintain and test
 * 3. Handles computed fields consistently
 * 4. Prevents code duplication across services
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserDTO (complete information)
     *
     * USE CASE: Return to authenticated user about themselves
     *
     * @param user The user entity from database
     * @return UserDTO with all user information
     */
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())  // Computed from entity method
                .branchOfService(user.getBranchOfService())
                .branchDisplayName(user.getBranchOfService().getDisplayName())
                .addressLine1(user.getAddressLine1())
                .addressLine2(user.getAddressLine2())
                .city(user.getCity())
                .state(user.getState())
                .zipCode(user.getZipCode())
                .isHomeless(user.isHomeless())
                .hasCompleteAddress(user.hasCompleteAddress())  // Computed from entity
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Convert User entity to UserProfileDTO (public information only)
     *
     * USE CASE: Show to other users, admin lists
     * EXCLUDES: Email, full address, homeless status, timestamps
     *
     * @param user The user entity from database
     * @return UserProfileDTO with limited public information
     */
    public UserProfileDTO toProfileDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserProfileDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .branchOfService(user.getBranchOfService())
                .branchDisplayName(user.getBranchOfService().getDisplayName())
                .city(user.getCity())  // Only city/state for privacy
                .state(user.getState())
                .build();
    }

    /**
     * Create new User entity from RegisterRequest
     *
     * NOTE: Password should be hashed BEFORE calling this method
     *
     * @param firstName User's first name
     * @param lastName User's last name
     * @param email User's email
     * @param hashedPassword Already hashed password
     * @param branchOfService Military branch
     * @return User entity (not yet saved to database)
     */
    public User toEntity(String email, String hashedPassword,
                         com.vetconnect.dto.auth.RegisterRequest request) {
        return User.builder()
                .email(email)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .branchOfService(request.getBranchOfService())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .isHomeless(request.isHomeless())
                .build();
    }

    /**
     * Update User entity from UpdateUserRequest
     *
     * IMPORTANT: Only updates non-null fields from the request
     * This allows partial updates (user can update just name, or just address, etc.)
     *
     * @param user Existing user entity to update
     * @param updateRequest The update request with new values
     */
    public void updateEntityFromDTO(User user,
                                    com.vetconnect.dto.user.UpdateUserRequest updateRequest) {
        if (updateRequest == null) {
            return;
        }

        // Update name fields if provided
        if (updateRequest.getFirstName() != null) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null) {
            user.setLastName(updateRequest.getLastName());
        }

        // Update branch of service if provided
        if (updateRequest.getBranchOfService() != null) {
            user.setBranchOfService(updateRequest.getBranchOfService());
        }

        // Update address fields if provided
        if (updateRequest.getAddressLine1() != null) {
            user.setAddressLine1(updateRequest.getAddressLine1());
        }
        if (updateRequest.getAddressLine2() != null) {
            user.setAddressLine2(updateRequest.getAddressLine2());
        }
        if (updateRequest.getCity() != null) {
            user.setCity(updateRequest.getCity());
        }
        if (updateRequest.getState() != null) {
            user.setState(updateRequest.getState());
        }
        if (updateRequest.getZipCode() != null) {
            user.setZipCode(updateRequest.getZipCode());
        }

        // Update homeless status if provided
        if (updateRequest.getIsHomeless() != null) {
            user.setHomeless(updateRequest.getIsHomeless());
        }
    }

    /**
     * Update User address from UpdateAddressRequest
     *
     * @param user Existing user entity to update
     * @param addressRequest The address update request
     */
    public void updateAddressFromDTO(User user,
                                     com.vetconnect.dto.user.UpdateAddressRequest addressRequest) {
        if (addressRequest == null) {
            return;
        }

        user.setAddressLine1(addressRequest.getAddressLine1());
        user.setAddressLine2(addressRequest.getAddressLine2());
        user.setCity(addressRequest.getCity());
        user.setState(addressRequest.getState());
        user.setZipCode(addressRequest.getZipCode());

        if (addressRequest.getIsHomeless() != null) {
            user.setHomeless(addressRequest.getIsHomeless());
        }
    }
}