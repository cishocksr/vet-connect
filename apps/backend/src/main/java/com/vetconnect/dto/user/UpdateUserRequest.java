package com.vetconnect.dto.user;

import com.vetconnect.model.enums.BranchOfService;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user profile
 *
 * PUT /api/users/profile
 * {
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "branchOfService": "ARMY",
 *   "addressLine1": "123 Main St",
 *   "addressLine2": "Apt 4B",
 *   "city": "Ashburn",
 *   "state": "VA",
 *   "zipCode": "20147",
 *   "isHomeless": false
 * }
 *
 * NOTES:
 * - All fields are optional (user can update only what they want)
 * - Email cannot be changed through this endpoint (requires separate verification flow)
 * - Password cannot be changed here (use UpdatePasswordRequest)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 100, message = "First name must be less than 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;

    private BranchOfService branchOfService;

    // ========== ADDRESS FIELDS ==========

    @Size(max = 255, message = "Address line 1 must be less than 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must be less than 255 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(min = 2, max = 2, message = "State must be exactly 2 characters")
    private String state;

    @Size(max = 10, message = "Zip code must be less than 10 characters")
    private String zipCode;

    private Boolean isHomeless;

    /**
     * Check if this update request actually contains any data
     * Useful for validation - don't allow empty updates
     */
    public boolean hasAnyUpdates() {
        return firstName != null ||
                lastName != null ||
                branchOfService != null ||
                addressLine1 != null ||
                addressLine2 != null ||
                city != null ||
                state != null ||
                zipCode != null ||
                isHomeless != null;
    }
}