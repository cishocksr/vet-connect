package com.vetconnect.dto.saved;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vetconnect.dto.resource.ResourceSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Saved resource DTO
 * Represents a resource that a user has saved for later reference
 *
 * Example response:
 * {
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "resource": {
 *     "id": "456e7890-e89b-12d3-a456-426614174111",
 *     "name": "VA Greater Los Angeles Healthcare System",
 *     "shortDescription": "Comprehensive healthcare services...",
 *     "categoryName": "Healthcare",
 *     "categoryIconName": "heart-pulse",
 *     "locationDisplay": "Los Angeles, CA",
 *     "phoneNumber": "310-478-3711"
 *   },
 *   "notes": "Recommended by my case worker. Call for appointment.",
 *   "hasNotes": true,
 *   "savedAt": "2024-01-15T10:30:00",
 *   "formattedSavedDate": "2 days ago"
 * }
 *
 * USE CASES:
 * - User's "My Saved Resources" page
 * - Quick access to resources user is interested in
 * - Personal notes for each resource
 * - Track when user saved it
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedResourceDTO {

    /**
     * Unique ID of this saved resource record
     * (Not the resource ID itself)
     */
    private UUID id;

    /**
     * The actual resource information (summary version)
     * We use ResourceSummaryDTO instead of full ResourceDTO
     * to keep the response lightweight when showing lists
     */
    private ResourceSummaryDTO resource;

    /**
     * User's personal notes about this resource
     * Examples:
     * - "Recommended by my counselor"
     * - "Need to bring VA ID and discharge papers"
     * - "Hours: Mon-Fri 9am-5pm"
     * - "Ask for John Smith"
     */
    private String notes;

    /**
     * Quick check: Does this saved resource have notes?
     * Useful for UI to show/hide notes section
     */
    private boolean hasNotes;

    /**
     * When the user saved this resource
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime savedAt;

    /**
     * Human-readable saved date
     * Examples: "2 days ago", "Last week", "2 hours ago"
     * Frontend can format this using a library like moment.js
     */
    private String formattedSavedDate;

    // ========== HELPER METHODS ==========

    /**
     * Get the resource ID (convenience method)
     */
    public UUID getResourceId() {
        return resource != null ? resource.getId() : null;
    }

    /**
     * Get the resource name (convenience method)
     */
    public String getResourceName() {
        return resource != null ? resource.getName() : "Unknown Resource";
    }

    /**
     * Check if notes are empty/blank
     */
    public boolean hasValidNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
}