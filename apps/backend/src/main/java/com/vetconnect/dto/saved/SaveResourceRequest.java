package com.vetconnect.dto.saved;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for saving a resource
 *
 * POST /api/saved
 * {
 *   "resourceId": "456e7890-e89b-12d3-a456-426614174111",
 *   "notes": "Recommended by counselor. Bring VA ID."
 * }
 *
 * BUSINESS RULES:
 * 1. User must be authenticated (userId comes from JWT token)
 * 2. Resource must exist
 * 3. User cannot save the same resource twice (unique constraint)
 * 4. Notes are optional but recommended
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveResourceRequest {

    /**
     * ID of the resource to save
     * Must be a valid resource ID that exists in the database
     */
    @NotNull(message = "Resource ID is required")
    private UUID resourceId;

    /**
     * Optional notes about this resource
     * Users can add personal reminders, instructions, or recommendations
     *
     * Max length: 2000 characters (plenty for detailed notes)
     */
    @Size(max = 2000, message = "Notes must be less than 2000 characters")
    private String notes;

    /**
     * Check if user is adding notes
     */
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
}