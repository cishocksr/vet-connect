package com.vetconnect.mapper;

import com.vetconnect.dto.resource.ResourceCategoryDTO;
import com.vetconnect.model.ResourceCategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for ResourceCategory entity and ResourceCategoryDTO
 *
 * This is a simple mapper since categories are straightforward
 * No complex logic needed - just direct field mapping
 */
@Component
public class ResourceCategoryMapper {

    /**
     * Convert ResourceCategory entity to DTO
     *
     * @param category The category entity
     * @return ResourceCategoryDTO
     */
    public ResourceCategoryDTO toDTO(ResourceCategory category) {
        if (category == null) {
            return null;
        }

        return ResourceCategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconName(category.getIconName())
                // resourceCount is set separately when needed (requires query)
                .build();
    }

    /**
     * Convert ResourceCategory entity to DTO with resource count
     *
     * @param category The category entity
     * @param resourceCount Number of resources in this category
     * @return ResourceCategoryDTO with count
     */
    public ResourceCategoryDTO toDTOWithCount(ResourceCategory category, Long resourceCount) {
        if (category == null) {
            return null;
        }

        ResourceCategoryDTO dto = toDTO(category);
        dto.setResourceCount(resourceCount);
        return dto;
    }

    /**
     * Convert list of categories to DTOs
     *
     * @param categories List of category entities
     * @return List of DTOs
     */
    public List<ResourceCategoryDTO> toDTOList(List<ResourceCategory> categories) {
        if (categories == null) {
            return null;
        }

        return categories.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert DTO to entity (for creating new categories)
     *
     * NOTE: Typically only admins can create categories
     *
     * @param dto The category DTO
     * @return ResourceCategory entity
     */
    public ResourceCategory toEntity(ResourceCategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        return ResourceCategory.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .iconName(dto.getIconName())
                .build();
    }
}