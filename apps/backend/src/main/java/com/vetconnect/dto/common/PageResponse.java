package com.vetconnect.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Paginated response wrapper
 * Used when returning lists of data with pagination support
 *
 * Example response:
 * {
 *   "content": [ ...array of items... ],
 *   "pageNumber": 0,
 *   "pageSize": 20,
 *   "totalElements": 150,
 *   "totalPages": 8,
 *   "isFirst": true,
 *   "isLast": false,
 *   "hasNext": true,
 *   "hasPrevious": false
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * The actual list of items for this page
     */
    private List<T> content;

    /**
     * Current page number (0-indexed)
     * Page 0 = first page
     */
    private int pageNumber;

    /**
     * Number of items per page
     */
    private int pageSize;

    /**
     * Total number of items across all pages
     */
    private long totalElements;

    /**
     * Total number of pages available
     */
    private int totalPages;

    /**
     * Is this the first page?
     */
    private boolean isFirst;

    /**
     * Is this the last page?
     */
    private boolean isLast;

    /**
     * Are there more pages after this one?
     */
    private boolean hasNext;

    /**
     * Are there pages before this one?
     */
    private boolean hasPrevious;

    /**
     * Convert Spring's Page object to our PageResponse
     */
    public static <T> PageResponse<T> fromPage(org.springframework.data.domain.Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}