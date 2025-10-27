package com.vetconnect.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Lightweight resource summary DTO
 * Used for:
 * 1. Resource listing pages (shows many resources)
 * 2. Search results
 * 3. Related resources
 * 4. Category browsing
 *
 * This is MUCH smaller than ResourceDTO to improve performance
 * when loading lists of resources.
 *
 * Example response:
 * {
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "name": "VA Greater Los Angeles Healthcare System",
 *   "shortDescription": "Comprehensive healthcare services for veterans in the LA area.",
 *   "categoryName": "Healthcare",
 *   "categoryIconName": "heart-pulse",
 *   "locationDisplay": "Los Angeles, CA",
 *   "isNational": false,
 *   "phoneNumber": "310-478-3711",
 *   "websiteUrl": "https://www.va.gov/greater-los-angeles-health-care"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSummaryDTO {

    private UUID id;
    private String name;

    /**
     * Shortened description (first 150 characters)
     * Full description available in ResourceDTO
     */
    private String shortDescription;

    /**
     * Category name only (not full category object)
     * Example: "Healthcare", "Housing"
     */
    private String categoryName;

    /**
     * Category icon for UI display
     */
    private String categoryIconName;

    /**
     * Simple location display
     * Examples: "Los Angeles, CA", "National"
     */
    private String locationDisplay;

    /**
     * Quick filter: Is this nationwide?
     */
    private boolean isNational;

    /**
     * Primary contact info (just the essentials for list view)
     */
    private String phoneNumber;
    private String websiteUrl;

    /**
     * Truncate description to max length for previews
     */
    public static String truncateDescription(String description, int maxLength) {
        if (description == null) {
            return "";
        }
        if (description.length() <= maxLength) {
            return description;
        }
        return description.substring(0, maxLength) + "...";
    }
}