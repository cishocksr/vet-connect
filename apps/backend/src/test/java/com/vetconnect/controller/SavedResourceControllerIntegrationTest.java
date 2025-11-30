package com.vetconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.dto.saved.SaveResourceRequest;
import com.vetconnect.dto.saved.UpdateSavedResourceNotesRequest;
import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.repository.ResourceCategoryRepository;
import com.vetconnect.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SavedResourceController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("SavedResourceController Integration Tests")
class SavedResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceCategoryRepository categoryRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    private String authToken;
    private UUID resourceId;
    private Integer categoryId;

    @BeforeEach
    void setUp() throws Exception {
        // Create a category
        ResourceCategory category = ResourceCategory.builder()
                .name("Housing")
                .description("Housing resources")
                .iconName("home")
                .build();
        ResourceCategory savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getId();

        // Create a resource
        Resource resource = Resource.builder()
                .name("VA Housing Center")
                .description("Housing assistance for veterans")
                .category(savedCategory)
                .city("Detroit")
                .state("MI")
                .zipCode("48201")
                .isNational(false)
                .build();
        Resource savedResource = resourceRepository.save(resource);
        resourceId = savedResource.getId();

        // Register a user and get auth token
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("savedtest@example.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .branchOfService(BranchOfService.ARMY)
                .city("Detroit")
                .state("MI")
                .zipCode("48201")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response)
                .get("data")
                .get("token")
                .asText();
    }

    @Test
    @DisplayName("POST /api/saved - Should save a resource")
    void saveResource_WithValidRequest_ShouldSaveResource() throws Exception {
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);
        saveRequest.setNotes("This looks helpful");

        mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource saved successfully"))
                .andExpect(jsonPath("$.data.notes").value("This looks helpful"))
                .andExpect(jsonPath("$.data.resource").exists());
    }

    @Test
    @DisplayName("POST /api/saved - Should save without notes")
    void saveResource_WithoutNotes_ShouldSaveResource() throws Exception {
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);

        mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/saved - Should require authentication")
    void saveResource_WithoutAuth_ShouldReturn401() throws Exception {
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);

        mockMvc.perform(post("/api/saved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/saved - Should return user's saved resources")
    void getSavedResources_WithAuth_ShouldReturnResources() throws Exception {
        // First save a resource
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);
        saveRequest.setNotes("Test notes");

        mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isCreated());

        // Then retrieve saved resources
        mockMvc.perform(get("/api/saved")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Saved resources retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].notes").value("Test notes"));
    }

    @Test
    @DisplayName("GET /api/saved/count - Should return count of saved resources")
    void getSavedResourceCount_ShouldReturnCount() throws Exception {
        // Save a resource first
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);

        mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isCreated());

        // Get count
        mockMvc.perform(get("/api/saved/count")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Count retrieved successfully"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("GET /api/saved/check/{resourceId} - Should check if resource is saved")
    void isResourceSaved_AfterSaving_ShouldReturnTrue() throws Exception {
        // Save a resource
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);

        mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isCreated());

        // Check if saved
        mockMvc.perform(get("/api/saved/check/" + resourceId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("GET /api/saved/check/{resourceId} - Should return false for unsaved resource")
    void isResourceSaved_WithoutSaving_ShouldReturnFalse() throws Exception {
        mockMvc.perform(get("/api/saved/check/" + resourceId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("PATCH /api/saved/{id}/notes - Should update notes")
    void updateNotes_WithValidRequest_ShouldUpdateNotes() throws Exception {
        // First save a resource
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);
        saveRequest.setNotes("Original notes");

        MvcResult saveResult = mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String saveResponse = saveResult.getResponse().getContentAsString();
        String savedResourceId = objectMapper.readTree(saveResponse)
                .get("data")
                .get("id")
                .asText();

        // Update notes
        UpdateSavedResourceNotesRequest notesRequest = new UpdateSavedResourceNotesRequest();
        notesRequest.setNotes("Updated notes");

        mockMvc.perform(patch("/api/saved/" + savedResourceId + "/notes")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notesRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notes updated successfully"))
                .andExpect(jsonPath("$.data.notes").value("Updated notes"));
    }

    @Test
    @DisplayName("DELETE /api/saved/{id} - Should unsave resource")
    void unsaveResource_WithValidId_ShouldRemoveResource() throws Exception {
        // First save a resource
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);

        MvcResult saveResult = mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String saveResponse = saveResult.getResponse().getContentAsString();
        String savedResourceId = objectMapper.readTree(saveResponse)
                .get("data")
                .get("id")
                .asText();

        // Delete it
        mockMvc.perform(delete("/api/saved/" + savedResourceId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Resource removed from saved successfully"));

        // Verify it's gone
        mockMvc.perform(get("/api/saved/check/" + resourceId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("GET /api/saved/with-notes - Should return only resources with notes")
    void getSavedResourcesWithNotes_ShouldReturnOnlyResourcesWithNotes() throws Exception {
        // Save resource with notes
        SaveResourceRequest withNotes = new SaveResourceRequest();
        withNotes.setResourceId(resourceId);
        withNotes.setNotes("Has notes");

        mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withNotes)))
                .andExpect(status().isCreated());

        // Get only with notes
        mockMvc.perform(get("/api/saved/with-notes")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].notes").value("Has notes"));
    }

    @Test
    @DisplayName("GET /api/saved/category/{categoryId} - Should filter by category")
    void getSavedResourcesByCategory_ShouldReturnFilteredResources() throws Exception {
        // Save a resource
        SaveResourceRequest saveRequest = new SaveResourceRequest();
        saveRequest.setResourceId(resourceId);

        mockMvc.perform(post("/api/saved")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveRequest)))
                .andExpect(status().isCreated());

        // Get by category
        mockMvc.perform(get("/api/saved/category/" + categoryId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}