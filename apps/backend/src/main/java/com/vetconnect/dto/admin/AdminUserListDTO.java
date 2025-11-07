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
 * Admin user list view - condensed user info for admin dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserListDTO {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private BranchOfService branchOfService;
    private UserRole role;
    private boolean isActive;
    private boolean isHomeless;
    private String city;
    private String state;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}