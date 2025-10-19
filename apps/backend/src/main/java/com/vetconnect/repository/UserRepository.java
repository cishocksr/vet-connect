package com.vetconnect.repository;

import com.vetconnect.model.User;
import com.vetconnect.model.enums.BranchOfService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (for login)
     * @param email user's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email (for registration validation)
     * @param email user's email
     * @return true if user exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by branch of service
     * @param branchOfService the military branch
     * @return list of users in that branch
     */
    List<User> findByBranchOfService(BranchOfService branchOfService);

    /**
     * Find all homeless users
     * @return list of homeless users
     */
    List<User> findByIsHomelessTrue();

    /**
     * Find users by state (for location-based services)
     * @param state two-letter state code
     * @return list of users in that state
     */
    List<User> findByState(String state);

    /**
     * Find users by state and branch of service
     * @param state two-letter state code
     * @param branchOfService the military branch
     * @return list of users matching both criteria
     */
    List<User> findByStateAndBranchOfService(String state, BranchOfService branchOfService);

    /**
     * Custom query to find users by partial name match (case-insensitive)
     * @param name partial name to search for
     * @return list of users matching the name
     */
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> searchByName(@Param("name") String name);
}
