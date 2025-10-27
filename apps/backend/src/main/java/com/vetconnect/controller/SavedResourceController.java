package com.vetconnect.controller;

import com.vetconnect.dto.common.ApiResponse;
import com.vetconnect.dto.saved.SaveResourceRequest;
import com.vetconnect.dto.saved.SavedResourceDTO;
import com.vetconnect.dto.saved.UpdateSavedResourceNotesRequest;
import com.vetconnect.security.CustomUserDetails;
import com.vetconnect.service.SavedResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Saved Resource Controller
 *
 * ENDPOINTS:
 * - GET /api/saved - Get user's saved resources
 * - GET /api/saved/with-notes - Get saved resources with notes
 * - GET /api/saved/category/{categoryId} - Get by category
 * - GET /api/saved/count - Get count
 * - GET /api/saved/check/{resourceId} - Check if resource is saved
 * - POST /api/saved - Save a resource
 * - PATCH /api/saved/{id}/notes - Update notes
 * - DELETE /api/saved/{id} - Unsave resource
 *
 * ALL ENDPOINTS REQUIRE AUTHENTICATION
 */
@RestController
@RequestMapping("/api/saved")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Saved Resources", description = "User's saved resources management")
@SecurityRequirement(name = "bearerAuth")
public class SavedResourceController {

    private final SavedResourceService savedResourceService;

    /**
     * Get user's saved resources
     *
     * GET /api/saved
     *
     * Headers:
     * Authorization: Bearer <token>
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Saved resources retrieved successfully",
     *   "data": [
     *     {
     *       "id": "123e4567-e89b-12d3-a456-426614174000",
     *       "resource": { ...resource summary... },
     *       "notes": "Recommended by my counselor",
     *       "hasNotes": true,
     *       "savedAt": "2024-01-15T10:30:00",
     *       "formattedSavedDate": "2 days ago"
     *     },
     *     ...
     *   ]
     * }
     */
    @GetMapping
    @Operation(summary = "Get saved resources",
            description = "Get all saved resources for authenticated user")
    public ResponseEntity<ApiResponse<List<SavedResourceDTO>>> getSavedResources(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.debug("Getting saved resources for user: {}", currentUser.getId());

        List<SavedResourceDTO> savedResources =
                savedResourceService.getUserSavedResources(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Saved resources retrieved successfully", savedResources)
        );
    }

    /**
     * Get saved resources with notes only
     *
     * GET /api/saved/with-notes
     *
     * Response: 200 OK
     */
    @GetMapping("/with-notes")
    @Operation(summary = "Get saved resources with notes",
            description = "Get only saved resources that have notes")
    public ResponseEntity<ApiResponse<List<SavedResourceDTO>>> getSavedResourcesWithNotes(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.debug("Getting saved resources with notes for user: {}", currentUser.getId());

        List<SavedResourceDTO> savedResources =
                savedResourceService.getUserSavedResourcesWithNotes(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Saved resources with notes retrieved successfully", savedResources)
        );
    }

    /**
     * Get saved resources by category
     *
     * GET /api/saved/category/{categoryId}
     *
     * Response: 200 OK
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get saved resources by category",
            description = "Get saved resources filtered by category")
    public ResponseEntity<ApiResponse<List<SavedResourceDTO>>> getSavedResourcesByCategory(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Integer categoryId) {

        log.debug("Getting saved resources for user: {} in category: {}",
                currentUser.getId(), categoryId);

        List<SavedResourceDTO> savedResources =
                savedResourceService.getUserSavedResourcesByCategory(currentUser.getId(), categoryId);

        return ResponseEntity.ok(
                ApiResponse.success("Saved resources retrieved successfully", savedResources)
        );
    }

    /**
     * Get count of saved resources
     *
     * GET /api/saved/count
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Count retrieved successfully",
     *   "data": 15
     * }
     */
    @GetMapping("/count")
    @Operation(summary = "Get saved resource count",
            description = "Get count of user's saved resources")
    public ResponseEntity<ApiResponse<Long>> getSavedResourceCount(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.debug("Getting saved resource count for user: {}", currentUser.getId());

        long count = savedResourceService.getSavedResourceCount(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("Count retrieved successfully", count)
        );
    }

    /**
     * Check if resource is saved
     *
     * GET /api/saved/check/{resourceId}
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Check completed",
     *   "data": true
     * }
     */
    @GetMapping("/check/{resourceId}")
    @Operation(summary = "Check if resource is saved",
            description = "Check if user has saved a specific resource")
    public ResponseEntity<ApiResponse<Boolean>> isResourceSaved(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable UUID resourceId) {

        log.debug("Checking if resource {} is saved by user: {}",
                resourceId, currentUser.getId());

        boolean isSaved = savedResourceService.isResourceSaved(currentUser.getId(), resourceId);

        return ResponseEntity.ok(
                ApiResponse.success("Check completed", isSaved)
        );
    }

    /**
     * Save a resource
     *
     * POST /api/saved
     *
     * Request body:
     * {
     *   "resourceId": "123e4567-e89b-12d3-a456-426614174000",
     *   "notes": "Recommended by my counselor. Bring VA ID."
     * }
     *
     * Response: 201 Created
     * {
     *   "success": true,
     *   "message": "Resource saved successfully",
     *   "data": { ...saved resource... }
     * }
     */
    @PostMapping
    @Operation(summary = "Save resource",
            description = "Save a resource for later reference")
    public ResponseEntity<ApiResponse<SavedResourceDTO>> saveResource(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody SaveResourceRequest saveRequest) {

        log.info("User {} saving resource: {}", currentUser.getId(), saveRequest.getResourceId());

        SavedResourceDTO savedResource =
                savedResourceService.saveResource(currentUser.getId(), saveRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Resource saved successfully", savedResource));
    }

    /**
     * Update notes on saved resource
     *
     * PATCH /api/saved/{id}/notes
     *
     * Request body:
     * {
     *   "notes": "Updated notes: Called and scheduled appointment for Monday 10am"
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Notes updated successfully",
     *   "data": { ...updated saved resource... }
     * }
     */
    @PatchMapping("/{id}/notes")
    @Operation(summary = "Update notes",
            description = "Update notes on a saved resource")
    public ResponseEntity<ApiResponse<SavedResourceDTO>> updateNotes(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSavedResourceNotesRequest notesRequest) {

        log.info("User {} updating notes on saved resource: {}", currentUser.getId(), id);

        SavedResourceDTO updatedSavedResource =
                savedResourceService.updateNotes(currentUser.getId(), id, notesRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Notes updated successfully", updatedSavedResource)
        );
    }

    /**
     * Unsave resource
     *
     * DELETE /api/saved/{id}
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Resource removed from saved successfully"
     * }
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Unsave resource",
            description = "Remove resource from saved list")
    public ResponseEntity<ApiResponse<Void>> unsaveResource(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable UUID id) {

        log.info("User {} unsaving resource: {}", currentUser.getId(), id);

        savedResourceService.unsaveResource(currentUser.getId(), id);

        return ResponseEntity.ok(
                ApiResponse.success("Resource removed from saved successfully")
        );
    }
}