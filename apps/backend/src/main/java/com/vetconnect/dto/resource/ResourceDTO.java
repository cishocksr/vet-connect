package com.vetconnect.dto.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Complete resource information DTO
 * Contains ALL resource details - used for:
 * 1. Single resource detail page
 * 2. Resource editing (admin)
 * 3. Full resource information in saved resources
 *
 * Example response:
 * {
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "name": "VA Greater Los Angeles Healthcare System",
 *   "description": "Comprehensive healthcare services for veterans...",
 *   "category": {
 *     "id": 5,
 *     "name": "Healthcare",
 *     "description": "Primary care, dental, and general health services",
 *     "iconName": "heart-pulse"
 *   },
 *   "websiteUrl": "https://www.va.gov/greater-los-angeles-health-care",
 *   "phoneNumber": "310-478-3711",
 *   "email": "vhaglahealthcare@va.gov",
 *   "addressLine1": "11301 Wilshire Blvd",
 *   "city": "Los Angeles",
 *   "state": "CA",
 *   "zipCode": "90073",
 *   "isNational": false,
 *   "locationDisplay": "Los Angeles, CA 90073",
 *   "eligibilityCriteria": "All enrolled veterans",
 *   "hasContactInfo": true,
 *   "hasCompleteAddress": true,
 *   "createdAt": "2024-01-15T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {

    /**
     * Unique resource identifier
     */
    private UUID id;

    /**
     * Resource name/title
     * Example: "VA Greater Los Angeles Healthcare System"
     */
    private String name;

    /**
     * Detailed description of what this resource provides
     * Can be multiple paragraphs
     */
    private String description;

    /**
     * Resource category information
     * Includes: id, name, description, iconName
     */
    private ResourceCategoryDTO category;

    // ========== CONTACT INFORMATION ==========

    /**
     * Resource website URL
     */
    private String websiteUrl;

    /**
     * Phone number (formatted or unformatted)
     */
    private String phoneNumber;

    /**
     * Contact email
     */
    private String email;

    // ========== LOCATION INFORMATION ==========

    private String addressLine1;
    private String city;
    private String state;
    private String zipCode;

    /**
     * Is this resource available nationwide?
     * true = Available everywhere (like VA hotline)
     * false = Location-specific
     */
    private boolean isNational;

    /**
     * Formatted location string for display
     * Examples:
     * - "Los Angeles, CA 90073" (local resource)
     * - "National" (nationwide resource)
     */
    private String locationDisplay;

    // ========== ADDITIONAL INFORMATION ==========

    /**
     * Who is eligible to use this resource?
     * Example: "All veterans with honorable discharge"
     */
    private String eligibilityCriteria;

    /**
     * Computed field: Does this resource have any contact info?
     * Useful for frontend validation/display
     */
    private boolean hasContactInfo;

    /**
     * Computed field: Does this resource have a complete physical address?
     */
    private boolean hasCompleteAddress;

    /**
     * When this resource was added to the system
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // ========== HELPER METHODS ==========

    /**
     * Get primary contact method for display
     * Priority: website > phone > email > address
     */
    public String getPrimaryContact() {
        if (websiteUrl != null && !websiteUrl.isBlank()) {
            return websiteUrl;
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            return phoneNumber;
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        if (hasCompleteAddress) {
            return locationDisplay;
        }
        return "Contact information not available";
    }

    /**
     * Get formatted full address
     */
    public String getFullAddress() {
        if (!hasCompleteAddress) {
            return null;
        }
        return String.format("%s, %s, %s %s",
                addressLine1, city, state, zipCode);
    }
}