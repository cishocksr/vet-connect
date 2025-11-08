package com.vetconnect.controller;

import com.vetconnect.dto.admin.*;
import com.vetconnect.dto.common.ApiResponse;
import com.vetconnect.dto.user.UpdateUserRequest;
import com.vetconnect.model.enums.UserRole;
import com.vetconnect.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin Controller - System Administration
 *
 * ALL ENDPOINTS REQUIRE ADMIN ROLE
 *
 * ENDPOINTS:
 * - GET /api/admin/users - List all users
 * - GET /api/admin/users/search - Search users
 * - GET /api/admin/users/{id} - Get user details
 * - PUT /api/admin/users/{id}/role - Update user role
 * - POST /api/admin/users/{id}/suspend - Suspend user
 * - POST /api/admin/users/{id}/activate - Activate user
 * - PUT /api/admin/users/{id} - Update user
 * - DELETE /api/admin/users/{id} - Delete user
 * - GET /api/admin/stats - System statistics
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin", description = "System administration endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * Get all users with pagination
     *
     * GET /api/admin/users?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "List all users with pagination")
    public ResponseEntity<ApiResponse<Page<AdminUserListDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.debug("Admin fetching users - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AdminUserListDTO> users = adminService.getAllUsers(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Search users
     *
     * GET /api/admin/users/search?q=john&page=0&size=20
     */
    @GetMapping("/users/search")
    @Operation(summary = "Search users", description = "Search users by email, name, or location")
    public ResponseEntity<ApiResponse<Page<AdminUserListDTO>>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("Admin searching users: {}", q);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminUserListDTO> users = adminService.searchUsers(q, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Search results", users));
    }

    /**
     * Filter users by role
     *
     * GET /api/admin/users/filter/role?role=ADMIN
     */
    @GetMapping("/users/filter/role")
    @Operation(summary = "Filter by role", description = "Get users by role")
    public ResponseEntity<ApiResponse<Page<AdminUserListDTO>>> getUsersByRole(
            @RequestParam UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminUserListDTO> users = adminService.getUsersByRole(role, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Users filtered by role", users));
    }

    /**
     * Filter users by active status
     *
     * GET /api/admin/users/filter/status?active=true
     */
    @GetMapping("/users/filter/status")
    @Operation(summary = "Filter by status", description = "Get active or suspended users")
    public ResponseEntity<ApiResponse<Page<AdminUserListDTO>>> getUsersByStatus(
            @RequestParam boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminUserListDTO> users = adminService.getUsersByActiveStatus(active, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Users filtered by status", users));
    }

    /**
     * Get homeless users
     *
     * GET /api/admin/users/filter/homeless
     */
    @GetMapping("/users/filter/homeless")
    @Operation(summary = "Get homeless users", description = "List all homeless veterans")
    public ResponseEntity<ApiResponse<Page<AdminUserListDTO>>> getHomelessUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminUserListDTO> users = adminService.getHomelessUsers(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Homeless users retrieved", users));
    }

    /**
     * Get user details
     *
     * GET /api/admin/users/{id}
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user details", description = "Get complete user information")
    public ResponseEntity<ApiResponse<AdminUserDetailDTO>> getUserDetails(
            @PathVariable UUID userId) {

        log.debug("Admin fetching user details: {}", userId);

        AdminUserDetailDTO user = adminService.getUserDetails(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User details retrieved", user));
    }

    /**
     * Update user role
     *
     * PUT /api/admin/users/{id}/role
     * Body: { "role": "ADMIN" }
     */
    @PutMapping("/users/{userId}/role")
    @Operation(summary = "Update user role", description = "Change user role (promote/demote)")
    public ResponseEntity<ApiResponse<AdminUserDetailDTO>> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        log.info("Admin updating user role: {} to {}", userId, request.getRole());

        AdminUserDetailDTO updatedUser = adminService.updateUserRole(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("User role updated successfully", updatedUser));
    }

    /**
     * Suspend user
     *
     * POST /api/admin/users/{id}/suspend
     * Body: { "reason": "Violation of terms" }
     */
    @PostMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspend user", description = "Suspend user account with reason")
    public ResponseEntity<ApiResponse<AdminUserDetailDTO>> suspendUser(
            @PathVariable UUID userId,
            @Valid @RequestBody SuspendUserRequest request) {

        log.info("Admin suspending user: {}", userId);

        AdminUserDetailDTO updatedUser = adminService.suspendUser(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("User suspended successfully", updatedUser));
    }

    /**
     * Activate user
     *
     * POST /api/admin/users/{id}/activate
     */
    @PostMapping("/users/{userId}/activate")
    @Operation(summary = "Activate user", description = "Reactivate suspended user")
    public ResponseEntity<ApiResponse<AdminUserDetailDTO>> activateUser(
            @PathVariable UUID userId) {

        log.info("Admin activating user: {}", userId);

        AdminUserDetailDTO updatedUser = adminService.activateUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User activated successfully", updatedUser));
    }

    /**
     * Update user information
     *
     * PUT /api/admin/users/{id}
     */
    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<ApiResponse<AdminUserDetailDTO>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Admin updating user: {}", userId);

        AdminUserDetailDTO updatedUser = adminService.updateUser(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", updatedUser));
    }

    /**
     * Delete user
     *
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", description = "Permanently delete user (use with caution)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {

        log.warn("Admin deleting user: {}", userId);

        adminService.deleteUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully"));
    }

    /**
     * Get system statistics
     *
     * GET /api/admin/stats
     */
    @GetMapping("/stats")
    @Operation(summary = "System statistics", description = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<AdminStatsDTO>> getSystemStats() {

        log.debug("Admin fetching system statistics");

        AdminStatsDTO stats = adminService.getSystemStats();

        return ResponseEntity.ok(
                ApiResponse.success("Statistics retrieved successfully", stats));
    }
}