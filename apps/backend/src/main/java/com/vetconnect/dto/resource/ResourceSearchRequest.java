package com.vetconnect.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for searching/filtering resources
 *
 * GET /api/resources/search?keyword=housing&categoryId=1&state=VA&page=0&size=20
 *
 * All parameters are optional - combine them for powerful filtering
 *
 * Examples:
 *
 * 1. Search by keyword:
 *    ?keyword=mental health
 *
 * 2. Filter by category:
 *    ?categoryId=4
 *
 * 3. Filter by state:
 *    ?state=VA
 *
 * 4. Combine filters:
 *    ?keyword=housing&state=CA&page=0&size=10
 *
 * 5. Include national resources:
 *    ?state=VA&includeNational=true
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSearchRequest {

    /**
     * Search keyword (searches name and description)
     * Example: "mental health", "housing", "job training"
     */
    private String keyword;

    /**
     * Filter by category ID
     * Example: 1 = Housing, 2 = Financial, etc.
     */
    private Integer categoryId;

    /**
     * Filter by state (2-letter code)
     * Example: "VA", "CA", "TX"
     */
    private String state;

    /**
     * Include national resources in results?
     * Default: true (always include national resources)
     *
     * When true: Returns state-specific + national resources
     * When false: Returns only state-specific resources
     */
    @Builder.Default
    private boolean includeNational = true;

    /**
     * Page number (0-indexed)
     * Default: 0 (first page)
     */
    @Builder.Default
    private int page = 0;

    /**
     * Page size (items per page)
     * Default: 20
     * Max: 100 (enforced in service layer)
     */
    @Builder.Default
    private int size = 20;

    /**
     * Sort field
     * Options: "name", "createdAt", "category"
     * Default: "name"
     */
    @Builder.Default
    private String sortBy = "name";

    /**
     * Sort direction
     * Options: "ASC", "DESC"
     * Default: "ASC"
     */
    @Builder.Default
    private String sortDirection = "ASC";

    // ========== HELPER METHODS ==========

    /**
     * Is this a search request (has keyword)?
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

    /**
     * Is this a filtered request (has any filters)?
     */
    public boolean hasFilters() {
        return categoryId != null || state != null || hasKeyword();
    }

    /**
     * Validate and cap page size
     */
    public int getValidatedSize() {
        if (size < 1) return 20;
        if (size > 100) return 100;
        return size;
    }
}