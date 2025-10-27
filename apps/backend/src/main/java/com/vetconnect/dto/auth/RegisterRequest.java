package com.vetconnect.dto.auth;

import com.vetconnect.model.enums.BranchOfService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration
 *
 * POST /api/auth/register
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be less than 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;

    @NotNull(message = "Branch of service is required")
    private BranchOfService branchOfService;

    // Optional address fields
    @Size(max = 255, message = "Address line 1 must be less than 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must be less than 255 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(min = 2, max = 2, message = "State must be exactly 2 characters")
    private String state;

    @Size(max = 10, message = "Zip code must be less than 10 characters")
    private String zipCode;

    @Builder.Default
    private boolean isHomeless = false;
}