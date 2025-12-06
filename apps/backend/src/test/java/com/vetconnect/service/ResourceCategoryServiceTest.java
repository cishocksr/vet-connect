package com.vetconnect.service;

import com.vetconnect.dto.resource.ResourceCategoryDTO;
import com.vetconnect.mapper.ResourceCategoryMapper;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResourceCategoryService Tests")
class ResourceCategoryServiceTest {

    @Mock
    private ResourceCategoryRepository categoryRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private ResourceCategoryMapper categoryMapper;

    @InjectMocks
    private ResourceCategoryService resourceCategoryService;

    private ResourceCategory category;
    private ResourceCategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = ResourceCategory.builder()
                .id(1)
                .name("Housing")
                .description("Housing resources")
                .iconName("home")
                .build();

        categoryDTO = new ResourceCategoryDTO();
        categoryDTO.setId(1);
        categoryDTO.setName("Housing");
        categoryDTO.setDescription("Housing resources");
        categoryDTO.setIconName("home");
    }

    @Test
    @DisplayName("Should get all categories successfully")
    void testGetAllCategories_Success() {
        // Arrange
        List<ResourceCategory> categories = Arrays.asList(category);
        List<ResourceCategoryDTO> expectedDTOs = Arrays.asList(categoryDTO);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toDTOList(categories)).thenReturn(expectedDTOs);

        // Act
        List<ResourceCategoryDTO> result = resourceCategoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Housing", result.get(0).getName());

        verify(categoryRepository).findAll();
        verify(categoryMapper).toDTOList(categories);
    }

    @Test
    @DisplayName("Should get all categories with counts successfully")
    void testGetAllCategoriesWithCounts_Success() {
        // Arrange
        List<ResourceCategory> categories = Arrays.asList(category);
        List<Resource> resources = Arrays.asList(new Resource(), new Resource());
        ResourceCategoryDTO categoryDTOWithCount = new ResourceCategoryDTO();
        categoryDTOWithCount.setId(1);
        categoryDTOWithCount.setName("Housing");
        categoryDTOWithCount.setResourceCount(2L);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(resourceRepository.findByCategory(category)).thenReturn(resources);
        when(categoryMapper.toDTOWithCount(category, 2L)).thenReturn(categoryDTOWithCount);

        // Act
        List<ResourceCategoryDTO> result = resourceCategoryService.getAllCategoriesWithCounts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getResourceCount());

        verify(categoryRepository).findAll();
        verify(resourceRepository).findByCategory(category);
        verify(categoryMapper).toDTOWithCount(category, 2L);
    }

    @Test
    @DisplayName("Should get category by ID successfully")
    void testGetCategoryById_Success() {
        // Arrange
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(categoryMapper.toDTO(category)).thenReturn(categoryDTO);

        // Act
        ResourceCategoryDTO result = resourceCategoryService.getCategoryById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Housing", result.getName());

        verify(categoryRepository).findById(1);
        verify(categoryMapper).toDTO(category);
    }

    @Test
    @DisplayName("Should throw exception when category not found by ID")
    void testGetCategoryById_NotFound() {
        // Arrange
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> resourceCategoryService.getCategoryById(999));

        verify(categoryRepository).findById(999);
        verify(categoryMapper, never()).toDTO(any());
    }

    @Test
    @DisplayName("Should get category by name successfully")
    void testGetCategoryByName_Success() {
        // Arrange
        when(categoryRepository.findByName("Housing")).thenReturn(Optional.of(category));
        when(categoryMapper.toDTO(category)).thenReturn(categoryDTO);

        // Act
        ResourceCategoryDTO result = resourceCategoryService.getCategoryByName("Housing");

        // Assert
        assertNotNull(result);
        assertEquals("Housing", result.getName());

        verify(categoryRepository).findByName("Housing");
        verify(categoryMapper).toDTO(category);
    }

    @Test
    @DisplayName("Should throw exception when category not found by name")
    void testGetCategoryByName_NotFound() {
        // Arrange
        when(categoryRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> resourceCategoryService.getCategoryByName("NonExistent"));

        verify(categoryRepository).findByName("NonExistent");
    }

    @Test
    @DisplayName("Should get category entity by ID successfully")
    void testGetCategoryEntityById_Success() {
        // Arrange
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

        // Act
        ResourceCategory result = resourceCategoryService.getCategoryEntityById(1);

        // Assert
        assertNotNull(result);
        assertEquals("Housing", result.getName());

        verify(categoryRepository).findById(1);
    }

    @Test
    @DisplayName("Should create category successfully")
    void testCreateCategory_Success() {
        // Arrange
        ResourceCategoryDTO newCategoryDTO = new ResourceCategoryDTO();
        newCategoryDTO.setName("Healthcare");
        newCategoryDTO.setDescription("Healthcare resources");
        newCategoryDTO.setIconName("health");

        ResourceCategory newCategory = ResourceCategory.builder()
                .name("Healthcare")
                .description("Healthcare resources")
                .iconName("health")
                .build();

        ResourceCategory savedCategory = ResourceCategory.builder()
                .id(2)
                .name("Healthcare")
                .description("Healthcare resources")
                .iconName("health")
                .build();

        ResourceCategoryDTO savedCategoryDTO = new ResourceCategoryDTO();
        savedCategoryDTO.setId(2);
        savedCategoryDTO.setName("Healthcare");

        when(categoryRepository.existsByName("Healthcare")).thenReturn(false);
        when(categoryMapper.toEntity(newCategoryDTO)).thenReturn(newCategory);
        when(categoryRepository.save(newCategory)).thenReturn(savedCategory);
        when(categoryMapper.toDTO(savedCategory)).thenReturn(savedCategoryDTO);

        // Act
        ResourceCategoryDTO result = resourceCategoryService.createCategory(newCategoryDTO);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("Healthcare", result.getName());

        verify(categoryRepository).existsByName("Healthcare");
        verify(categoryRepository).save(newCategory);
    }

    @Test
    @DisplayName("Should throw exception when creating category with existing name")
    void testCreateCategory_NameAlreadyExists() {
        // Arrange
        ResourceCategoryDTO newCategoryDTO = new ResourceCategoryDTO();
        newCategoryDTO.setName("Housing");

        when(categoryRepository.existsByName("Housing")).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> resourceCategoryService.createCategory(newCategoryDTO));

        verify(categoryRepository).existsByName("Housing");
        verify(categoryRepository, never()).save(any());
    }
}
