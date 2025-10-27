package com.vetconnect.dto.resource;

import com.vetconnect.dto.common.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Enhanced search response with additional metadata
 *
 * Example response:
 * {
 *   "results": {
 *     "content": [ ...array of ResourceSummaryDTO... ],
 *     "pageNumber": 0,
 *     "pageSize": 20,
 *     "totalElements": 47,
 *     "totalPages": 3,
 *     "isFirst": true,
 *     "isLast": false,
 *     "hasNext": true,
 *     "hasPrevious": false
 *   },
 *   "searchMetadata": {
 *     "keyword": "housing",
 *     "categoryId": 1,
 *     "state": "VA",
 *     "includeNational": true
 *   },
 *   "facets": {
 *     "categoryCounts": {
 *       "Housing": 47,
 *       "Financial": 12
 *     },
 *     "stateCounts": {
 *       "VA": 35,
 *       "National": 12
 *     }
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceSearchResponse {

    /**
     * Paginated search results
     */
    private PageResponse<ResourceSummaryDTO> results;

    /**
     * Search criteria that was used
     * Useful for frontend to display "Showing results for..."
     */
    private ResourceSearchRequest searchMetadata;

    /**
     * Faceted counts for filtering UI
     * Shows how many results exist in each category/state
     * Useful for "refine your search" UI
     */
    private SearchFacets facets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchFacets {
        /**
         * Count of results per category
         * Example: {"Housing": 47, "Healthcare": 23, ...}
         */
        private Map<String, Long> categoryCounts;

        /**
         * Count of results per state
         * Example: {"VA": 35, "CA": 12, "National": 15}
         */
        private Map<String, Long> stateCounts;
    }

    /**
     * Get total number of results
     */
    public long getTotalResults() {
        return results != null ? results.getTotalElements() : 0;
    }

    /**
     * Check if search returned any results
     */
    public boolean hasResults() {
        return results != null &&
                results.getContent() != null &&
                !results.getContent().isEmpty();
    }
}