package com.vetconnect.service;

import com.vetconnect.dto.resource.ResourceCategoryDTO;
import com.vetconnect.mapper.ResourceCategoryMapper;
import com.vetconnect.model.ResourceCategory;
import com.vetconnect.repository.ResourceCategoryRepository;
import com.vetconnect.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for resource category operations
 *
 * RESPONSIBILITIES:
 * - Get all categories
 * - Get category by ID
 * - Get categories with resource counts
 * - Create new categories (admin only)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResourceCategoryService {

    private final ResourceCategoryRepository categoryRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceCategoryMapper categoryMapper;

    /**
     * Get all categories
     *
     * @return List of all ResourceCategoryDTOs
     */
    public List<ResourceCategoryDTO> getAllCategories() {
        log.debug("Fetching all categories");

        List<ResourceCategory> categories = categoryRepository.findAll();

        return categoryMapper.toDTOList(categories);
    }

    /**
     * Get all categories with resource counts
     *
     * USE CASE: Homepage showing "Housing (45)", "Healthcare (67)", etc.
     *
     * @return List of ResourceCategoryDTOs with counts
     */
    public List<ResourceCategoryDTO> getAllCategoriesWithCounts() {
        log.debug("Fetching all categories with resource counts");

        List<ResourceCategory> categories = categoryRepository.findAll();

        return categories.stream()
                .map(category -> {
                    long count = resourceRepository.findByCategory(category).size();
                    return categoryMapper.toDTOWithCount(category, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get category by ID
     *
     * @param categoryId Category ID
     * @return ResourceCategoryDTO
     * @throws RuntimeException if category not found
     */
    public ResourceCategoryDTO getCategoryById(Integer categoryId) {
        log.debug("Fetching category by ID: {}", categoryId);

        ResourceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));

        return categoryMapper.toDTO(category);
    }

    /**
     * Get category by name
     *
     * @param name Category name
     * @return ResourceCategoryDTO
     * @throws RuntimeException if category not found
     */
    public ResourceCategoryDTO getCategoryByName(String name) {
        log.debug("Fetching category by name: {}", name);

        ResourceCategory category = categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));

        return categoryMapper.toDTO(category);
    }

    /**
     * Get ResourceCategory entity by ID (internal use)
     *
     * @param categoryId Category ID
     * @return ResourceCategory entity
     * @throws RuntimeException if not found
     */
    protected ResourceCategory getCategoryEntityById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + categoryId));
    }

    /**
     * Create new category (admin only)
     *
     * @param categoryDTO Category information
     * @return Created ResourceCategoryDTO
     * @throws RuntimeException if category name already exists
     */
    @Transactional
    public ResourceCategoryDTO createCategory(ResourceCategoryDTO categoryDTO) {
        log.info("Creating new category: {}", categoryDTO.getName());

        // Check if category name already exists
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Category already exists with name: " + categoryDTO.getName());
        }

        ResourceCategory category = categoryMapper.toEntity(categoryDTO);
        ResourceCategory savedCategory = categoryRepository.save(category);

        log.info("Successfully created category: {}", savedCategory.getName());

        return categoryMapper.toDTO(savedCategory);
    }
}