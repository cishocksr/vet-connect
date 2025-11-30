package com.vetconnect.controller;

import com.vetconnect.model.ResourceCategory;
import com.vetconnect.repository.ResourceCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ResourceCategoryController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("ResourceCategoryController Integration Tests")
class ResourceCategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceCategoryRepository categoryRepository;

    private Integer category1Id;
    private Integer category2Id;

    @BeforeEach
    void setUp() {
        // Create test categories
        ResourceCategory category1 = ResourceCategory.builder()
                .name("Housing")
                .description("Housing and shelter resources")
                .iconName("home")
                .build();
        ResourceCategory savedCategory1 = categoryRepository.save(category1);
        category1Id = savedCategory1.getId();

        ResourceCategory category2 = ResourceCategory.builder()
                .name("Healthcare")
                .description("Healthcare and medical resources")
                .iconName("health")
                .build();
        ResourceCategory savedCategory2 = categoryRepository.save(category2);
        category2Id = savedCategory2.getId();
    }

    @Test
    @DisplayName("GET /api/categories - Should return all categories")
    void getAllCategories_ShouldReturnAllCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categories retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[?(@.name == 'Housing')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'Healthcare')]").exists());
    }

    @Test
    @DisplayName("GET /api/categories/with-counts - Should return categories with counts")
    void getAllCategoriesWithCounts_ShouldReturnCategoriesWithCounts() throws Exception {
        mockMvc.perform(get("/api/categories/with-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Categories with counts retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data[0].resourceCount").exists());
    }

    @Test
    @DisplayName("GET /api/categories/{id} - Should return category by ID")
    void getCategoryById_WithValidId_ShouldReturnCategory() throws Exception {
        mockMvc.perform(get("/api/categories/" + category1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(category1Id))
                .andExpect(jsonPath("$.data.name").value("Housing"))
                .andExpect(jsonPath("$.data.description").value("Housing and shelter resources"))
                .andExpect(jsonPath("$.data.iconName").value("home"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - Should return 404 for non-existent category")
    void getCategoryById_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/categories/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/categories/name/{name} - Should return category by name")
    void getCategoryByName_WithValidName_ShouldReturnCategory() throws Exception {
        mockMvc.perform(get("/api/categories/name/Housing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category retrieved successfully"))
                .andExpect(jsonPath("$.data.name").value("Housing"))
                .andExpect(jsonPath("$.data.description").value("Housing and shelter resources"))
                .andExpect(jsonPath("$.data.iconName").value("home"));
    }

    @Test
    @DisplayName("GET /api/categories/name/{name} - Should return 404 for non-existent name")
    void getCategoryByName_WithInvalidName_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/categories/name/NonExistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/categories - Should return categories with correct structure")
    void getAllCategories_ShouldReturnCorrectStructure() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[0].description").exists())
                .andExpect(jsonPath("$.data[0].iconName").exists());
    }
}