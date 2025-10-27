package com.vetconnect.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing user password
 *
 * PUT /api/users/password
 * {
 *   "currentPassword": "OldPassword123!",
 *   "newPassword": "NewSecurePassword456!",
 *   "confirmNewPassword": "NewSecurePassword456!"
 * }
 *
 * SECURITY NOTES:
 * - Requires current password to prevent unauthorized changes
 * - New password must meet minimum security requirements
 * - Confirmation must match new password
 * - This will invalidate all existing JWT tokens (force re-login)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequest {

    /**
     * User's current password (for verification)
     */
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    /**
     * New password
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    // TODO: Add regex pattern for stronger validation
    // @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
    //          message = "Password must contain at least one uppercase, lowercase, number, and special character")
    private String newPassword;

    /**
     * Confirmation of new password (must match newPassword)
     */
    @NotBlank(message = "Password confirmation is required")
    private String confirmNewPassword;

    /**
     * Validate that new passwords match
     * Should be called in service layer before processing
     */
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }

    /**
     * Validate that new password is different from current
     */
    public boolean isNewPasswordDifferent() {
        return !currentPassword.equals(newPassword);
    }
}