package com.vetconnect.controller;

import com.vetconnect.dto.common.ApiResponse;
import com.vetconnect.dto.user.*;
import com.vetconnect.security.CustomUserDetails;
import com.vetconnect.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Controller
 *
 * ENDPOINTS:
 * - GET /api/users/profile - Get current user profile
 * - GET /api/users/{userId} - Get public user profile
 * - PUT /api/users/profile - Update user profile
 * - PATCH /api/users/address - Update address
 * - PUT /api/users/password - Change password
 *
 * ALL ENDPOINTS REQUIRE AUTHENTICATION
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Get current user's profile
     *
     * GET /api/users/profile
     *
     * Headers:
     * Authorization: Bearer <token>
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "User profile retrieved successfully",
     *   "data": {
     *     "id": "123e4567-e89b-12d3-a456-426614174000",
     *     "email": "veteran@example.com",
     *     "firstName": "John",
     *     "lastName": "Doe",
     *     "fullName": "John Doe",
     *     "branchOfService": "ARMY",
     *     "branchDisplayName": "Army",
     *     ...
     *   }
     * }
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile",
            description = "Get complete profile of authenticated user")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        log.debug("Getting profile for user: {}", currentUser.getId());

        UserDTO userDTO = userService.getUserById(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.success("User profile retrieved successfully", userDTO)
        );
    }

    /**
     * Get public user profile by ID
     *
     * GET /api/users/{userId}
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "User profile retrieved successfully",
     *   "data": {
     *     "id": "123e4567-e89b-12d3-a456-426614174000",
     *     "firstName": "John",
     *     "lastName": "Doe",
     *     "fullName": "John Doe",
     *     "branchOfService": "ARMY",
     *     "branchDisplayName": "Army",
     *     "city": "Ashburn",
     *     "state": "VA"
     *   }
     * }
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get public user profile",
            description = "Get limited public profile of any user")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfile(
            @PathVariable UUID userId) {

        log.debug("Getting public profile for user: {}", userId);

        UserProfileDTO profileDTO = userService.getUserProfile(userId);

        return ResponseEntity.ok(
                ApiResponse.success("User profile retrieved successfully", profileDTO)
        );
    }

    /**
     * Update user profile
     *
     * PUT /api/users/profile
     *
     * Request body (all fields optional):
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "branchOfService": "ARMY",
     *   "addressLine1": "123 Main St",
     *   "addressLine2": "Apt 4B",
     *   "city": "Ashburn",
     *   "state": "VA",
     *   "zipCode": "20147",
     *   "isHomeless": false
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Profile updated successfully",
     *   "data": { ...updated user... }
     * }
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile",
            description = "Update profile information (partial update supported)")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateUserRequest updateRequest) {

        log.info("Updating profile for user: {}", currentUser.getId());

        UserDTO updatedUser = userService.updateUserProfile(currentUser.getId(), updateRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updatedUser)
        );
    }

    /**
     * Update user address
     *
     * PATCH /api/users/address
     *
     * Request body:
     * {
     *   "addressLine1": "123 Main St",
     *   "addressLine2": "Apt 4B",
     *   "city": "Ashburn",
     *   "state": "VA",
     *   "zipCode": "20147",
     *   "isHomeless": false
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Address updated successfully",
     *   "data": { ...updated user... }
     * }
     */
    @PatchMapping("/address")
    @Operation(summary = "Update user address",
            description = "Update address information specifically")
    public ResponseEntity<ApiResponse<UserDTO>> updateAddress(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdateAddressRequest addressRequest) {

        log.info("Updating address for user: {}", currentUser.getId());

        UserDTO updatedUser = userService.updateUserAddress(currentUser.getId(), addressRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Address updated successfully", updatedUser)
        );
    }

    /**
     * Change password
     *
     * PUT /api/users/password
     *
     * Request body:
     * {
     *   "currentPassword": "OldPassword123!",
     *   "newPassword": "NewSecurePassword456!",
     *   "confirmNewPassword": "NewSecurePassword456!"
     * }
     *
     * Response: 200 OK
     * {
     *   "success": true,
     *   "message": "Password changed successfully"
     * }
     */
    @PutMapping("/password")
    @Operation(summary = "Change password",
            description = "Change user password (requires current password)")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody UpdatePasswordRequest passwordRequest) {

        log.info("Password change request for user: {}", currentUser.getId());

        userService.updatePassword(currentUser.getId(), passwordRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully")
        );
    }
}