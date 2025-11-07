package com.vetconnect.dto.user;

import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Complete user information DTO
 * This contains ALL user data and should only be returned to:
 * 1. The user themselves (viewing their own profile)
 * 2. Admin users
 *
 * NEVER return this to other users - use UserProfileDTO for that
 *
 * Example response:
 * {
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "email": "john.doe@example.com",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "fullName": "John Doe",
 *   "branchOfService": "ARMY",
 *   "branchDisplayName": "Army",
 *   "addressLine1": "123 Main St",
 *   "addressLine2": "Apt 4B",
 *   "city": "Ashburn",
 *   "state": "VA",
 *   "zipCode": "20147",
 *   "isHomeless": false,
 *   "hasCompleteAddress": true,
 *   "createdAt": "2024-01-15T10:30:00",
 *   "updatedAt": "2024-01-20T14:45:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /**
     * Unique user identifier
     */
    private UUID id;

    /**
     * User's email address (used for login)
     */
    private String email;

    /**
     * User's first name
     */
    private String firstName;

    /**
     * User's last name
     */
    private String lastName;

    /**
     * User's full name (computed field)
     * Frontend doesn't need to concatenate firstName + lastName
     */
    private String fullName;

    /**
     * Military branch (enum value)
     * Examples: ARMY, NAVY, AIR_FORCE, MARINES, COAST_GUARD, SPACE_FORCE
     */
    private BranchOfService branchOfService;

    /**
     * Human-readable branch name (computed field)
     * Examples: "Army", "Navy", "Air Force"
     */
    private String branchDisplayName;

    // ========== ADDRESS INFORMATION ==========

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;

    /**
     * Indicates if user is currently homeless
     * Important for prioritizing certain resources
     */
    private boolean isHomeless;

    private String profilePictureUrl;

    /**
     * User's role in the system
     * Examples: USER, ADMIN
     */
    private UserRole role;


    /**
     * Convenience field: Does the user have a complete address?
     * Useful for frontend validation and display logic
     */
    private boolean hasCompleteAddress;

    // ========== TIMESTAMPS ==========

    /**
     * When the user account was created
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * When the user account was last updated
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========== HELPER METHODS ==========

    /**
     * Get formatted address for display
     * Returns: "123 Main St, Apt 4B, Ashburn, VA 20147"
     * Or: "Address not provided" if incomplete
     */
    public String getFormattedAddress() {
        if (!hasCompleteAddress) {
            return "Address not provided";
        }

        StringBuilder address = new StringBuilder();
        address.append(addressLine1);

        if (addressLine2 != null && !addressLine2.isBlank()) {
            address.append(", ").append(addressLine2);
        }

        address.append(", ").append(city)
                .append(", ").append(state)
                .append(" ").append(zipCode);

        return address.toString();
    }
}