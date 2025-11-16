package com.vetconnect.dto.saved;

import com.vetconnect.dto.resource.ResourceSummaryDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for saved resource DTOs
 */
class SavedResourceDTOsTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== SaveResourceRequest Tests ==========

    @Test
    void saveResourceRequest_withValidData_shouldPassValidation() {
        SaveResourceRequest request = SaveResourceRequest.builder()
                .resourceId(UUID.randomUUID())
                .notes("Test notes")
                .build();

        Set<ConstraintViolation<SaveResourceRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void saveResourceRequest_withNullResourceId_shouldFailValidation() {
        SaveResourceRequest request = SaveResourceRequest.builder()
                .resourceId(null)
                .notes("Test notes")
                .build();

        Set<ConstraintViolation<SaveResourceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Resource ID is required")));
    }

    @Test
    void saveResourceRequest_withNotesTooLong_shouldFailValidation() {
        String longNotes = "a".repeat(2001);
        SaveResourceRequest request = SaveResourceRequest.builder()
                .resourceId(UUID.randomUUID())
                .notes(longNotes)
                .build();

        Set<ConstraintViolation<SaveResourceRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Notes must be less than 2000 characters")));
    }

    @Test
    void saveResourceRequest_withNotesExactlyAtLimit_shouldPassValidation() {
        String notesAtLimit = "a".repeat(2000);
        SaveResourceRequest request = SaveResourceRequest.builder()
                .resourceId(UUID.randomUUID())
                .notes(notesAtLimit)
                .build();

        Set<ConstraintViolation<SaveResourceRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void saveResourceRequest_hasNotes_withNonEmptyNotes_shouldReturnTrue() {
        SaveResourceRequest request = SaveResourceRequest.builder()
                .resourceId(UUID.randomUUID())
                .notes("Some notes")
                .build();

        assertTrue(request.hasNotes());
    }

    @Test
    void saveResourceRequest_hasNotes_withNullNotes_shouldReturnFalse() {
        SaveResourceRequest request = SaveResourceRequest.builder()
                .resourceId(UUID.randomUUID())
                .notes(null)
                .build();

        assertFalse(request.hasNotes());
    }

    @Test
    void saveResourceRequest_hasNotes_withEmptyNotes_shouldReturnFalse() {
        SaveResourceRequest request = SaveResourceRequest.builder()
                .resourceId(UUID.randomUUID())
                .notes("")
                .build();

        assertFalse(request.hasNotes());
    }

    @Test
    void saveResourceRequest_hasNotes_withWhitespaceNotes_shouldReturnFalse() {
        SaveResourceRequest request = SaveResourceRequest.builder()
                .resourceId(UUID.randomUUID())
                .notes("   ")
                .build();

        assertFalse(request.hasNotes());
    }

    @Test
    void saveResourceRequest_gettersAndSetters_shouldWork() {
        UUID resourceId = UUID.randomUUID();
        SaveResourceRequest request = new SaveResourceRequest();
        request.setResourceId(resourceId);
        request.setNotes("Test notes");

        assertEquals(resourceId, request.getResourceId());
        assertEquals("Test notes", request.getNotes());
    }

    // ========== SavedResourceDTO Tests ==========

    @Test
    void savedResourceDTO_builder_shouldCreateValidObject() {
        UUID id = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        ResourceSummaryDTO resourceSummary = ResourceSummaryDTO.builder()
                .id(resourceId)
                .name("Test Resource")
                .build();
        LocalDateTime savedAt = LocalDateTime.now();

        SavedResourceDTO dto = SavedResourceDTO.builder()
                .id(id)
                .resource(resourceSummary)
                .notes("Test notes")
                .hasNotes(true)
                .savedAt(savedAt)
                .formattedSavedDate("2 days ago")
                .build();

        assertEquals(id, dto.getId());
        assertEquals(resourceSummary, dto.getResource());
        assertEquals("Test notes", dto.getNotes());
        assertTrue(dto.isHasNotes());
        assertEquals(savedAt, dto.getSavedAt());
        assertEquals("2 days ago", dto.getFormattedSavedDate());
    }

    @Test
    void savedResourceDTO_getResourceId_withValidResource_shouldReturnResourceId() {
        UUID resourceId = UUID.randomUUID();
        ResourceSummaryDTO resourceSummary = ResourceSummaryDTO.builder()
                .id(resourceId)
                .name("Test Resource")
                .build();

        SavedResourceDTO dto = SavedResourceDTO.builder()
                .resource(resourceSummary)
                .build();

        assertEquals(resourceId, dto.getResourceId());
    }

    @Test
    void savedResourceDTO_getResourceId_withNullResource_shouldReturnNull() {
        SavedResourceDTO dto = SavedResourceDTO.builder()
                .resource(null)
                .build();

        assertNull(dto.getResourceId());
    }

    @Test
    void savedResourceDTO_getResourceName_withValidResource_shouldReturnResourceName() {
        ResourceSummaryDTO resourceSummary = ResourceSummaryDTO.builder()
                .id(UUID.randomUUID())
                .name("VA Healthcare")
                .build();

        SavedResourceDTO dto = SavedResourceDTO.builder()
                .resource(resourceSummary)
                .build();

        assertEquals("VA Healthcare", dto.getResourceName());
    }

    @Test
    void savedResourceDTO_getResourceName_withNullResource_shouldReturnDefault() {
        SavedResourceDTO dto = SavedResourceDTO.builder()
                .resource(null)
                .build();

        assertEquals("Unknown Resource", dto.getResourceName());
    }

    @Test
    void savedResourceDTO_hasValidNotes_withNonEmptyNotes_shouldReturnTrue() {
        SavedResourceDTO dto = SavedResourceDTO.builder()
                .notes("Some notes")
                .build();

        assertTrue(dto.hasValidNotes());
    }

    @Test
    void savedResourceDTO_hasValidNotes_withNullNotes_shouldReturnFalse() {
        SavedResourceDTO dto = SavedResourceDTO.builder()
                .notes(null)
                .build();

        assertFalse(dto.hasValidNotes());
    }

    @Test
    void savedResourceDTO_hasValidNotes_withEmptyNotes_shouldReturnFalse() {
        SavedResourceDTO dto = SavedResourceDTO.builder()
                .notes("")
                .build();

        assertFalse(dto.hasValidNotes());
    }

    @Test
    void savedResourceDTO_hasValidNotes_withWhitespaceNotes_shouldReturnFalse() {
        SavedResourceDTO dto = SavedResourceDTO.builder()
                .notes("   \n  ")
                .build();

        assertFalse(dto.hasValidNotes());
    }

    @Test
    void savedResourceDTO_gettersAndSetters_shouldWork() {
        SavedResourceDTO dto = new SavedResourceDTO();
        UUID id = UUID.randomUUID();
        ResourceSummaryDTO resource = ResourceSummaryDTO.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .build();
        LocalDateTime savedAt = LocalDateTime.now();

        dto.setId(id);
        dto.setResource(resource);
        dto.setNotes("Notes");
        dto.setHasNotes(true);
        dto.setSavedAt(savedAt);
        dto.setFormattedSavedDate("Today");

        assertEquals(id, dto.getId());
        assertEquals(resource, dto.getResource());
        assertEquals("Notes", dto.getNotes());
        assertTrue(dto.isHasNotes());
        assertEquals(savedAt, dto.getSavedAt());
        assertEquals("Today", dto.getFormattedSavedDate());
    }

    // ========== UpdateSavedResourceNotesRequest Tests ==========

    @Test
    void updateSavedResourceNotesRequest_withValidNotes_shouldPassValidation() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes("Updated notes")
                .build();

        Set<ConstraintViolation<UpdateSavedResourceNotesRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void updateSavedResourceNotesRequest_withNotesTooLong_shouldFailValidation() {
        String longNotes = "a".repeat(2001);
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes(longNotes)
                .build();

        Set<ConstraintViolation<UpdateSavedResourceNotesRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Notes must be less than 2000 characters")));
    }

    @Test
    void updateSavedResourceNotesRequest_withNotesAtLimit_shouldPassValidation() {
        String notesAtLimit = "a".repeat(2000);
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes(notesAtLimit)
                .build();

        Set<ConstraintViolation<UpdateSavedResourceNotesRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void updateSavedResourceNotesRequest_isClearingNotes_withNull_shouldReturnTrue() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes(null)
                .build();

        assertTrue(request.isClearingNotes());
    }

    @Test
    void updateSavedResourceNotesRequest_isClearingNotes_withEmptyString_shouldReturnTrue() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes("")
                .build();

        assertTrue(request.isClearingNotes());
    }

    @Test
    void updateSavedResourceNotesRequest_isClearingNotes_withWhitespace_shouldReturnTrue() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes("   ")
                .build();

        assertTrue(request.isClearingNotes());
    }

    @Test
    void updateSavedResourceNotesRequest_isClearingNotes_withValidNotes_shouldReturnFalse() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes("Some notes")
                .build();

        assertFalse(request.isClearingNotes());
    }

    @Test
    void updateSavedResourceNotesRequest_hasValidNotes_withNonEmptyNotes_shouldReturnTrue() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes("Valid notes")
                .build();

        assertTrue(request.hasValidNotes());
    }

    @Test
    void updateSavedResourceNotesRequest_hasValidNotes_withNull_shouldReturnFalse() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes(null)
                .build();

        assertFalse(request.hasValidNotes());
    }

    @Test
    void updateSavedResourceNotesRequest_hasValidNotes_withEmptyString_shouldReturnFalse() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes("")
                .build();

        assertFalse(request.hasValidNotes());
    }

    @Test
    void updateSavedResourceNotesRequest_hasValidNotes_withWhitespace_shouldReturnFalse() {
        UpdateSavedResourceNotesRequest request = UpdateSavedResourceNotesRequest.builder()
                .notes("  \t  ")
                .build();

        assertFalse(request.hasValidNotes());
    }

    @Test
    void updateSavedResourceNotesRequest_gettersAndSetters_shouldWork() {
        UpdateSavedResourceNotesRequest request = new UpdateSavedResourceNotesRequest();
        request.setNotes("New notes");

        assertEquals("New notes", request.getNotes());
    }
}
