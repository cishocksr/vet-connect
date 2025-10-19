package com.vetconnect.repository;

import com.vetconnect.model.Resource;
import com.vetconnect.model.SavedResource;
import com.vetconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedResourcesRepository extends JpaRepository<SavedResource, UUID> {

    List<SavedResource> findByUser(User user);


    Optional<SavedResource> findByUserAndResource(User user, Resource resource);


    boolean existsByUserAndResource(User user, Resource resource);


    long countByUser(User user);


    void deleteByUserAndResource(User user, Resource resource);

    List<SavedResource> findByUserOrderBySavedAtDesc(User user);


    List<SavedResource> findByResource(Resource resource);


    @Query("SELECT sr FROM SavedResource sr WHERE sr.user = :user AND sr.notes IS NOT NULL AND sr.notes != ''")
    List<SavedResource> findByUserWithNotes(@Param("user") User user);

    @Query("SELECT sr FROM SavedResource sr JOIN sr.resource r WHERE sr.user = :user AND r.category.id = :categoryId")
    List<SavedResource> findByUserAndCategory(@Param("user") User user, @Param("categoryId") Integer categoryId);



}
