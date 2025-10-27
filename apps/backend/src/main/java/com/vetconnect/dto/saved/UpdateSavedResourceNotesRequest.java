package com.vetconnect.dto.saved;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating notes on a saved resource
 *
 * PATCH /api/saved/{savedResourceId}/notes
 * {
 *   "notes": "Updated notes: Called and scheduled appointment for Monday 10am"
 * }
 *
 * USE CASES:
 * - User wants to add notes after initially saving
 * - User wants to update existing notes with new information
 * - User wants to remove notes (send empty string or null)
 *
 * NOTES:
 * - This is a PATCH operation (partial update)
 * - Only updates the notes field
 * - Sending null or empty string will clear the notes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSavedResourceNotesRequest {

    /**
     * Updated notes
     *
     * Can be:
     * - New notes (if previously empty)
     * - Updated notes (if previously had notes)
     * - null or empty string (to clear notes)
     */
    @Size(max = 2000, message = "Notes must be less than 2000 characters")
    private String notes;

    /**
     * Check if this request is clearing notes
     */
    public boolean isClearingNotes() {
        return notes == null || notes.trim().isEmpty();
    }

    /**
     * Check if this request has valid notes
     */
    public boolean hasValidNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
}