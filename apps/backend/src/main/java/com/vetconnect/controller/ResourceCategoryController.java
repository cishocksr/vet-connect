package com.vetconnect.controller;

import com.vetconnect.dto.common.ApiResponse;
import com.vetconnect.dto.resource.ResourceCategoryDTO;
import com.vetconnect.service.ResourceCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Resource Category Controller
 *
 * ENDPOINTS:
 * - GET /api/categories - Get all categories
 * - GET /api/categories/with-counts - Get categories with resource counts
 * - GET /api/categories/{id} - Get single category
 * - GET /api/categories/name/{name} - Get category by name
 *
 * ALL ENDPOINTS ARE PUBLIC
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Resource category operations")
public class ResourceCategoryController {

    private final ResourceCategoryService categoryService;

    /**
     * Get all categories
     *
     * GET /api/categories
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Categories retrieved successfully",
     *   "data": [
     *     {
     *       "id": 1,
     *       "name": "Housing",
     *       "description": "Resources for finding shelter...",
     *       "iconName": "home"
     *     },
     *     ...
     *   ]
     * }
     */
    @GetMapping
    @Operation(summary = "Get all categories",
            description = "Get list of all resource categories")
    public ResponseEntity<ApiResponse<List<ResourceCategoryDTO>>> getAllCategories() {
        log.debug("Getting all categories");

        List<ResourceCategoryDTO> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(
                ApiResponse.success("Categories retrieved successfully", categories)
        );
    }

    /**
     * Get all categories with resource counts
     *
     * GET /api/categories/with-counts
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Categories with counts retrieved successfully",
     *   "data": [
     *     {
     *       "id": 1,
     *       "name": "Housing",
     *       "description": "Resources for finding shelter...",
     *       "iconName": "home",
     *       "resourceCount": 45
     *     },
     *     ...
     *   ]
     * }
     */
    @GetMapping("/with-counts")
    @Operation(summary = "Get categories with counts",
            description = "Get all categories with resource counts")
    public ResponseEntity<ApiResponse<List<ResourceCategoryDTO>>> getAllCategoriesWithCounts() {
        log.debug("Getting all categories with counts");

        List<ResourceCategoryDTO> categories = categoryService.getAllCategoriesWithCounts();

        return ResponseEntity.ok(
                ApiResponse.success("Categories with counts retrieved successfully", categories)
        );
    }

    /**
     * Get category by ID
     *
     * GET /api/categories/{id}
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Category retrieved successfully",
     *   "data": {
     *     "id": 1,
     *     "name": "Housing",
     *     "description": "Resources for finding shelter...",
     *     "iconName": "home"
     *   }
     * }
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID",
            description = "Get single category by ID")
    public ResponseEntity<ApiResponse<ResourceCategoryDTO>> getCategoryById(
            @PathVariable Integer id) {

        log.debug("Getting category by ID: {}", id);

        ResourceCategoryDTO category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Category retrieved successfully", category)
        );
    }

    /**
     * Get category by name
     *
     * GET /api/categories/name/{name}
     *
     * Example: GET /api/categories/name/Housing
     *
     * Response: 200 OK
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "Get category by name",
            description = "Get single category by name")
    public ResponseEntity<ApiResponse<ResourceCategoryDTO>> getCategoryByName(
            @PathVariable String name) {

        log.debug("Getting category by name: {}", name);

        ResourceCategoryDTO category = categoryService.getCategoryByName(name);

        return ResponseEntity.ok(
                ApiResponse.success("Category retrieved successfully", category)
        );
    }
}