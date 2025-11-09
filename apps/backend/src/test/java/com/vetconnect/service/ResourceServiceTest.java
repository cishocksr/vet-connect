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
import com.vetconnect.util.InputSanitizer;
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

    @Mock
    private ResourceCategoryService categoryService;

    @Mock
    private InputSanitizer inputSanitizer;

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
        assertThrows(RuntimeException.class,
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

        when(categoryService.getCategoryEntityById(1)).thenReturn(category);
        when(inputSanitizer.sanitizeHtml(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(resourceMapper.toEntity(any(CreateResourceRequest.class), eq(category))).thenReturn(resource);
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);
        when(resourceMapper.toDTO(resource)).thenReturn(resourceDTO);

        // Act
        ResourceDTO result = resourceService.createResource(request);

        // Assert
        assertNotNull(result);
        verify(categoryService).getCategoryEntityById(1);
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

        when(categoryService.getCategoryEntityById(1)).thenReturn(category);
        when(inputSanitizer.sanitizeHtml(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(resourceMapper.toEntity(any(CreateResourceRequest.class), eq(category))).thenReturn(nationalResource);
        when(resourceRepository.save(any(Resource.class))).thenReturn(nationalResource);
        when(resourceMapper.toDTO(nationalResource)).thenReturn(resourceDTO);

        // Act
        ResourceDTO result = resourceService.createResource(request);

        // Assert
        assertNotNull(result);
        verify(categoryService).getCategoryEntityById(1);
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should get resources by state including national resources")
    void testGetResourcesByState() {
        // Arrange
        List<Resource> resources = Arrays.asList(resource);
        Page<Resource> page = new PageImpl<>(resources);

        when(resourceRepository.findByIsNationalTrueOrState(eq("VA"), any(Pageable.class))).thenReturn(page);
        when(resourceMapper.toSummaryDTO(any(Resource.class))).thenReturn(resourceSummaryDTO);

        // Act
        PageResponse<ResourceSummaryDTO> result = resourceService.getResourcesByState("VA", 0, 20);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(resourceRepository).findByIsNationalTrueOrState(eq("VA"), any(Pageable.class));
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
        List<ResourceDTO> dtoList = Arrays.asList(resourceDTO);

        when(resourceRepository.findByIsNationalTrue()).thenReturn(resources);
        when(resourceMapper.toDTOList(resources)).thenReturn(dtoList);

        // Act
        List<ResourceDTO> result = resourceService.getNationalResources();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(resourceRepository).findByIsNationalTrue();
        verify(resourceMapper).toDTOList(resources);
    }

    @Test
    @DisplayName("Should delete resource successfully")
    void testDeleteResource_Success() {
        // Arrange
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        doNothing().when(resourceRepository).delete(resource);

        // Act
        resourceService.deleteResource(resourceId);

        // Assert
        verify(resourceRepository).findById(resourceId);
        verify(resourceRepository).delete(resource);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent resource")
    void testDeleteResource_NotFound() {
        // Arrange
        when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> resourceService.deleteResource(resourceId));

        verify(resourceRepository).findById(resourceId);
        verify(resourceRepository, never()).delete(any());
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

    @Test
    @DisplayName("Should sanitize resource name and description before saving")
    void testCreateResource_SanitizesNameAndDescription() {
        // Arrange - Create request with potentially malicious inputs
        CreateResourceRequest maliciousRequest = CreateResourceRequest.builder()
                .categoryId(1)
                .name("Housing <script>alert('xss')</script> Resource")
                .description("Description with <img src=x onerror=alert('xss')> malicious content")
                .phoneNumber("1-800-123-4567")
                .state("VA")
                .city("Ashburn")
                .isNational(false)
                .build();

        String sanitizedName = "Housing &lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt; Resource";
        String sanitizedDescription = "Description with &lt;img src=x onerror=alert(&#39;xss&#39;)&gt; malicious content";

        when(categoryService.getCategoryEntityById(1)).thenReturn(category);
        when(inputSanitizer.sanitizeHtml(maliciousRequest.getName())).thenReturn(sanitizedName);
        when(inputSanitizer.sanitizeHtml(maliciousRequest.getDescription())).thenReturn(sanitizedDescription);
        when(inputSanitizer.sanitizeHtml(maliciousRequest.getCity())).thenReturn("Ashburn");
        when(resourceMapper.toEntity(any(CreateResourceRequest.class), eq(category))).thenReturn(resource);
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);
        when(resourceMapper.toDTO(resource)).thenReturn(resourceDTO);

        // Act
        ResourceDTO result = resourceService.createResource(maliciousRequest);

        // Assert
        assertNotNull(result);

        // Verify sanitization was called for text fields (verify with original unsanitized values)
        verify(inputSanitizer).sanitizeHtml("Housing <script>alert('xss')</script> Resource");
        verify(inputSanitizer).sanitizeHtml("Description with <img src=x onerror=alert('xss')> malicious content");
        verify(inputSanitizer).sanitizeHtml("Ashburn");

        // Verify resource was saved
        verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should sanitize all text fields in resource creation")
    void testCreateResource_SanitizesAllTextFields() {
        // Arrange - Request with all text fields populated
        CreateResourceRequest request = CreateResourceRequest.builder()
                .categoryId(1)
                .name("Test <b>Resource</b>")
                .description("Test <i>Description</i>")
                .addressLine1("123 Main St<script>")
                .city("Ashburn<script>")
                .zipCode("20147<script>")
                .phoneNumber("1-800-123-4567")
                .state("VA")
                .isNational(false)
                .build();

        when(categoryService.getCategoryEntityById(1)).thenReturn(category);
        when(inputSanitizer.sanitizeHtml(anyString())).thenAnswer(invocation ->
                invocation.getArgument(0).toString().replace("<", "&lt;").replace(">", "&gt;"));
        when(resourceMapper.toEntity(any(CreateResourceRequest.class), eq(category))).thenReturn(resource);
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);
        when(resourceMapper.toDTO(resource)).thenReturn(resourceDTO);

        // Act
        resourceService.createResource(request);

        // Assert - Verify sanitization was called for all text fields (verify with original values)
        verify(inputSanitizer).sanitizeHtml("Test <b>Resource</b>");
        verify(inputSanitizer).sanitizeHtml("Test <i>Description</i>");
        verify(inputSanitizer).sanitizeHtml("123 Main St<script>");
        verify(inputSanitizer).sanitizeHtml("Ashburn<script>");
        verify(inputSanitizer).sanitizeHtml("20147<script>");
    }

    @Test
    @DisplayName("Should handle null optional fields without sanitization during create")
    void testCreateResource_HandlesNullOptionalFields() {
        // Arrange - Request without optional fields
        CreateResourceRequest minimalRequest = CreateResourceRequest.builder()
                .categoryId(1)
                .name("Simple Resource")
                .description("Simple Description")
                .phoneNumber("1-800-123-4567")
                .isNational(true)
                .build();

        when(categoryService.getCategoryEntityById(1)).thenReturn(category);
        when(inputSanitizer.sanitizeHtml("Simple Resource")).thenReturn("Simple Resource");
        when(inputSanitizer.sanitizeHtml("Simple Description")).thenReturn("Simple Description");
        when(resourceMapper.toEntity(any(CreateResourceRequest.class), eq(category))).thenReturn(resource);
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);
        when(resourceMapper.toDTO(resource)).thenReturn(resourceDTO);

        // Act
        resourceService.createResource(minimalRequest);

        // Assert - Verify sanitization was only called for non-null fields
        verify(inputSanitizer).sanitizeHtml("Simple Resource");
        verify(inputSanitizer).sanitizeHtml("Simple Description");
        // Should only be called twice (name and description)
        verify(inputSanitizer, times(2)).sanitizeHtml(anyString());
    }

    @Test
    @DisplayName("Should sanitize complex XSS attempts in resource fields")
    void testCreateResource_SanitizesComplexXSS() {
        // Arrange - Various XSS attack vectors
        String xssName = "Resource<script>document.cookie</script>";
        String xssDescription = "Description<img src=x onerror=\"fetch('evil.com')\"";
        String sanitizedName = "Resource&lt;script&gt;document.cookie&lt;/script&gt;";
        String sanitizedDescription = "Description&lt;img src=x onerror=&quot;fetch(&#39;evil.com&#39;)&quot;";

        CreateResourceRequest request = CreateResourceRequest.builder()
                .categoryId(1)
                .name(xssName)
                .description(xssDescription)
                .phoneNumber("1-800-123-4567")
                .state("VA")
                .isNational(false)
                .build();

        when(categoryService.getCategoryEntityById(1)).thenReturn(category);
        when(inputSanitizer.sanitizeHtml(xssName)).thenReturn(sanitizedName);
        when(inputSanitizer.sanitizeHtml(xssDescription)).thenReturn(sanitizedDescription);
        when(resourceMapper.toEntity(any(CreateResourceRequest.class), eq(category))).thenReturn(resource);
        when(resourceRepository.save(any(Resource.class))).thenReturn(resource);
        when(resourceMapper.toDTO(resource)).thenReturn(resourceDTO);

        // Act
        resourceService.createResource(request);

        // Assert
        verify(inputSanitizer).sanitizeHtml(xssName);
        verify(inputSanitizer).sanitizeHtml(xssDescription);

        // Verify the request object was modified with sanitized values
        assertEquals(sanitizedName, request.getName());
        assertEquals(sanitizedDescription, request.getDescription());
    }
}
