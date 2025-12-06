package com.vetconnect.mapper;

import com.vetconnect.dto.resource.*;
import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceMapperTest {

    @Mock
    private ResourceCategoryMapper categoryMapper;

    @InjectMocks
    private ResourceMapper resourceMapper;

    private Resource testResource;
    private ResourceCategory testCategory;
    private ResourceCategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        // Setup test category - uses Integer ID
        testCategory = new ResourceCategory();
        testCategory.setId(1);
        testCategory.setName("Healthcare");
        testCategory.setDescription("Healthcare resources");
        testCategory.setIconName("health");

        testCategoryDTO = ResourceCategoryDTO.builder()
                .id(1)
                .name("Healthcare")
                .description("Healthcare resources")
                .iconName("health")
                .build();

        // Setup test resource - uses UUID ID
        testResource = Resource.builder()
                .id(UUID.randomUUID())
                .name("VA Medical Center")
                .description("Full-service VA medical center providing comprehensive healthcare")
                .category(testCategory)
                .websiteUrl("https://www.va.gov/detroit")
                .phoneNumber("313-576-1000")
                .email("detroit.vamc@va.gov")
                .addressLine1("4646 John R Street")
                .city("Detroit")
                .state("MI")
                .zipCode("48201")
                .isNational(false)
                .eligibilityCriteria("Must be an enrolled veteran")
                .createAt(LocalDateTime.now())
                .build();
    }

    @Test
    void toDTO_WithCompleteResource_ShouldMapAllFields() {
        when(categoryMapper.toDTO(any(ResourceCategory.class))).thenReturn(testCategoryDTO);

        ResourceDTO result = resourceMapper.toDTO(testResource);

        assertNotNull(result);
        assertEquals(testResource.getId(), result.getId());
        assertEquals(testResource.getName(), result.getName());
        assertEquals(testResource.getDescription(), result.getDescription());
        assertEquals(testResource.getWebsiteUrl(), result.getWebsiteUrl());
        assertEquals(testResource.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(testResource.getEmail(), result.getEmail());
        assertEquals(testResource.getAddressLine1(), result.getAddressLine1());
        assertEquals(testResource.getCity(), result.getCity());
        assertEquals(testResource.getState(), result.getState());
        assertEquals(testResource.getZipCode(), result.getZipCode());
        // Note: ResourceDTO.isNational is primitive boolean, uses isNational() not getIsNational()
        assertEquals(testResource.getIsNational(), result.isNational());
        assertEquals(testResource.getEligibilityCriteria(), result.getEligibilityCriteria());
        assertNotNull(result.getCategory());
    }

    @Test
    void toDTO_WithNullResource_ShouldReturnNull() {
        ResourceDTO result = resourceMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void toSummaryDTO_ShouldMapEssentialFields() {
        ResourceSummaryDTO result = resourceMapper.toSummaryDTO(testResource);

        assertNotNull(result);
        assertEquals(testResource.getId(), result.getId());
        assertEquals(testResource.getName(), result.getName());
        assertNotNull(result.getShortDescription());
        assertEquals("Healthcare", result.getCategoryName());
        assertEquals("health", result.getCategoryIconName());
    }

    @Test
    void toSummaryDTO_WithNullResource_ShouldReturnNull() {
        ResourceSummaryDTO result = resourceMapper.toSummaryDTO(null);
        assertNull(result);
    }

    @Test
    void toDTOList_ShouldMapAllResources() {
        Resource resource2 = Resource.builder()
                .id(UUID.randomUUID())
                .name("Job Center")
                .description("Employment assistance")
                .category(testCategory)
                .isNational(false)
                .build();

        when(categoryMapper.toDTO(any(ResourceCategory.class))).thenReturn(testCategoryDTO);

        List<Resource> resources = Arrays.asList(testResource, resource2);
        List<ResourceDTO> result = resourceMapper.toDTOList(resources);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void toDTOList_WithNullList_ShouldReturnNull() {
        List<ResourceDTO> result = resourceMapper.toDTOList(null);
        assertNull(result);
    }

    @Test
    void toSummaryDTOList_ShouldMapAllResources() {
        Resource resource2 = Resource.builder()
                .id(UUID.randomUUID())
                .name("Education Center")
                .description("Educational resources")
                .category(testCategory)
                .isNational(true)
                .build();

        List<Resource> resources = Arrays.asList(testResource, resource2);
        List<ResourceSummaryDTO> result = resourceMapper.toSummaryDTOList(resources);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void toEntity_FromCreateRequest_ShouldMapAllFields() {
        CreateResourceRequest request = new CreateResourceRequest();
        request.setName("New Resource");
        request.setDescription("New description");
        request.setWebsiteUrl("https://example.com");
        request.setPhoneNumber("555-1234");
        request.setNational(false);

        Resource result = resourceMapper.toEntity(request, testCategory);

        assertNotNull(result);
        assertEquals(request.getName(), result.getName());
        assertEquals(testCategory, result.getCategory());
    }

    @Test
    void toEntity_WithNullRequest_ShouldReturnNull() {
        Resource result = resourceMapper.toEntity(null, testCategory);
        assertNull(result);
    }

    @Test
    void updateEntityFromDTO_ShouldUpdateProvidedFields() {
        UpdateResourceRequest updateRequest = new UpdateResourceRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated Description");

        resourceMapper.updateEntityFromDTO(testResource, updateRequest, null);

        assertEquals("Updated Name", testResource.getName());
        assertEquals("Updated Description", testResource.getDescription());
    }

    @Test
    void updateEntityFromDTO_WithNullRequest_ShouldNotModifyEntity() {
        String originalName = testResource.getName();

        resourceMapper.updateEntityFromDTO(testResource, null, null);

        assertEquals(originalName, testResource.getName());
    }
}