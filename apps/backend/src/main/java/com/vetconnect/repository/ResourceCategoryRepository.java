package com.vetconnect.repository;

import com.vetconnect.model.ResourceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceCategoryRepository extends JpaRepository<ResourceCategory, Integer> {

    /**
     * Find resource by name
     * @param name the category name to search for
     * @return Optional containing the category if found, empty Optional if not
     */
    Optional<ResourceCategory> findByName(String name);


    /**
     * Check if category exists  by name
     * @param name the category to check
     * @return true if a category with this name exists, false otherwise
     */
    boolean existsByName(String name);



    /**
     * Find category by icon name
     * @param iconName the icon identifier
     * @return Optional containing the category if found
     */
    Optional<ResourceCategory> findByIconName(String iconName);

}
