package com.vetconnect.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user address only
 *
 * PATCH /api/users/address
 * {
 *   "addressLine1": "123 Main St",
 *   "addressLine2": "Apt 4B",
 *   "city": "Ashburn",
 *   "state": "VA",
 *   "zipCode": "20147",
 *   "isHomeless": false
 * }
 *
 * USE CASE:
 * This is separate from UpdateUserRequest because address updates
 * are particularly important for veterans transitioning from homelessness.
 * Having a dedicated endpoint makes it easier to:
 * - Track address changes specifically
 * - Provide targeted UI for address updates
 * - Trigger notifications when a homeless user gets housing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressRequest {

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
     * Check if address is being cleared (all fields null)
     */
    public boolean isClearingAddress() {
        return addressLine1 == null &&
                addressLine2 == null &&
                city == null &&
                state == null &&
                zipCode == null;
    }

    /**
     * Check if this is a complete address
     */
    public boolean isCompleteAddress() {
        return addressLine1 != null && !addressLine1.isBlank() &&
                city != null && !city.isBlank() &&
                state != null && !state.isBlank() &&
                zipCode != null && !zipCode.isBlank();
    }
}