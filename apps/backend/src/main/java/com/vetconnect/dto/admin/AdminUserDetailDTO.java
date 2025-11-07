package com.vetconnect.dto.admin;

import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Complete user details for admin view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private BranchOfService branchOfService;
    private UserRole role;

    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private boolean isHomeless;

    // Account status
    private boolean isActive;
    private LocalDateTime suspendedAt;
    private String suspendedReason;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // Profile
    private String profilePictureUrl;
}