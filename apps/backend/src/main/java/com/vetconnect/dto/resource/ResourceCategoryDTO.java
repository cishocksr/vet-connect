package com.vetconnect.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resource category information DTO
 *
 * Example response:
 * {
 *   "id": 5,
 *   "name": "Healthcare",
 *   "description": "Primary care, dental, and general health services",
 *   "iconName": "heart-pulse",
 *   "resourceCount": 127
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCategoryDTO {

    /**
     * Category ID (integer, not UUID)
     */
    private Integer id;

    /**
     * Category name
     * Examples: "Housing", "Healthcare", "Financial", "Education", "Mental Health"
     */
    private String name;

    /**
     * Category description
     */
    private String description;

    /**
     * Icon identifier for frontend
     * Examples: "home", "heart-pulse", "dollar-sign", "graduation-cap", "brain"
     */
    private String iconName;

    /**
     * Number of resources in this category (optional)
     * Useful for showing "Housing (45 resources)"
     */
    private Long resourceCount;
}