package com.vetconnect.service;

import com.vetconnect.dto.saved.SaveResourceRequest;
import com.vetconnect.dto.saved.SavedResourceDTO;
import com.vetconnect.dto.saved.UpdateSavedResourceNotesRequest;
import com.vetconnect.mapper.SavedResourceMapper;
import com.vetconnect.model.Resource;
import com.vetconnect.model.SavedResource;
import com.vetconnect.model.User;
import com.vetconnect.repository.SavedResourcesRepository;
import com.vetconnect.util.InputSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for saved resource operations
 *
 * RESPONSIBILITIES:
 * - Save resources for users
 * - Unsave resources
 * - Update notes on saved resources
 * - Get user's saved resources
 * - Check if resource is saved
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SavedResourceService {

    private final SavedResourcesRepository savedResourcesRepository;
    private final SavedResourceMapper savedResourceMapper;
    private final UserService userService;
    private final ResourceService resourceService;
    private final InputSanitizer inputSanitizer;

    // ========== READ OPERATIONS ==========

    /**
     * Get all saved resources for a user
     *
     * @param userId User's UUID
     * @return List of SavedResourceDTOs (sorted by saved date, newest first)
     */
    public List<SavedResourceDTO> getUserSavedResources(UUID userId) {
        log.debug("Fetching saved resources for user: {}", userId);

        User user = userService.getUserEntityById(userId);
        List<SavedResource> savedResources = savedResourcesRepository.findByUserOrderBySavedAtDesc(user);

        return savedResourceMapper.toDTOList(savedResources);
    }

    /**
     * Get saved resources with notes only
     *
     * USE CASE: User wants to see resources they've added notes to
     *
     * @param userId User's UUID
     * @return List of SavedResourceDTOs that have notes
     */
    public List<SavedResourceDTO> getUserSavedResourcesWithNotes(UUID userId) {
        log.debug("Fetching saved resources with notes for user: {}", userId);

        User user = userService.getUserEntityById(userId);
        List<SavedResource> savedResources = savedResourcesRepository.findByUserWithNotes(user);

        return savedResourceMapper.toDTOList(savedResources);
    }

    /**
     * Get saved resources by category
     *
     * @param userId User's UUID
     * @param categoryId Category ID
     * @return List of SavedResourceDTOs in that category
     */
    public List<SavedResourceDTO> getUserSavedResourcesByCategory(UUID userId, Integer categoryId) {
        log.debug("Fetching saved resources for user: {} in category: {}", userId, categoryId);

        User user = userService.getUserEntityById(userId);
        List<SavedResource> savedResources = savedResourcesRepository.findByUserAndCategory(user, categoryId);

        return savedResourceMapper.toDTOList(savedResources);
    }

    /**
     * Check if user has saved a specific resource
     *
     * @param userId User's UUID
     * @param resourceId Resource UUID
     * @return true if saved, false otherwise
     */
    public boolean isResourceSaved(UUID userId, UUID resourceId) {
        User user = userService.getUserEntityById(userId);
        Resource resource = resourceService.getResourceEntityById(resourceId);

        return savedResourcesRepository.existsByUserAndResource(user, resource);
    }

    /**
     * Get count of user's saved resources
     *
     * @param userId User's UUID
     * @return Number of saved resources
     */
    public long getSavedResourceCount(UUID userId) {
        User user = userService.getUserEntityById(userId);
        return savedResourcesRepository.countByUser(user);
    }

    // ========== WRITE OPERATIONS ==========

    /**
     * Save a resource for a user
     *
     * BUSINESS RULES:
     * - User can only save a resource once (unique constraint)
     * - Notes are optional
     *
     * @param userId User's UUID
     * @param saveRequest Save request with resource ID and optional notes
     * @return Created SavedResourceDTO
     * @throws RuntimeException if resource already saved or not found
     */

    @Transactional
    public SavedResourceDTO saveResource(UUID userId, SaveResourceRequest saveRequest) {
        log.info("User {} saving resource: {}", userId, saveRequest.getResourceId());

        User user = userService.getUserEntityById(userId);
        Resource resource = resourceService.getResourceEntityById(saveRequest.getResourceId());

        // Check if already saved
        if (savedResourcesRepository.existsByUserAndResource(user, resource)) {
            throw new RuntimeException("Resource already saved");
        }

        // Sanitize notes if provided
        String sanitizedNotes = saveRequest.getNotes() != null
                ? inputSanitizer.sanitizeHtml(saveRequest.getNotes())
                : null;

        // Create saved resource
        SavedResource savedResource = SavedResource.builder()
                .user(user)
                .resource(resource)
                .notes(sanitizedNotes)  // Use sanitized notes
                .build();

        SavedResource saved = savedResourcesRepository.save(savedResource);
        log.info("Successfully saved resource {} for user {}", saveRequest.getResourceId(), userId);

        return savedResourceMapper.toDTO(saved);
    }

    /**
     * Unsave (remove) a saved resource
     *
     * @param userId User's UUID
     * @param savedResourceId SavedResource UUID (not Resource UUID!)
     * @throws RuntimeException if saved resource not found or doesn't belong to user
     */
    @Transactional
    public void unsaveResource(UUID userId, UUID savedResourceId) {
        log.info("User {} unsaving resource: {}", userId, savedResourceId);

        SavedResource savedResource = savedResourcesRepository.findById(savedResourceId)
                .orElseThrow(() -> new RuntimeException("Saved resource not found with ID: " + savedResourceId));

        // Verify ownership
        if (!savedResource.getUser().getId().equals(userId)) {
            throw new RuntimeException("Saved resource does not belong to user");
        }

        savedResourcesRepository.delete(savedResource);
        log.info("Successfully unsaved resource {} for user {}", savedResourceId, userId);
    }

    /**
     * Update notes on a saved resource
     *
     * @param userId User's UUID
     * @param savedResourceId SavedResource UUID
     * @param notesRequest New notes
     * @return Updated SavedResourceDTO
     * @throws RuntimeException if saved resource not found or doesn't belong to user
     */
    @Transactional
    public SavedResourceDTO updateNotes(UUID userId, UUID savedResourceId,
                                        UpdateSavedResourceNotesRequest notesRequest) {
        log.info("User {} updating notes on saved resource: {}", userId, savedResourceId);

        SavedResource savedResource = savedResourcesRepository.findById(savedResourceId)
                .orElseThrow(() -> new RuntimeException("Saved resource not found with ID: " + savedResourceId));

        // Verify ownership
        if (!savedResource.getUser().getId().equals(userId)) {
            throw new RuntimeException("Saved resource does not belong to user");
        }

        // Sanitize and update notes
        String sanitizedNotes = notesRequest.getNotes() != null
                ? inputSanitizer.sanitizeHtml(notesRequest.getNotes())
                : null;
        savedResource.setNotes(sanitizedNotes);

        SavedResource updated = savedResourcesRepository.save(savedResource);
        log.info("Successfully updated notes for saved resource: {}", savedResourceId);

        return savedResourceMapper.toDTO(updated);
    }
}