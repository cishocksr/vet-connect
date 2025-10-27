package com.vetconnect.util;

import com.vetconnect.dto.auth.RegisterRequest;
import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;

import java.util.UUID;

/**
 * Utility class for building test data
 */
public class TestDataBuilder {

    public static User buildTestUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isHomeless(false)
                .build();
    }

    public static User buildTestUser(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .zipCode("20147")
                .build();
    }

    public static RegisterRequest buildRegisterRequest() {
        return RegisterRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isHomeless(false)
                .build();
    }

    public static ResourceCategory buildTestCategory() {
        return ResourceCategory.builder()
                .id(1)
                .name("Housing")
                .description("Housing resources")
                .iconName("home")
                .build();
    }

    public static Resource buildTestResource(ResourceCategory category) {
        return Resource.builder()
                .id(UUID.randomUUID())
                .category(category)
                .name("Test Resource")
                .description("Test description")
                .websiteUrl("https://test.com")
                .phoneNumber("123-456-7890")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isNational(false)
                .build();
    }
}