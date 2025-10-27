package com.vetconnect.dto.saved;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Minimal saved resource summary
 * Used for:
 * 1. Saved resource counts ("You have 5 saved resources")
 * 2. Quick reference lists
 * 3. Mobile views (smaller payload)
 *
 * Example response:
 * {
 *   "id": "123e4567-e89b-12d3-a456-426614174000",
 *   "resourceId": "456e7890-e89b-12d3-a456-426614174111",
 *   "resourceName": "VA Healthcare",
 *   "categoryName": "Healthcare",
 *   "hasNotes": true,
 *   "savedAt": "2024-01-15T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedResourceSummaryDTO {

    /**
     * Saved resource record ID
     */
    private UUID id;

    /**
     * The actual resource ID
     */
    private UUID resourceId;

    /**
     * Resource name (for quick display)
     */
    private String resourceName;

    /**
     * Category name (for grouping/filtering)
     */
    private String categoryName;

    /**
     * Category icon name (for UI)
     */
    private String categoryIconName;

    /**
     * Quick flag: Does this have notes?
     */
    private boolean hasNotes;

    /**
     * When saved
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime savedAt;
}