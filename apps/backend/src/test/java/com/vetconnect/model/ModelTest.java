package com.vetconnect.model;

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
 * Comprehensive tests for model entities
 */
class ModelTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== ResourceCategory Tests ==========

    @Test
    void resourceCategory_withValidData_shouldPassValidation() {
        ResourceCategory category = ResourceCategory.builder()
                .name("Healthcare")
                .description("Healthcare resources for veterans")
                .iconName("health")
                .build();

        Set<ConstraintViolation<ResourceCategory>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void resourceCategory_withBlankName_shouldFailValidation() {
        ResourceCategory category = ResourceCategory.builder()
                .name("")
                .build();

        Set<ConstraintViolation<ResourceCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Category name is required")));
    }

    @Test
    void resourceCategory_withTooLongName_shouldFailValidation() {
        String longName = "a".repeat(101);
        ResourceCategory category = ResourceCategory.builder()
                .name(longName)
                .build();

        Set<ConstraintViolation<ResourceCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Category name must be less than 100 characters")));
    }

    @Test
    void resourceCategory_withTooLongDescription_shouldFailValidation() {
        String longDescription = "a".repeat(501);
        ResourceCategory category = ResourceCategory.builder()
                .name("Healthcare")
                .description(longDescription)
                .build();

        Set<ConstraintViolation<ResourceCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Description must be less than 500 characters")));
    }

    @Test
    void resourceCategory_withTooLongIconName_shouldFailValidation() {
        String longIconName = "a".repeat(51);
        ResourceCategory category = ResourceCategory.builder()
                .name("Healthcare")
                .iconName(longIconName)
                .build();

        Set<ConstraintViolation<ResourceCategory>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Icon name must be less than 50 characters")));
    }

    @Test
    void resourceCategory_builder_shouldSetAllFields() {
        ResourceCategory category = ResourceCategory.builder()
                .id(1)
                .name("Healthcare")
                .description("Healthcare resources")
                .iconName("health")
                .build();

        assertEquals(1, category.getId());
        assertEquals("Healthcare", category.getName());
        assertEquals("Healthcare resources", category.getDescription());
        assertEquals("health", category.getIconName());
    }

    @Test
    void resourceCategory_gettersAndSetters_shouldWork() {
        ResourceCategory category = new ResourceCategory();
        category.setId(1);
        category.setName("Healthcare");
        category.setDescription("Healthcare resources");
        category.setIconName("health");

        assertEquals(1, category.getId());
        assertEquals("Healthcare", category.getName());
        assertEquals("Healthcare resources", category.getDescription());
        assertEquals("health", category.getIconName());
    }

    @Test
    void resourceCategory_toString_shouldContainKeyInfo() {
        ResourceCategory category = ResourceCategory.builder()
                .id(1)
                .name("Healthcare")
                .iconName("health")
                .build();

        String result = category.toString();
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name=' Healthcare'") || result.contains("name='Healthcare'"));
        assertTrue(result.contains("iconName='health'"));
    }

    @Test
    void resourceCategory_hashCode_shouldReturnClassHashCode() {
        ResourceCategory category1 = new ResourceCategory();
        ResourceCategory category2 = new ResourceCategory();
        
        assertEquals(category1.hashCode(), category2.hashCode());
    }

    // ========== SavedResource Tests ==========

    @Test
    void savedResource_hasNotes_withNonBlankNotes_shouldReturnTrue() {
        SavedResource savedResource = SavedResource.builder()
                .notes("Some notes")
                .build();

        assertTrue(savedResource.hasNotes());
    }

    @Test
    void savedResource_hasNotes_withNullNotes_shouldReturnFalse() {
        SavedResource savedResource = SavedResource.builder()
                .notes(null)
                .build();

        assertFalse(savedResource.hasNotes());
    }

    @Test
    void savedResource_hasNotes_withEmptyNotes_shouldReturnFalse() {
        SavedResource savedResource = SavedResource.builder()
                .notes("")
                .build();

        assertFalse(savedResource.hasNotes());
    }

    @Test
    void savedResource_hasNotes_withBlankNotes_shouldReturnFalse() {
        SavedResource savedResource = SavedResource.builder()
                .notes("   ")
                .build();

        assertFalse(savedResource.hasNotes());
    }

    @Test
    void savedResource_getFormattedSavedDate_shouldReturnFormattedString() {
        LocalDateTime savedAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        SavedResource savedResource = SavedResource.builder()
                .savedAt(savedAt)
                .build();

        String result = savedResource.getFormattedSavedDate();
        assertNotNull(result);
        assertTrue(result.contains("2024"));
    }

    @Test
    void savedResource_builder_shouldSetAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime savedAt = LocalDateTime.now();
        User user = new User();
        user.setId(UUID.randomUUID());
        Resource resource = new Resource();
        resource.setId(UUID.randomUUID());

        SavedResource savedResource = SavedResource.builder()
                .id(id)
                .user(user)
                .resource(resource)
                .notes("Test notes")
                .savedAt(savedAt)
                .build();

        assertEquals(id, savedResource.getId());
        assertEquals(user, savedResource.getUser());
        assertEquals(resource, savedResource.getResource());
        assertEquals("Test notes", savedResource.getNotes());
        assertEquals(savedAt, savedResource.getSavedAt());
    }

    @Test
    void savedResource_equals_withSameId_shouldReturnTrue() {
        UUID id = UUID.randomUUID();
        SavedResource savedResource1 = SavedResource.builder()
                .id(id)
                .build();
        SavedResource savedResource2 = SavedResource.builder()
                .id(id)
                .build();

        assertEquals(savedResource1, savedResource2);
    }

    @Test
    void savedResource_equals_withDifferentIds_shouldReturnFalse() {
        SavedResource savedResource1 = SavedResource.builder()
                .id(UUID.randomUUID())
                .build();
        SavedResource savedResource2 = SavedResource.builder()
                .id(UUID.randomUUID())
                .build();

        assertNotEquals(savedResource1, savedResource2);
    }

    @Test
    void savedResource_equals_withNullId_shouldReturnFalse() {
        SavedResource savedResource1 = SavedResource.builder()
                .id(null)
                .build();
        SavedResource savedResource2 = SavedResource.builder()
                .id(UUID.randomUUID())
                .build();

        assertNotEquals(savedResource1, savedResource2);
    }

    @Test
    void savedResource_equals_withSameObject_shouldReturnTrue() {
        SavedResource savedResource = SavedResource.builder()
                .id(UUID.randomUUID())
                .build();

        assertEquals(savedResource, savedResource);
    }

    @Test
    void savedResource_equals_withNull_shouldReturnFalse() {
        SavedResource savedResource = SavedResource.builder()
                .id(UUID.randomUUID())
                .build();

        assertNotEquals(savedResource, null);
    }

    @Test
    void savedResource_equals_withDifferentClass_shouldReturnFalse() {
        SavedResource savedResource = SavedResource.builder()
                .id(UUID.randomUUID())
                .build();

        assertNotEquals(savedResource, "not a SavedResource");
    }

    @Test
    void savedResource_hashCode_shouldReturnClassHashCode() {
        SavedResource savedResource1 = new SavedResource();
        SavedResource savedResource2 = new SavedResource();

        assertEquals(savedResource1.hashCode(), savedResource2.hashCode());
    }

    @Test
    void savedResource_toString_shouldContainKeyInfo() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();
        LocalDateTime savedAt = LocalDateTime.now();
        
        User user = new User();
        user.setId(userId);
        
        Resource resource = new Resource();
        resource.setId(resourceId);

        SavedResource savedResource = SavedResource.builder()
                .id(id)
                .user(user)
                .resource(resource)
                .notes("Test notes")
                .savedAt(savedAt)
                .build();

        String result = savedResource.toString();
        assertTrue(result.contains("id=" + id));
        assertTrue(result.contains("userId=" + userId));
        assertTrue(result.contains("resourceId=" + resourceId));
        assertTrue(result.contains("hasNotes=true"));
    }

    @Test
    void savedResource_toString_withNullRelations_shouldNotFail() {
        UUID id = UUID.randomUUID();
        LocalDateTime savedAt = LocalDateTime.now();

        SavedResource savedResource = SavedResource.builder()
                .id(id)
                .user(null)
                .resource(null)
                .savedAt(savedAt)
                .build();

        String result = savedResource.toString();
        assertTrue(result.contains("userId=null"));
        assertTrue(result.contains("resourceId=null"));
    }

    @Test
    void savedResource_gettersAndSetters_shouldWork() {
        SavedResource savedResource = new SavedResource();
        UUID id = UUID.randomUUID();
        User user = new User();
        Resource resource = new Resource();
        LocalDateTime savedAt = LocalDateTime.now();

        savedResource.setId(id);
        savedResource.setUser(user);
        savedResource.setResource(resource);
        savedResource.setNotes("Notes");
        savedResource.setSavedAt(savedAt);

        assertEquals(id, savedResource.getId());
        assertEquals(user, savedResource.getUser());
        assertEquals(resource, savedResource.getResource());
        assertEquals("Notes", savedResource.getNotes());
        assertEquals(savedAt, savedResource.getSavedAt());
    }
}
