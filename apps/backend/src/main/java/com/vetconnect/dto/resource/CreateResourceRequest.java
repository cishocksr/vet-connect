package com.vetconnect.dto.resource;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new resource
 *
 * POST /api/resources
 * {
 *   "categoryId": 5,
 *   "name": "New Veteran Resource Center",
 *   "description": "Provides comprehensive services...",
 *   "websiteUrl": "https://example.com",
 *   "phoneNumber": "703-555-0100",
 *   "email": "contact@example.com",
 *   "addressLine1": "123 Main St",
 *   "city": "Ashburn",
 *   "state": "VA",
 *   "zipCode": "20147",
 *   "isNational": false,
 *   "eligibilityCriteria": "All veterans"
 * }
 *
 * VALIDATION RULES:
 * 1. Must have category
 * 2. Must have name and description
 * 3. Must have at least ONE contact method (website, phone, email, or address)
 * 4. If not national, should have state
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceRequest {

    /**
     * Category ID (required)
     * Must reference an existing category
     */
    @NotNull(message = "Category is required")
    private Integer categoryId;

    /**
     * Resource name (required)
     */
    @NotBlank(message = "Resource name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    /**
     * Detailed description (required)
     */
    @NotBlank(message = "Description is required")
    private String description;

    // ========== CONTACT INFORMATION (at least one required) ==========

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

    /**
     * Is this resource available nationwide?
     * Default: false
     */
    @Builder.Default
    private boolean isNational = false;

    /**
     * Eligibility criteria (optional)
     */
    private String eligibilityCriteria;

    // ========== VALIDATION HELPERS ==========

    /**
     * Check if at least one contact method is provided
     * Used in service layer validation
     */
    public boolean hasContactInfo() {
        return (websiteUrl != null && !websiteUrl.isBlank()) ||
                (phoneNumber != null && !phoneNumber.isBlank()) ||
                (email != null && !email.isBlank()) ||
                (addressLine1 != null && !addressLine1.isBlank());
    }

    /**
     * Check if address is complete
     */
    public boolean hasCompleteAddress() {
        return addressLine1 != null && !addressLine1.isBlank() &&
                city != null && !city.isBlank() &&
                state != null && !state.isBlank() &&
                zipCode != null && !zipCode.isBlank();
    }

    /**
     * Validate national resource
     * National resources don't need state, but local ones do
     */
    public boolean isValidForScope() {
        if (isNational) {
            return true; // National resources don't need location
        }
        // Local resources should have at least a state
        return state != null && !state.isBlank();
    }
}