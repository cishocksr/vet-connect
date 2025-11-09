package com.vetconnect.repository;

import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ResourceRepository Tests")
class ResourceRepositoryTest {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ResourceCategory category;
    private Resource resource;

    @BeforeEach
    void setUp() {
        category = ResourceCategory.builder()
                .name("Housing")
                .description("Housing resources")
                .iconName("home")
                .build();
        category = entityManager.persist(category);

        resource = Resource.builder()
                .category(category)
                .name("VA Housing Assistance")
                .description("Housing assistance for veterans")
                .websiteUrl("https://va.gov")
                .phoneNumber("1-800-123-4567")
                .city("Ashburn")
                .state("VA")
                .zipCode("20147")
                .isNational(false)
                .build();
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save resource successfully")
    void testSaveResource() {
        // Act
        Resource savedResource = resourceRepository.save(resource);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertNotNull(savedResource.getId());
        assertEquals("VA Housing Assistance", savedResource.getName());
        assertNotNull(savedResource.getCreateAt());
    }

    @Test
    @DisplayName("Should find resources by category")
    void testFindByCategory() {
        // Arrange
        resourceRepository.save(resource);

        // Act
        List<Resource> resources = resourceRepository.findByCategory(category);

        // Assert
        assertEquals(1, resources.size());
        assertEquals("VA Housing Assistance", resources.get(0).getName());
    }

    @Test
    @DisplayName("Should find resources by state")
    void testFindByState() {
        // Arrange
        resourceRepository.save(resource);

        Resource mdResource = Resource.builder()
                .category(category)
                .name("Maryland Resource")
                .description("MD resource")
                .state("MD")
                .isNational(false)
                .build();
        resourceRepository.save(mdResource);

        // Act
        List<Resource> vaResources = resourceRepository.findByState("VA");

        // Assert
        assertEquals(1, vaResources.size());
        assertEquals("VA", vaResources.get(0).getState());
    }

    @Test
    @DisplayName("Should find national resources")
    void testFindByIsNationalTrue() {
        // Arrange
        Resource nationalResource = Resource.builder()
                .category(category)
                .name("National Hotline")
                .description("Available nationwide")
                .isNational(true)
                .build();

        resourceRepository.save(resource); // Not national
        resourceRepository.save(nationalResource); // National

        // Act
        List<Resource> nationalResources = resourceRepository.findByIsNationalTrue();

        // Assert
        assertEquals(1, nationalResources.size());
        assertTrue(nationalResources.get(0).getIsNational());
    }

    @Test
    @DisplayName("Should find resources by state or national")
    void testFindByIsNationalTrueOrState() {
        // Arrange
        Resource nationalResource = Resource.builder()
                .category(category)
                .name("National Resource")
                .description("National")
                .isNational(true)
                .build();

        Resource vaResource = Resource.builder()
                .category(category)
                .name("VA Resource")
                .description("Virginia")
                .state("VA")
                .isNational(false)
                .build();

        Resource mdResource = Resource.builder()
                .category(category)
                .name("MD Resource")
                .description("Maryland")
                .state("MD")
                .isNational(false)
                .build();

        resourceRepository.save(nationalResource);
        resourceRepository.save(vaResource);
        resourceRepository.save(mdResource);

        // Act
        List<Resource> vaAvailable = resourceRepository.findByIsNationalTrueOrState("VA");

        // Assert
        assertEquals(2, vaAvailable.size()); // National + VA specific
    }

    @Test
    @DisplayName("Should search resources by keyword")
    void testSearchByKeyword() {
        // Arrange
        Resource housingResource = Resource.builder()
                .category(category)
                .name("Housing Assistance")
                .description("Help with housing")
                .isNational(false)
                .build();

        Resource healthResource = Resource.builder()
                .category(category)
                .name("Health Clinic")
                .description("Medical services")
                .isNational(false)
                .build();

        resourceRepository.save(housingResource);
        resourceRepository.save(healthResource);

        // Act
        List<Resource> results = resourceRepository.searchByKeyword("housing");

        // Assert
        assertEquals(1, results.size());
        assertTrue(results.get(0).getName().toLowerCase().contains("housing"));
    }

    @Test
    @DisplayName("Should search with pagination")
    void testSearchByKeywordPaginated() {
        // Arrange
        for (int i = 0; i < 15; i++) {
            Resource r = Resource.builder()
                    .category(category)
                    .name("Resource " + i)
                    .description("Test resource number " + i)
                    .isNational(false)
                    .build();
            resourceRepository.save(r);
        }

        // Act
        Page<Resource> page = resourceRepository.searchByKeywordPaginated(
                "resource",
                PageRequest.of(0, 10)
        );

        // Assert
        assertEquals(10, page.getContent().size());
        assertTrue(page.getTotalElements() >= 15);
    }

    @Test
    @DisplayName("Should find resources by category and location")
    void testFindByCategoryAndLocation() {
        // Arrange
        Resource vaHousing = Resource.builder()
                .category(category)
                .name("VA Housing")
                .description("Housing in VA")
                .state("VA")
                .isNational(false)
                .build();

        Resource nationalHousing = Resource.builder()
                .category(category)
                .name("National Housing")
                .description("National housing")
                .isNational(true)
                .build();

        resourceRepository.save(vaHousing);
        resourceRepository.save(nationalHousing);

        // Act
        List<Resource> results = resourceRepository.findByCategoryAndLocation(category, "VA");

        // Assert
        assertEquals(2, results.size()); // Both VA-specific and national
    }
}