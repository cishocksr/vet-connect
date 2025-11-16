package com.vetconnect.dto.user;

import com.vetconnect.model.enums.BranchOfService;
import com.vetconnect.model.enums.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for user DTOs
 */
class UserDTOsTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== UserDTO Tests ==========

    @Test
    void userDTO_getFormattedAddress_withCompleteAddress_shouldReturnFullAddress() {
        UserDTO dto = UserDTO.builder()
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .hasCompleteAddress(true)
                .build();

        assertEquals("123 Main St, Apt 4B, Ashburn, VA 20147", dto.getFormattedAddress());
    }

    @Test
    void userDTO_getFormattedAddress_withoutAddressLine2_shouldSkipLine2() {
        UserDTO dto = UserDTO.builder()
                .addressLine1("123 Main St")
                .addressLine2(null)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .hasCompleteAddress(true)
                .build();

        assertEquals("123 Main St, Ashburn, VA 20147", dto.getFormattedAddress());
    }

    @Test
    void userDTO_getFormattedAddress_withBlankAddressLine2_shouldSkipLine2() {
        UserDTO dto = UserDTO.builder()
                .addressLine1("123 Main St")
                .addressLine2("   ")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .hasCompleteAddress(true)
                .build();

        assertEquals("123 Main St, Ashburn, VA 20147", dto.getFormattedAddress());
    }

    @Test
    void userDTO_getFormattedAddress_withIncompleteAddress_shouldReturnDefault() {
        UserDTO dto = UserDTO.builder()
                .hasCompleteAddress(false)
                .build();

        assertEquals("Address not provided", dto.getFormattedAddress());
    }

    @Test
    void userDTO_builder_shouldSetAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        UserDTO dto = UserDTO.builder()
                .id(id)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .branchOfService(BranchOfService.ARMY)
                .branchDisplayName("Army")
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isHomeless(false)
                .profilePictureUrl("https://example.com/pic.jpg")
                .role(UserRole.USER)
                .hasCompleteAddress(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(id, dto.getId());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("John Doe", dto.getFullName());
        assertEquals(BranchOfService.ARMY, dto.getBranchOfService());
        assertEquals("Army", dto.getBranchDisplayName());
        assertEquals("123 Main St", dto.getAddressLine1());
        assertEquals("Apt 4B", dto.getAddressLine2());
        assertEquals("Ashburn", dto.getCity());
        assertEquals("VA", dto.getState());
        assertEquals("20147", dto.getZipCode());
        assertFalse(dto.isHomeless());
        assertEquals("https://example.com/pic.jpg", dto.getProfilePictureUrl());
        assertEquals(UserRole.USER, dto.getRole());
        assertTrue(dto.isHasCompleteAddress());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    // ========== UserProfileDTO Tests ==========

    @Test
    void userProfileDTO_getLocationDisplay_withCityAndState_shouldReturnFormatted() {
        UserProfileDTO dto = UserProfileDTO.builder()
                .city("Ashburn")
                .state("VA")
                .build();

        assertEquals("Ashburn, VA", dto.getLocationDisplay());
    }

    @Test
    void userProfileDTO_getLocationDisplay_withNullCity_shouldReturnDefault() {
        UserProfileDTO dto = UserProfileDTO.builder()
                .city(null)
                .state("VA")
                .build();

        assertEquals("Location not provided", dto.getLocationDisplay());
    }

    @Test
    void userProfileDTO_getLocationDisplay_withNullState_shouldReturnDefault() {
        UserProfileDTO dto = UserProfileDTO.builder()
                .city("Ashburn")
                .state(null)
                .build();

        assertEquals("Location not provided", dto.getLocationDisplay());
    }

    @Test
    void userProfileDTO_getLocationDisplay_withBothNull_shouldReturnDefault() {
        UserProfileDTO dto = UserProfileDTO.builder()
                .city(null)
                .state(null)
                .build();

        assertEquals("Location not provided", dto.getLocationDisplay());
    }

    @Test
    void userProfileDTO_builder_shouldSetAllFields() {
        UUID id = UUID.randomUUID();

        UserProfileDTO dto = UserProfileDTO.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .branchOfService(BranchOfService.NAVY)
                .branchDisplayName("Navy")
                .profilePictureUrl("https://example.com/pic.jpg")
                .city("Norfolk")
                .state("VA")
                .build();

        assertEquals(id, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("John Doe", dto.getFullName());
        assertEquals(BranchOfService.NAVY, dto.getBranchOfService());
        assertEquals("Navy", dto.getBranchDisplayName());
        assertEquals("https://example.com/pic.jpg", dto.getProfilePictureUrl());
        assertEquals("Norfolk", dto.getCity());
        assertEquals("VA", dto.getState());
    }

    // ========== UpdatePasswordRequest Tests ==========

    @Test
    void updatePasswordRequest_withValidData_shouldPassValidation() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("OldPass123!")
                .newPassword("NewPass456!")
                .confirmNewPassword("NewPass456!")
                .build();

        Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void updatePasswordRequest_withBlankCurrentPassword_shouldFailValidation() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("")
                .newPassword("NewPass456!")
                .confirmNewPassword("NewPass456!")
                .build();

        Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Current password is required")));
    }

    @Test
    void updatePasswordRequest_withBlankNewPassword_shouldFailValidation() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("OldPass123!")
                .newPassword("")
                .confirmNewPassword("NewPass456!")
                .build();

        Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void updatePasswordRequest_withShortNewPassword_shouldFailValidation() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("OldPass123!")
                .newPassword("Short1!")
                .confirmNewPassword("Short1!")
                .build();

        Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password must be at least 8 characters")));
    }

    @Test
    void updatePasswordRequest_withBlankConfirmPassword_shouldFailValidation() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("OldPass123!")
                .newPassword("NewPass456!")
                .confirmNewPassword("")
                .build();

        Set<ConstraintViolation<UpdatePasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Password confirmation is required")));
    }

    @Test
    void updatePasswordRequest_passwordsMatch_withMatchingPasswords_shouldReturnTrue() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .newPassword("NewPass456!")
                .confirmNewPassword("NewPass456!")
                .build();

        assertTrue(request.passwordsMatch());
    }

    @Test
    void updatePasswordRequest_passwordsMatch_withDifferentPasswords_shouldReturnFalse() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .newPassword("NewPass456!")
                .confirmNewPassword("DifferentPass789!")
                .build();

        assertFalse(request.passwordsMatch());
    }

    @Test
    void updatePasswordRequest_passwordsMatch_withNullNewPassword_shouldReturnFalse() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .newPassword(null)
                .confirmNewPassword("NewPass456!")
                .build();

        assertFalse(request.passwordsMatch());
    }

    @Test
    void updatePasswordRequest_isNewPasswordDifferent_withDifferentPasswords_shouldReturnTrue() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("OldPass123!")
                .newPassword("NewPass456!")
                .build();

        assertTrue(request.isNewPasswordDifferent());
    }

    @Test
    void updatePasswordRequest_isNewPasswordDifferent_withSamePassword_shouldReturnFalse() {
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .currentPassword("SamePass123!")
                .newPassword("SamePass123!")
                .build();

        assertFalse(request.isNewPasswordDifferent());
    }

    // ========== UpdateAddressRequest Tests ==========

    @Test
    void updateAddressRequest_withValidCompleteAddress_shouldPassValidation() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1("123 Main St")
                .addressLine2("Apt 4B")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isHomeless(false)
                .build();

        Set<ConstraintViolation<UpdateAddressRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void updateAddressRequest_withTooLongAddressLine1_shouldFailValidation() {
        String longAddress = "a".repeat(256);
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1(longAddress)
                .build();

        Set<ConstraintViolation<UpdateAddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Address line 1 must be less than 255 characters")));
    }

    @Test
    void updateAddressRequest_withTooLongCity_shouldFailValidation() {
        String longCity = "a".repeat(101);
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .city(longCity)
                .build();

        Set<ConstraintViolation<UpdateAddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("City must be less than 100 characters")));
    }

    @Test
    void updateAddressRequest_withInvalidStateLength_shouldFailValidation() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .state("VAA")
                .build();

        Set<ConstraintViolation<UpdateAddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("State must be exactly 2 characters")));
    }

    @Test
    void updateAddressRequest_withSingleCharacterState_shouldFailValidation() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .state("V")
                .build();

        Set<ConstraintViolation<UpdateAddressRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void updateAddressRequest_isClearingAddress_withAllNulls_shouldReturnTrue() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1(null)
                .addressLine2(null)
                .city(null)
                .state(null)
                .zipCode(null)
                .build();

        assertTrue(request.isClearingAddress());
    }

    @Test
    void updateAddressRequest_isClearingAddress_withOneFieldSet_shouldReturnFalse() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1("123 Main St")
                .addressLine2(null)
                .city(null)
                .state(null)
                .zipCode(null)
                .build();

        assertFalse(request.isClearingAddress());
    }

    @Test
    void updateAddressRequest_isCompleteAddress_withAllRequiredFields_shouldReturnTrue() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1("123 Main St")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .build();

        assertTrue(request.isCompleteAddress());
    }

    @Test
    void updateAddressRequest_isCompleteAddress_withMissingAddressLine1_shouldReturnFalse() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1(null)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .build();

        assertFalse(request.isCompleteAddress());
    }

    @Test
    void updateAddressRequest_isCompleteAddress_withBlankAddressLine1_shouldReturnFalse() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1("   ")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .build();

        assertFalse(request.isCompleteAddress());
    }

    @Test
    void updateAddressRequest_isCompleteAddress_withMissingCity_shouldReturnFalse() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1("123 Main St")
                .city(null)
                .state("VA")
                .zipCode("20147")
                .build();

        assertFalse(request.isCompleteAddress());
    }

    @Test
    void updateAddressRequest_isCompleteAddress_withMissingState_shouldReturnFalse() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1("123 Main St")
                .city("Ashburn")
                .state(null)
                .zipCode("20147")
                .build();

        assertFalse(request.isCompleteAddress());
    }

    @Test
    void updateAddressRequest_isCompleteAddress_withMissingZipCode_shouldReturnFalse() {
        UpdateAddressRequest request = UpdateAddressRequest.builder()
                .addressLine1("123 Main St")
                .city("Ashburn")
                .state("VA")
                .zipCode(null)
                .build();

        assertFalse(request.isCompleteAddress());
    }

    @Test
    void updateAddressRequest_gettersAndSetters_shouldWork() {
        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setAddressLine1("123 Main St");
        request.setAddressLine2("Apt 4B");
        request.setCity("Ashburn");
        request.setState("VA");
        request.setZipCode("20147");
        request.setIsHomeless(false);

        assertEquals("123 Main St", request.getAddressLine1());
        assertEquals("Apt 4B", request.getAddressLine2());
        assertEquals("Ashburn", request.getCity());
        assertEquals("VA", request.getState());
        assertEquals("20147", request.getZipCode());
        assertFalse(request.getIsHomeless());
    }
}
