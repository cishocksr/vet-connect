package com.vetconnect.mapper;

import com.vetconnect.dto.admin.AdminUserDetailDTO;
import com.vetconnect.dto.admin.AdminUserListDTO;
import com.vetconnect.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for admin-specific user DTOs
 */
@Component
public class AdminMapper {

    public AdminUserListDTO toListDTO(User user) {
        if (user == null) {
            return null;
        }

        return AdminUserListDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .branchOfService(user.getBranchOfService())
                .role(user.getRole())
                .isActive(user.isActive())
                .isHomeless(user.isHomeless())
                .city(user.getCity())
                .state(user.getState())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public AdminUserDetailDTO toDetailDTO(User user) {
        if (user == null) {
            return null;
        }

        return AdminUserDetailDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .branchOfService(user.getBranchOfService())
                .role(user.getRole())
                .addressLine1(user.getAddressLine1())
                .addressLine2(user.getAddressLine2())
                .city(user.getCity())
                .state(user.getState())
                .zipCode(user.getZipCode())
                .isHomeless(user.isHomeless())
                .isActive(user.isActive())
                .suspendedAt(user.getSuspendedAt())
                .suspendedReason(user.getSuspendedReason())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
}