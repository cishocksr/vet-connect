package com.vetconnect.dto.user;

import com.vetconnect.model.enums.BranchOfService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Public user profile DTO
 * This is a LIMITED version of user data that can be safely shown to other users
 *
 * What's EXCLUDED (for privacy):
 * - Email address
 * - Full address (only city/state shown)
 * - Homeless status
 * - Account timestamps
 *
 * Use cases:
 * - Showing who created a resource
 * - Displaying user in comments/reviews (future feature)
 * - Admin viewing user list
 *
 * Example:
 * {
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "fullName": "John Doe",
 *   "branchOfService": "ARMY",
 *   "branchDisplayName": "Army",
 *   "city": "Ashburn",
 *   "state": "VA"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private BranchOfService branchOfService;
    private String branchDisplayName;
    private String profilePictureUrl;

    // Only show city/state for privacy (not full address)
    private String city;
    private String state;

    /**
     * Get location display for UI
     * Returns: "Ashburn, VA" or "Location not provided"
     */
    public String getLocationDisplay() {
        if (city != null && state != null) {
            return city + ", " + state;
        }
        return "Location not provided";
    }
}