package com.vetconnect.repository;

import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests for UserRepository
 *
 * Uses @DataJpaTest for lightweight database testing
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isHomeless(false)
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("Should save user successfully")
    void testSaveUser() {
        // Act
        User savedUser = userRepository.save(testUser);
        userRepository.flush();

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        // Arrange
        userRepository.save(testUser);

        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
        assertEquals("Doe", found.get().getLastName());
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void testFindByEmail_NotFound() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void testExistsByEmail() {
        // Arrange
        userRepository.save(testUser);

        // Act & Assert
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @DisplayName("Should find users by branch of service")
    void testFindByBranchOfService() {
        // Arrange
        User armyUser = User.builder()
                .email("army@example.com")
                .passwordHash("hash")
                .firstName("Army")
                .lastName("Vet")
                .branchOfService(BranchOfService.ARMY)
                .zipCode("12345")
                .isDeleted(false)
                .build();

        User navyUser = User.builder()
                .email("navy@example.com")
                .passwordHash("hash")
                .firstName("Navy")
                .lastName("Vet")
                .branchOfService(BranchOfService.NAVY)
                .zipCode("12345")
                .isDeleted(false)
                .build();

        userRepository.save(armyUser);
        userRepository.save(navyUser);

        // Act
        List<User> armyUsers = userRepository.findByBranchOfService(BranchOfService.ARMY);

        // Assert
        assertEquals(1, armyUsers.size());
        assertEquals("Army", armyUsers.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should find homeless users")
    void testFindByIsHomelessTrue() {
        // Arrange
        User homelessUser = User.builder()
                .email("homeless@example.com")
                .passwordHash("hash")
                .firstName("John")
                .lastName("Smith")
                .branchOfService(BranchOfService.MARINES)
                .isHomeless(true)
                .zipCode("12345")
                .isDeleted(false)
                .build();

        userRepository.save(testUser); // Not homeless
        userRepository.save(homelessUser); // Homeless

        // Act
        List<User> homelessUsers = userRepository.findByIsHomelessTrue();

        // Assert
        assertEquals(1, homelessUsers.size());
        assertEquals("homeless@example.com", homelessUsers.get(0).getEmail());
    }

    @Test
    @DisplayName("Should find users by state")
    void testFindByState() {
        // Arrange
        User vaUser = User.builder()
                .email("va@example.com")
                .passwordHash("hash")
                .firstName("Virginia")
                .lastName("User")
                .branchOfService(BranchOfService.AIR_FORCE)
                .state("VA")
                .zipCode("12345")
                .isDeleted(false)
                .build();

        User mdUser = User.builder()
                .email("md@example.com")
                .passwordHash("hash")
                .firstName("Maryland")
                .lastName("User")
                .branchOfService(BranchOfService.COAST_GUARD)
                .state("MD")
                .zipCode("12345")
                .isDeleted(false)
                .build();

        userRepository.save(vaUser);
        userRepository.save(mdUser);

        // Act
        List<User> vaUsers = userRepository.findByState("VA");

        // Assert
        assertEquals(1, vaUsers.size());
        assertEquals("VA", vaUsers.get(0).getState());
    }

    @Test
    @DisplayName("Should search users by name")
    void testSearchByName() {
        // Arrange
        User johnDoe = User.builder()
                .email("john@example.com")
                .passwordHash("hash")
                .firstName("John")
                .lastName("Doe")
                .branchOfService(BranchOfService.ARMY)
                .zipCode("12345")
                .isDeleted(false)
                .build();

        User janeSmith = User.builder()
                .email("jane@example.com")
                .passwordHash("hash")
                .firstName("Jane")
                .lastName("Smith")
                .branchOfService(BranchOfService.NAVY)
                .zipCode("12345")
                .isDeleted(false)
                .build();

        userRepository.save(johnDoe);
        userRepository.save(janeSmith);

        // Act
        List<User> results = userRepository.searchByName("john");

        // Assert
        assertEquals(1, results.size());
        assertEquals("John", results.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void testUniqueEmailConstraint() {
        // Arrange
        userRepository.save(testUser);

        User duplicateEmailUser = User.builder()
                .email("test@example.com") // Same email
                .passwordHash("differentHash")
                .firstName("Different")
                .lastName("Person")
                .branchOfService(BranchOfService.SPACE_FORCE)
                .zipCode("12345")
                .isDeleted(false)
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateEmailUser);
            userRepository.flush(); // Force the constraint check
        });
    }
}