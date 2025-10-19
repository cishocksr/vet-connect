package com.vetconnect.repository;

import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    /**
     * Find all resources in a specific category
     *
     * @param category The category to filter by
     * @return List of all resources in that category
     *
     */
    List<Resource> findByCategory(ResourceCategory category);

    /**
     * Find all resources in a specific state
     *
     * @param state Two=letter state code
     * @return List of all resources in that state
     */
    List<Resource> findByState(String state);

    /**
     * Find all national resource
     *
     * @return List of all national resources
     */
    List<Resource> findByIsNationalTrue();


    /**
     * Find resources by state or National
     *
     * @param state Two-letter state code
     * @return List of resources available in that state
     */
    List<Resource> findByIsNationalTrueOrState(String state);

    /**
     * Find resources by category and location
     *
     * @param category The category to filter by
     * @param state Two-letter state code
     * @return Resources in that category available in that state
     */
    @Query("SELECT r FROM Resource r WHERE r.category = :category AND (r.isNational = true OR r.state = :state)")
    List<Resource> findByCategoryAndLocation(@Param("category") ResourceCategory category, @Param("state") String state);

    /**
     * Search resources by keyword in name or description
     *
     * @param keyword The search term
     * @return List of resources matching the keyword
     */
    @Query("SELECT r FROM Resource r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Resource> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Search resources with pagination
     *
     * @param keyword Search term
     * @param pageable Pagination parameters
     * @return Page of resources matching the keyword
     */

    @Query("SELECT r FROM Resource r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Resource> searchByKeywordPaginated(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find all resources with pagination
     *
     * @param pageable Pagination parameters
     * @return Page of all resources
     */

    Page<Resource> findAll(Pageable pageable);

    /**
     * Find resources by category with pagination
     *
     * @param category The category to filter by
     * @param pageable Pagination parameters
     * @return Page of resources in that category
     */

    Page<Resource> findByCategory(ResourceCategory category, Pageable pageable);

    /**
     * Find resources by state or national with pagination
     *
     * @param state Two letter state code
     * @param pageable Pagination parameters
     * @return Page of resources available in that state
     */
    Page<Resource> findByIsNationalTrueOrState(String state, Pageable pageable);

    /**
     * Complex search with multiple filters
     *
     * @param category Filter by category (can be null)
     * @param state Filter by state (can be null)
     * @param keyword Search term (can be null)
     * @param pageable Pagination parameters
     * @return Page of resources matching all provided filters
     */

    @Query("SELECT r FROM Resource r WHERE " +
            "(:category IS NULL OR r.category = :category) AND " +
            "(:state IS NULL OR r.isNational = true OR r.state = :state) AND " +
            "(:keyword IS NULL OR " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Resource> searchWithFilters(
            @Param("category") ResourceCategory category,
            @Param("state") String state,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
