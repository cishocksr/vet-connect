package com.vetconnect.mapper;

import com.vetconnect.dto.resource.ResourceDTO;
import com.vetconnect.dto.resource.ResourceSummaryDTO;
import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Resource entity and Resource DTOs
 *
 * COMPLEX MAPPINGS:
 * - Handles nested category information
 * - Computes display fields (locationDisplay, etc.)
 * - Creates summary versions for lists
 * - Truncates descriptions for previews
 */
@Component
@RequiredArgsConstructor
public class ResourceMapper {

    private final ResourceCategoryMapper categoryMapper;

    /**
     * Convert Resource entity to full ResourceDTO
     *
     * USE CASE: Single resource detail page, editing
     *
     * @param resource The resource entity from database
     * @return ResourceDTO with complete information
     */
    public ResourceDTO toDTO(Resource resource) {
        if (resource == null) {
            return null;
        }

        return ResourceDTO.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .category(categoryMapper.toDTO(resource.getCategory()))
                .websiteUrl(resource.getWebsiteUrl())
                .phoneNumber(resource.getPhoneNumber())
                .email(resource.getEmail())
                .addressLine1(resource.getAddressLine1())
                .city(resource.getCity())
                .state(resource.getState())
                .zipCode(resource.getZipCode())
                .isNational(resource.getIsNational())
                .locationDisplay(resource.getLocationDisplay())  // Computed from entity
                .eligibilityCriteria(resource.getEligibilityCriteria())
                .hasContactInfo(resource.hasContactInfo())  // Computed from entity
                .hasCompleteAddress(resource.hasCompleteAddress())  // Computed from entity
                .createdAt(resource.getCreateAt())
                .build();
    }

    /**
     * Convert Resource entity to ResourceSummaryDTO (lightweight)
     *
     * USE CASE: Resource lists, search results, related resources
     *
     * @param resource The resource entity
     * @return ResourceSummaryDTO with essential information only
     */
    public ResourceSummaryDTO toSummaryDTO(Resource resource) {
        if (resource == null) {
            return null;
        }

        // Truncate description to 150 characters for preview
        String shortDescription = ResourceSummaryDTO.truncateDescription(
                resource.getDescription(), 150
        );

        return ResourceSummaryDTO.builder()
                .id(resource.getId())
                .name(resource.getName())
                .shortDescription(shortDescription)
                .categoryName(resource.getCategory() != null ?
                        resource.getCategory().getName() : null)
                .categoryIconName(resource.getCategory() != null ?
                        resource.getCategory().getIconName() : null)
                .locationDisplay(resource.getLocationDisplay())
                .isNational(resource.getIsNational())
                .phoneNumber(resource.getPhoneNumber())
                .websiteUrl(resource.getWebsiteUrl())
                .build();
    }

    /**
     * Convert list of resources to full DTOs
     *
     * @param resources List of resource entities
     * @return List of ResourceDTOs
     */
    public List<ResourceDTO> toDTOList(List<Resource> resources) {
        if (resources == null) {
            return null;
        }

        return resources.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of resources to summary DTOs
     *
     * @param resources List of resource entities
     * @return List of ResourceSummaryDTOs
     */
    public List<ResourceSummaryDTO> toSummaryDTOList(List<Resource> resources) {
        if (resources == null) {
            return null;
        }

        return resources.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create new Resource entity from CreateResourceRequest
     *
     * @param request The create request
     * @param category The category entity (must be fetched separately)
     * @return Resource entity (not yet saved to database)
     */
    public Resource toEntity(com.vetconnect.dto.resource.CreateResourceRequest request,
                             ResourceCategory category) {
        if (request == null) {
            return null;
        }

        return Resource.builder()
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .websiteUrl(request.getWebsiteUrl())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .addressLine1(request.getAddressLine1())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .isNational(request.isNational())
                .eligibilityCriteria(request.getEligibilityCriteria())
                .build();
    }

    /**
     * Update Resource entity from UpdateResourceRequest
     *
     * IMPORTANT: Only updates non-null fields from the request
     * Allows partial updates
     *
     * @param resource Existing resource entity to update
     * @param updateRequest The update request
     * @param category New category (if categoryId was provided)
     */
    public void updateEntityFromDTO(Resource resource,
                                    com.vetconnect.dto.resource.UpdateResourceRequest updateRequest,
                                    ResourceCategory category) {
        if (updateRequest == null) {
            return;
        }

        // Update category if provided
        if (category != null) {
            resource.setCategory(category);
        }

        // Update basic fields if provided
        if (updateRequest.getName() != null) {
            resource.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            resource.setDescription(updateRequest.getDescription());
        }

        // Update contact information if provided
        if (updateRequest.getWebsiteUrl() != null) {
            resource.setWebsiteUrl(updateRequest.getWebsiteUrl());
        }
        if (updateRequest.getPhoneNumber() != null) {
            resource.setPhoneNumber(updateRequest.getPhoneNumber());
        }
        if (updateRequest.getEmail() != null) {
            resource.setEmail(updateRequest.getEmail());
        }

        // Update location fields if provided
        if (updateRequest.getAddressLine1() != null) {
            resource.setAddressLine1(updateRequest.getAddressLine1());
        }
        if (updateRequest.getCity() != null) {
            resource.setCity(updateRequest.getCity());
        }
        if (updateRequest.getState() != null) {
            resource.setState(updateRequest.getState());
        }
        if (updateRequest.getZipCode() != null) {
            resource.setZipCode(updateRequest.getZipCode());
        }

        // Update national flag if provided
        if (updateRequest.getIsNational() != null) {
            resource.setIsNational(updateRequest.getIsNational());
        }

        // Update eligibility criteria if provided
        if (updateRequest.getEligibilityCriteria() != null) {
            resource.setEligibilityCriteria(updateRequest.getEligibilityCriteria());
        }
    }
}