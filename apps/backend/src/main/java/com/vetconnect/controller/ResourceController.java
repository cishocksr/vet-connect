package com.vetconnect.controller;

import com.vetconnect.dto.common.ApiResponse;
import com.vetconnect.dto.common.PageResponse;
import com.vetconnect.dto.resource.*;
import com.vetconnect.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Resource Controller
 *
 * ENDPOINTS:
 * - GET /api/resources - Get all resources (paginated)
 * - GET /api/resources/{id} - Get single resource
 * - GET /api/resources/search - Search resources
 * - GET /api/resources/category/{categoryId} - Get by category
 * - GET /api/resources/state/{state} - Get by state
 * - GET /api/resources/national - Get national resources
 * - POST /api/resources - Create resource (auth required)
 * - PUT /api/resources/{id} - Update resource (auth required)
 * - DELETE /api/resources/{id} - Delete resource (auth required)
 *
 * PUBLIC: GET endpoints
 * PROTECTED: POST, PUT, DELETE endpoints
 */
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resources", description = "Veteran resource management")
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * Get all resources (paginated)
     *
     * GET /api/resources?page=0&size=20
     *
     * Query Parameters:
     * - page: Page number (default: 0)
     * - size: Page size (default: 20)
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Resources retrieved successfully",
     *   "data": {
     *     "content": [ ...array of resources... ],
     *     "pageNumber": 0,
     *     "pageSize": 20,
     *     "totalElements": 150,
     *     "totalPages": 8,
     *     "isFirst": true,
     *     "isLast": false,
     *     "hasNext": true,
     *     "hasPrevious": false
     *   }
     * }
     */
    @GetMapping
    @Operation(summary = "Get all resources",
            description = "Get paginated list of all resources")
    public ResponseEntity<ApiResponse<PageResponse<ResourceSummaryDTO>>> getAllResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting all resources - page: {}, size: {}", page, size);

        PageResponse<ResourceSummaryDTO> resources = resourceService.getAllResources(page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Resources retrieved successfully", resources)
        );
    }

    /**
     * Get single resource by ID
     *
     * GET /api/resources/{id}
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Resource retrieved successfully",
     *   "data": {
     *     "id": "123e4567-e89b-12d3-a456-426614174000",
     *     "name": "VA Greater Los Angeles Healthcare System",
     *     "description": "Comprehensive healthcare services...",
     *     "category": { ... },
     *     "websiteUrl": "https://www.va.gov/...",
     *     ...
     *   }
     * }
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get resource by ID",
            description = "Get complete information about a single resource")
    public ResponseEntity<ApiResponse<ResourceDTO>> getResourceById(@PathVariable UUID id) {
        log.debug("Getting resource by ID: {}", id);

        ResourceDTO resource = resourceService.getResourceById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Resource retrieved successfully", resource)
        );
    }

    /**
     * Search resources
     *
     * GET /api/resources/search?keyword=housing&categoryId=1&state=VA&page=0&size=20
     *
     * Query Parameters (all optional):
     * - keyword: Search term
     * - categoryId: Filter by category
     * - state: Filter by state
     * - includeNational: Include national resources (default: true)
     * - page: Page number (default: 0)
     * - size: Page size (default: 20)
     * - sortBy: Sort field (default: "name")
     * - sortDirection: Sort direction "ASC" or "DESC" (default: "ASC")
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Search completed successfully",
     *   "data": {
     *     "results": { ...paginated results... },
     *     "searchMetadata": { ...search criteria... }
     *   }
     * }
     */
    @GetMapping("/search")
    @Operation(summary = "Search resources",
            description = "Search and filter resources with multiple criteria")
    public ResponseEntity<ApiResponse<ResourceSearchResponse>> searchResources(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "true") boolean includeNational,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.debug("Searching resources - keyword: {}, category: {}, state: {}",
                keyword, categoryId, state);

        ResourceSearchRequest searchRequest = ResourceSearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .state(state)
                .includeNational(includeNational)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        ResourceSearchResponse searchResponse = resourceService.searchResources(searchRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Search completed successfully", searchResponse)
        );
    }

    /**
     * Get resources by category
     *
     * GET /api/resources/category/{categoryId}?page=0&size=20
     *
     * Response: 200 OK
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get resources by category",
            description = "Get all resources in a specific category")
    public ResponseEntity<ApiResponse<PageResponse<ResourceSummaryDTO>>> getResourcesByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting resources by category: {}", categoryId);

        PageResponse<ResourceSummaryDTO> resources =
                resourceService.getResourcesByCategory(categoryId, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Resources retrieved successfully", resources)
        );
    }

    /**
     * Get resources by state
     *
     * GET /api/resources/state/{state}?page=0&size=20
     *
     * Returns state-specific + national resources
     *
     * Response: 200 OK
     */
    @GetMapping("/state/{state}")
    @Operation(summary = "Get resources by state",
            description = "Get resources available in a specific state (includes national)")
    public ResponseEntity<ApiResponse<PageResponse<ResourceSummaryDTO>>> getResourcesByState(
            @PathVariable String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Getting resources by state: {}", state);

        PageResponse<ResourceSummaryDTO> resources =
                resourceService.getResourcesByState(state, page, size);

        return ResponseEntity.ok(
                ApiResponse.success("Resources retrieved successfully", resources)
        );
    }

    /**
     * Get national resources
     *
     * GET /api/resources/national
     *
     * Response: 200 OK
     */
    @GetMapping("/national")
    @Operation(summary = "Get national resources",
            description = "Get all nationwide resources")
    public ResponseEntity<ApiResponse<List<ResourceDTO>>> getNationalResources() {
        log.debug("Getting national resources");

        List<ResourceDTO> resources = resourceService.getNationalResources();

        return ResponseEntity.ok(
                ApiResponse.success("National resources retrieved successfully", resources)
        );
    }

    // ========== PROTECTED ENDPOINTS (REQUIRE AUTH) ==========

    /**
     * Create new resource
     *
     * POST /api/resources
     *
     * Headers:
     * Authorization: Bearer <token>
     *
     * Request body:
     * {
     *   "categoryId": 5,
     *   "name": "New Veteran Resource Center",
     *   "description": "Provides comprehensive services...",
     *   "websiteUrl": "https://example.com",
     *   "phoneNumber": "703-555-0100",
     *   "addressLine1": "123 Main St",
     *   "city": "Ashburn",
     *   "state": "VA",
     *   "zipCode": "20147",
     *   "isNational": false
     * }
     *
     * Response: 201 Created
     * {
     *   "success": true,
     *   "message": "Resource created successfully",
     *   "data": { ...created resource... }
     * }
     */
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create resource",
            description = "Create new resource (authentication required)")
    public ResponseEntity<ApiResponse<ResourceDTO>> createResource(
            @Valid @RequestBody CreateResourceRequest createRequest) {

        log.info("Creating new resource: {}", createRequest.getName());

        ResourceDTO createdResource = resourceService.createResource(createRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource created successfully", createdResource));
    }

    /**
     * Update existing resource
     *
     * PUT /api/resources/{id}
     *
     * Headers:
     * Authorization: Bearer <token>
     *
     * Request body: (all fields optional)
     * {
     *   "name": "Updated Name",
     *   "phoneNumber": "703-555-0200"
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Resource updated successfully",
     *   "data": { ...updated resource... }
     * }
     */
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update resource",
            description = "Update existing resource (authentication required)")
    public ResponseEntity<ApiResponse<ResourceDTO>> updateResource(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResourceRequest updateRequest) {

        log.info("Updating resource: {}", id);

        ResourceDTO updatedResource = resourceService.updateResource(id, updateRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Resource updated successfully", updatedResource)
        );
    }

    /**
     * Delete resource
     *
     * DELETE /api/resources/{id}
     *
     * Headers:
     * Authorization: Bearer <token>
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Resource deleted successfully"
     * }
     */
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete resource",
            description = "Delete resource (authentication required)")
    public ResponseEntity<ApiResponse<Void>> deleteResource(@PathVariable UUID id) {
        log.info("Deleting resource: {}", id);

        resourceService.deleteResource(id);

        return ResponseEntity.ok(
                ApiResponse.success("Resource deleted successfully")
        );
    }
}