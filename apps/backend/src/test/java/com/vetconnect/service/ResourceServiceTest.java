package com.vetconnect.service;

import com.vetconnect.dto.resource.CreateResourceRequest;
import com.vetconnect.dto.resource.ResourceDTO;
import com.vetconnect.dto.resource.ResourceSummaryDTO;
import com.vetconnect.exception.ResourceNotFoundException;
import com.vetconnect.mapper.ResourceMapper;
import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import com.vetconnect.repository.ResourceCategoryRepository;
import com.vetconnect.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.vetconnect.dto.common.PageResponse;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceService Tests")
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceCategoryRepository categoryRepository;

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private ResourceService resourceService;

    private Resource resource;
    private ResourceDTO resourceDTO;
    private ResourceSummaryDTO resourceSummaryDTO;
    private ResourceCategory category;
    private UUID resourceId;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID();

        category = ResourceCategory.builder()
                .id(1)
                .name("Housing")
                .description("Housing resources")
                .iconName("home")
                .build();

        resource = Resource.builder()
                .id(resourceId)
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

        resourceDTO = ResourceDTO.builder()
                .id(resourceId)
                .name("VA Housing Assistance")
                .description("Housing assistance for veterans")
                .websiteUrl("https://va.gov")
                .phoneNumber("1-800-123-4567")
                .isNational(false)
                .build();

        resourceSummaryDTO = ResourceSummaryDTO.builder()
                .id(resourceId)
                .name("VA Housing Assistance")
                .shortDescription("Housing assistance for veterans")
                .categoryName("Housing")
                .categoryIconName("home")
                .locationDisplay("Ashburn, VA")
                .isNational(false)
                .phoneNumber("1-800-123-4567")
                .websiteUrl("https://va.gov")
                .build();
    }

    @Test
    @DisplayName("Should get resource by ID successfully")
    void testGetResourceById_Success() {
        // Arrange
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        when(resourceMapper.toDTO(resource)).thenReturn(resourceDTO);

        // Act
        ResourceDTO result = resourceService.getResourceById(resourceId);

        // Assert
        assertNotNull(result);
        assertEquals(resourceId, result.getId());
        assertEquals("VA Housing Assistance", result.getName());

        verify(resourceRepository).findById(resourceId);
        verify(resourceMapper).toDTO(resource);
    }

    @Test
    @DisplayName("Should throw exception when resource not found")
    void testGetResourceById_NotFound() {
        // Arrange
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.getResourceById(resourceId));

        verify(resourceRepository).findById(resourceId);
        verify(resourceMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Should create local resource successfully with state")
    void testCreateResource_Success() {
        // Arrange - LOCAL resource WITH state (required!)
        CreateResourceRequest request = CreateResourceRequest.builder()
                .categoryId(1)
                .name("New Local Resource")
                .description("Description")
                .phoneNumber("1-800-123-4567")
                .state("VA")
                .city("Ashburn")
                .isNational(false)
                .build();

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);
        when(resourceMapper.toDTO(resource)).thenReturn(resourceDTO);

        // Act
        ResourceDTO result = resourceService.createResource(request);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findById(1);
        verify(resourceRepository).save(any(Resource.class));
        verify(resourceMapper).toDTO(resource);
    }

    @Test
    @DisplayName("Should create national resource successfully without state")
    void testCreateNationalResource_Success() {
        // Arrange - NATIONAL resource (no state required)
        CreateResourceRequest request = CreateResourceRequest.builder()
                .categoryId(1)
                .name("New National Resource")
                .description("Description")
                .phoneNumber("1-800-123-4567")
                .isNational(true)
                .build();

        Resource nationalResource = Resource.builder()
                .id(resourceId)
                .category(category)
                .name("New National Resource")
                .description("Description")
                .phoneNumber("1-800-123-4567")
                .isNational(true)
                .build();

        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(resourceRepository.save(any(Resource.class))).thenReturn(nationalResource);
        when(resourceMapper.toDTO(nationalResource)).thenReturn(resourceDTO);

        // Act
        ResourceDTO result = resourceService.createResource(request);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findById(1);
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should get resources by state including national resources")
    void testGetResourcesByState() {
        // Arrange
        List<Resource> resources = Arrays.asList(resource);
        Page<Resource> page = new PageImpl<>(resources);
        Pageable pageable = PageRequest.of(0, 20);

        when(resourceRepository.findByIsNationalTrueOrState(eq("VA"), eq(pageable))).thenReturn(page);
        when(resourceMapper.toSummaryDTO(any(Resource.class))).thenReturn(resourceSummaryDTO);

        // Act
        PageResponse<ResourceSummaryDTO> result = resourceService.getResourcesByState("VA", 0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(resourceRepository).findByIsNationalTrueOrState(eq("VA"), eq(pageable));
        verify(resourceMapper).toSummaryDTO(resource);
    }

    @Test
    @DisplayName("Should get national resources")
    void testGetNationalResources() {
        // Arrange
        Resource nationalResource = Resource.builder()
                .id(resourceId)
                .category(category)
                .name("VA National Hotline")
                .description("National crisis hotline")
                .phoneNumber("1-800-273-8255")
                .isNational(true)
                .build();

        List<Resource> resources = Arrays.asList(nationalResource);

        when(resourceRepository.findByIsNationalTrue()).thenReturn(resources);
        when(resourceMapper.toDTO(nationalResource)).thenReturn(resourceDTO);

        // Act
        List<ResourceDTO> result = resourceService.getNationalResources();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(resourceRepository).findByIsNationalTrue();
        verify(resourceMapper).toDTO(nationalResource);
    }

    @Test
    @DisplayName("Should delete resource successfully")
    void testDeleteResource_Success() {
        // Arrange
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        doNothing().when(resourceRepository).deleteById(resourceId);

        // Act
        resourceService.deleteResource(resourceId);

        // Assert
        verify(resourceRepository).findById(resourceId);
        verify(resourceRepository).deleteById(resourceId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent resource")
    void testDeleteResource_NotFound() {
        // Arrange
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> resourceService.deleteResource(resourceId));

        verify(resourceRepository).findById(resourceId);
        verify(resourceRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should get all resources paginated")
    void testGetAllResources() {
        // Arrange
        List<Resource> resources = Arrays.asList(resource);
        Page<Resource> page = new PageImpl<>(resources);
        Pageable pageable = PageRequest.of(0, 20);

        when(resourceRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(resourceMapper.toSummaryDTO(any(Resource.class))).thenReturn(resourceSummaryDTO);

        // Act
        PageResponse<ResourceSummaryDTO> result = resourceService.getAllResources(0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(resourceRepository).findAll(any(Pageable.class));
    }
}