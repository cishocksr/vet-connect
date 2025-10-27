package com.vetconnect.dto.resource;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing resource
 *
 * PUT /api/resources/{id}
 *
 * All fields are optional - only include fields you want to update
 *
 * Example (updating only phone and website):
 * {
 *   "phoneNumber": "703-555-0200",
 *   "websiteUrl": "https://newwebsite.com"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResourceRequest {

    /**
     * Update category (optional)
     */
    private Integer categoryId;

    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    private String description;

    // ========== CONTACT INFORMATION ==========

    @Size(max = 500, message = "Website URL must be less than 500 characters")
    private String websiteUrl;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phoneNumber;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    // ========== LOCATION INFORMATION ==========

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String addressLine1;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(min = 2, max = 2, message = "State must be exactly 2 characters")
    private String state;

    @Size(max = 10, message = "Zip code must be less than 10 characters")
    private String zipCode;

    private Boolean isNational;

    private String eligibilityCriteria;

    // ========== VALIDATION HELPERS ==========

    /**
     * Check if this update request actually contains any data
     */
    public boolean hasAnyUpdates() {
        return categoryId != null ||
                name != null ||
                description != null ||
                websiteUrl != null ||
                phoneNumber != null ||
                email != null ||
                addressLine1 != null ||
                city != null ||
                state != null ||
                zipCode != null ||
                isNational != null ||
                eligibilityCriteria != null;
    }
}