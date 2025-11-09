package com.vetconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.dto.resource.CreateResourceRequest;
import com.vetconnect.model.ResourceCategory;
import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.repository.ResourceCategoryRepository;
import com.vetconnect.service.RedisRateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ResourceController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("ResourceController Integration Tests")
class ResourceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceCategoryRepository categoryRepository;

    @MockBean
    private RedisRateLimitService rateLimitService;

    private String authToken;
    private Integer categoryId;

    @BeforeEach
    void setUp() throws Exception {
        // Disable rate limiting for tests
        when(rateLimitService.allowRegisterAttempt(anyString())).thenReturn(true);
        when(rateLimitService.allowLoginAttempt(anyString())).thenReturn(true);

        // Create a category for testing
        ResourceCategory category = ResourceCategory.builder()
                .name("Housing")
                .description("Housing resources")
                .iconName("home")
                .build();
        ResourceCategory savedCategory = categoryRepository.save(category);
        categoryId = savedCategory.getId();

        // Register a user and get auth token
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("resourcetest@example.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .branchOfService(BranchOfService.ARMY)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        // Extract token from response
        authToken = objectMapper.readTree(response)
                .get("data")
                .get("token")
                .asText();
    }

    @Test
    @DisplayName("Should create resource with authentication")
    void testCreateResource_Success() throws Exception {
        CreateResourceRequest request = CreateResourceRequest.builder()
                .categoryId(categoryId)
                .name("VA Healthcare Center")
                .description("Comprehensive healthcare for veterans")
                .websiteUrl("https://va.gov")
                .phoneNumber("703-555-0100")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isNational(false)
                .build();

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("VA Healthcare Center"))
                .andExpect(jsonPath("$.data.city").value("Ashburn"));
    }

    @Test
    @DisplayName("Should fail to create resource without authentication")
    void testCreateResource_Unauthorized() throws Exception {
        CreateResourceRequest request = CreateResourceRequest.builder()
                .categoryId(categoryId)
                .name("Test Resource")
                .description("Test description")
                .websiteUrl("https://test.com")
                .isNational(false)
                .build();

        mockMvc.perform(post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should get all resources without authentication")
    void testGetAllResources_Success() throws Exception {
        mockMvc.perform(get("/api/resources")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Should search resources by keyword")
    void testSearchResources() throws Exception {
        // Create a resource first
        CreateResourceRequest request = CreateResourceRequest.builder()
                .categoryId(categoryId)
                .name("Housing Assistance Program")
                .description("Help with finding housing")
                .websiteUrl("https://housing.example.com")
                .isNational(true)
                .build();

        mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Search for it
        mockMvc.perform(get("/api/resources/search")
                        .param("keyword", "housing")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Should get resources by state")
    void testGetResourcesByState() throws Exception {
        mockMvc.perform(get("/api/resources/state/VA")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Should update resource with authentication")
    void testUpdateResource_Success() throws Exception {
        // Create a resource first
        CreateResourceRequest createRequest = CreateResourceRequest.builder()
                .categoryId(categoryId)
                .name("Original Name")
                .description("Original description")
                .phoneNumber("703-555-0100")
                .state("VA")
                .isNational(false)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String resourceId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("id")
                .asText();

        // Update the resource
        CreateResourceRequest updateRequest = CreateResourceRequest.builder()
                .categoryId(categoryId)
                .name("Updated Name")
                .description("Updated description")
                .phoneNumber("703-555-0200")
                .state("VA")
                .isNational(false)
                .build();

        mockMvc.perform(put("/api/resources/" + resourceId)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Name"));
    }

    @Test
    @DisplayName("Should delete resource with authentication")
    void testDeleteResource_Success() throws Exception {
        // Create a resource first
        CreateResourceRequest createRequest = CreateResourceRequest.builder()
                .categoryId(categoryId)
                .name("To Be Deleted")
                .description("This will be deleted")
                .email("delete@example.com")
                .state("VA")
                .isNational(false)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/resources")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String resourceId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("id")
                .asText();

        // Delete the resource
        mockMvc.perform(delete("/api/resources/" + resourceId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify it's deleted (might return 404 or 400 depending on implementation)
        mockMvc.perform(get("/api/resources/" + resourceId))
                .andExpect(status().is4xxClientError());
    }
}