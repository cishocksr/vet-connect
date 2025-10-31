package com.vetconnect.service;

import com.vetconnect.dto.common.PageResponse;
import com.vetconnect.dto.resource.*;
import com.vetconnect.mapper.ResourceMapper;
import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import com.vetconnect.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for resource operations
 *
 * RESPONSIBILITIES:
 * - Resource CRUD operations
 * - Resource search and filtering
 * - Location-based queries
 * - Category-based queries
 * - Pagination support
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final ResourceCategoryService categoryService;

    // ========== READ OPERATIONS ==========

    /**
     * Get resource by ID
     *
     * @param resourceId Resource UUID
     * @return ResourceDTO with complete information
     * @throws RuntimeException if resource not found
     */
    public ResourceDTO getResourceById(UUID resourceId) {
        log.debug("Fetching resource by ID: {}", resourceId);

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));

        return resourceMapper.toDTO(resource);
    }

    /**
     * Get all resources (paginated)
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return PageResponse of ResourceSummaryDTOs
     */
    public PageResponse<ResourceSummaryDTO> getAllResources(int page, int size) {
        log.debug("Fetching all resources - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Resource> resourcePage = resourceRepository.findAll(pageable);

        Page<ResourceSummaryDTO> dtoPage = resourcePage.map(resourceMapper::toSummaryDTO);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Get resources by category (paginated)
     *
     * @param categoryId Category ID
     * @param page Page number
     * @param size Page size
     * @return PageResponse of ResourceSummaryDTOs
     * @throws RuntimeException if category not found
     */
    public PageResponse<ResourceSummaryDTO> getResourcesByCategory(Integer categoryId, int page, int size) {
        log.debug("Fetching resources by category: {} - page: {}, size: {}", categoryId, page, size);

        ResourceCategory category = categoryService.getCategoryEntityById(categoryId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Resource> resourcePage = resourceRepository.findByCategory(category, pageable);

        Page<ResourceSummaryDTO> dtoPage = resourcePage.map(resourceMapper::toSummaryDTO);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Get resources by state or national (paginated)
     *
     * Returns:
     * - Resources specific to the state
     * - National resources (available everywhere)
     *
     * @param state Two-letter state code
     * @param page Page number
     * @param size Page size
     * @return PageResponse of ResourceSummaryDTOs
     */
    public PageResponse<ResourceSummaryDTO> getResourcesByState(String state, int page, int size) {
        log.debug("Fetching resources by state: {} - page: {}, size: {}", state, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Resource> resourcePage = resourceRepository.findByIsNationalTrueOrState(state, pageable);

        Page<ResourceSummaryDTO> dtoPage = resourcePage.map(resourceMapper::toSummaryDTO);

        return PageResponse.fromPage(dtoPage);
    }

    /**
     * Get national resources only
     *
     * @return List of national ResourceDTOs
     */
    public List<ResourceDTO> getNationalResources() {
        log.debug("Fetching national resources");

        List<Resource> resources = resourceRepository.findByIsNationalTrue();

        return resourceMapper.toDTOList(resources);
    }

    /**
     * Search resources with filters
     *
     * Supports:
     * - Keyword search (name + description)
     * - Category filter
     * - State filter
     * - Include/exclude national resources
     * - Pagination
     * - Sorting
     *
     * @param searchRequest Search criteria
     * @return ResourceSearchResponse with results and metadata
     */
    public ResourceSearchResponse searchResources(ResourceSearchRequest searchRequest) {
        log.debug("Searching resources with filters: {}", searchRequest);

        // Build sort
        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("DESC")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                searchRequest.getSortBy()
        );

        // Build pageable
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getValidatedSize(),
                sort
        );

        // Get category if provided
        ResourceCategory category = null;
        if (searchRequest.getCategoryId() != null) {
            category = categoryService.getCategoryEntityById(searchRequest.getCategoryId());
        }

        // Execute search
        Page<Resource> resourcePage = resourceRepository.searchWithFilters(
                category,
                searchRequest.getState(),
                searchRequest.getKeyword(),
                pageable
        );

        // Convert to DTOs
        Page<ResourceSummaryDTO> dtoPage = resourcePage.map(resourceMapper::toSummaryDTO);
        PageResponse<ResourceSummaryDTO> pageResponse = PageResponse.fromPage(dtoPage);

        // Build response with metadata
        return ResourceSearchResponse.builder()
                .results(pageResponse)
                .searchMetadata(searchRequest)
                // TODO: Add facets (category counts, state counts) in future enhancement
                .build();
    }

    /**
     * Search resources with filters (simplified version)
     * Returns PageResponse directly instead of ResourceSearchResponse
     *
     * This method is used by the REST API to return a simpler response
     * that matches the frontend's expectations.
     *
     * @param searchRequest Search criteria
     * @return PageResponse of ResourceSummaryDTOs
     */
    public PageResponse<ResourceSummaryDTO> searchResourcesSimple(ResourceSearchRequest searchRequest) {
        log.debug("Searching resources (simple) with filters: {}", searchRequest);

        // Build sort
        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("DESC")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                searchRequest.getSortBy()
        );

        // Build pageable
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getValidatedSize(),
                sort
        );

        // Get category entity if category filter is provided
        ResourceCategory category = null;
        if (searchRequest.getCategoryId() != null) {
            category = categoryService.getCategoryEntityById(searchRequest.getCategoryId());
        }

        // Use the existing searchWithFilters method
        // It handles all combinations of filters (category, state, keyword)
        Page<Resource> resourcePage = resourceRepository.searchWithFilters(
                category,
                searchRequest.getState(),
                searchRequest.getKeyword(),
                pageable
        );

        // Map to DTOs
        Page<ResourceSummaryDTO> dtoPage = resourcePage.map(resourceMapper::toSummaryDTO);

        return PageResponse.fromPage(dtoPage);
    }

    // ========== WRITE OPERATIONS ==========

    /**
     * Create new resource
     *
     * VALIDATION:
     * - Category must exist
     * - Must have at least one contact method
     * - If not national, must have state
     *
     * @param createRequest Resource creation request
     * @return Created ResourceDTO
     * @throws RuntimeException if validation fails
     */
    @Transactional
    public ResourceDTO createResource(CreateResourceRequest createRequest) {
        log.info("Creating new resource: {}", createRequest.getName());

        // Validate contact info
        if (!createRequest.hasContactInfo()) {
            throw new RuntimeException("Resource must have at least one contact method");
        }

        // Validate scope (national vs local)
        if (!createRequest.isValidForScope()) {
            throw new RuntimeException("Local resources must have a state");
        }

        // Get category
        ResourceCategory category = categoryService.getCategoryEntityById(createRequest.getCategoryId());

        // Create entity
        Resource resource = resourceMapper.toEntity(createRequest, category);

        // Save
        Resource savedResource = resourceRepository.save(resource);
        log.info("Successfully created resource: {}", savedResource.getName());

        return resourceMapper.toDTO(savedResource);
    }

    /**
     * Update existing resource
     *
     * @param resourceId Resource UUID
     * @param updateRequest Update request
     * @return Updated ResourceDTO
     * @throws RuntimeException if resource not found or validation fails
     */
    @Transactional
    public ResourceDTO updateResource(UUID resourceId, UpdateResourceRequest updateRequest) {
        log.info("Updating resource: {}", resourceId);

        // Validate has updates
        if (!updateRequest.hasAnyUpdates()) {
            throw new RuntimeException("No updates provided");
        }

        // Get existing resource
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));

        // Get new category if provided
        ResourceCategory category = null;
        if (updateRequest.getCategoryId() != null) {
            category = categoryService.getCategoryEntityById(updateRequest.getCategoryId());
        }

        // Apply updates
        resourceMapper.updateEntityFromDTO(resource, updateRequest, category);

        // Save
        Resource updatedResource = resourceRepository.save(resource);
        log.info("Successfully updated resource: {}", resourceId);

        return resourceMapper.toDTO(updatedResource);
    }

    /**
     * Delete resource
     *
     * @param resourceId Resource UUID
     * @throws RuntimeException if resource not found
     */
    @Transactional
    public void deleteResource(UUID resourceId) {
        log.warn("Deleting resource: {}", resourceId);

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));

        resourceRepository.delete(resource);
        log.info("Successfully deleted resource: {}", resourceId);
    }

    // ========== HELPER METHODS ==========

    /**
     * Get Resource entity by ID (internal use)
     *
     * @param resourceId Resource UUID
     * @return Resource entity
     * @throws RuntimeException if not found
     */
    protected Resource getResourceEntityById(UUID resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found with ID: " + resourceId));
    }
}