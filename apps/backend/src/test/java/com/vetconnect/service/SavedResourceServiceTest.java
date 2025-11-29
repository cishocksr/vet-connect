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
import com.vetconnect.util.XssProtection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavedResourceService Tests")
class SavedResourceServiceTest {

    @Mock
    private SavedResourcesRepository savedResourcesRepository;

    @Mock
    private SavedResourceMapper savedResourceMapper;

    @Mock
    private UserService userService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private InputSanitizer inputSanitizer;

    @Mock
    private XssProtection xssProtection;

    @InjectMocks
    private SavedResourceService savedResourceService;

    private User user;
    private Resource resource;
    private SavedResource savedResource;
    private SavedResourceDTO savedResourceDTO;
    private UUID userId;
    private UUID resourceId;
    private UUID savedResourceId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        savedResourceId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        resource = new Resource();
        resource.setId(resourceId);
        resource.setName("Test Resource");

        savedResource = SavedResource.builder()
                .id(savedResourceId)
                .user(user)
                .resource(resource)
                .notes("Test notes")
                .savedAt(LocalDateTime.now())
                .build();

        savedResourceDTO = SavedResourceDTO.builder()
                .id(savedResourceId)
                .notes("Test notes")
                .build();
    }

    @Test
    @DisplayName("Should get user saved resources successfully")
    void testGetUserSavedResources_Success() {
        // Arrange
        List<SavedResource> savedResources = Arrays.asList(savedResource);
        List<SavedResourceDTO> expectedDTOs = Arrays.asList(savedResourceDTO);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(savedResourcesRepository.findByUserOrderBySavedAtDesc(user)).thenReturn(savedResources);
        when(savedResourceMapper.toDTOList(savedResources)).thenReturn(expectedDTOs);

        // Act
        List<SavedResourceDTO> result = savedResourceService.getUserSavedResources(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(savedResourceId, result.get(0).getId());

        verify(userService).getUserEntityById(userId);
        verify(savedResourcesRepository).findByUserOrderBySavedAtDesc(user);
        verify(savedResourceMapper).toDTOList(savedResources);
    }

    @Test
    @DisplayName("Should get saved resources with notes")
    void testGetUserSavedResourcesWithNotes_Success() {
        // Arrange
        List<SavedResource> savedResources = Arrays.asList(savedResource);
        List<SavedResourceDTO> expectedDTOs = Arrays.asList(savedResourceDTO);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(savedResourcesRepository.findByUserWithNotes(user)).thenReturn(savedResources);
        when(savedResourceMapper.toDTOList(savedResources)).thenReturn(expectedDTOs);

        // Act
        List<SavedResourceDTO> result = savedResourceService.getUserSavedResourcesWithNotes(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(savedResourcesRepository).findByUserWithNotes(user);
    }

    @Test
    @DisplayName("Should get saved resources by category")
    void testGetUserSavedResourcesByCategory_Success() {
        // Arrange
        Integer categoryId = 1;
        List<SavedResource> savedResources = Arrays.asList(savedResource);
        List<SavedResourceDTO> expectedDTOs = Arrays.asList(savedResourceDTO);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(savedResourcesRepository.findByUserAndCategory(user, categoryId)).thenReturn(savedResources);
        when(savedResourceMapper.toDTOList(savedResources)).thenReturn(expectedDTOs);

        // Act
        List<SavedResourceDTO> result = savedResourceService.getUserSavedResourcesByCategory(userId, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(savedResourcesRepository).findByUserAndCategory(user, categoryId);
    }

    @Test
    @DisplayName("Should check if resource is saved")
    void testIsResourceSaved_ReturnsTrue() {
        // Arrange
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(resourceService.getResourceEntityById(resourceId)).thenReturn(resource);
        when(savedResourcesRepository.existsByUserAndResource(user, resource)).thenReturn(true);

        // Act
        boolean result = savedResourceService.isResourceSaved(userId, resourceId);

        // Assert
        assertTrue(result);

        verify(savedResourcesRepository).existsByUserAndResource(user, resource);
    }

    @Test
    @DisplayName("Should get saved resource count")
    void testGetSavedResourceCount_Success() {
        // Arrange
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(savedResourcesRepository.countByUser(user)).thenReturn(5L);

        // Act
        long result = savedResourceService.getSavedResourceCount(userId);

        // Assert
        assertEquals(5L, result);

        verify(savedResourcesRepository).countByUser(user);
    }

    @Test
    @DisplayName("Should save resource successfully")
    void testSaveResource_Success() {
        // Arrange
        SaveResourceRequest request = new SaveResourceRequest();
        request.setResourceId(resourceId);
        request.setNotes("My notes");

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(resourceService.getResourceEntityById(resourceId)).thenReturn(resource);
        when(savedResourcesRepository.existsByUserAndResource(user, resource)).thenReturn(false);
        when(xssProtection.sanitize("My notes")).thenReturn("My notes");
        when(savedResourcesRepository.save(any(SavedResource.class))).thenReturn(savedResource);
        when(savedResourceMapper.toDTO(savedResource)).thenReturn(savedResourceDTO);

        // Act
        SavedResourceDTO result = savedResourceService.saveResource(userId, request);

        // Assert
        assertNotNull(result);
        assertEquals(savedResourceId, result.getId());

        verify(savedResourcesRepository).save(any(SavedResource.class));
        verify(xssProtection).sanitize("My notes");
    }

    @Test
    @DisplayName("Should throw exception when saving already saved resource")
    void testSaveResource_AlreadySaved() {
        // Arrange
        SaveResourceRequest request = new SaveResourceRequest();
        request.setResourceId(resourceId);

        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(resourceService.getResourceEntityById(resourceId)).thenReturn(resource);
        when(savedResourcesRepository.existsByUserAndResource(user, resource)).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> savedResourceService.saveResource(userId, request));

        verify(savedResourcesRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should unsave resource successfully")
    void testUnsaveResource_Success() {
        // Arrange
        when(savedResourcesRepository.findById(savedResourceId)).thenReturn(Optional.of(savedResource));

        // Act
        savedResourceService.unsaveResource(userId, savedResourceId);

        // Assert
        verify(savedResourcesRepository).delete(savedResource);
    }

    @Test
    @DisplayName("Should throw exception when unsaving non-existent resource")
    void testUnsaveResource_NotFound() {
        // Arrange
        when(savedResourcesRepository.findById(savedResourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> savedResourceService.unsaveResource(userId, savedResourceId));

        verify(savedResourcesRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when unsaving another user's resource")
    void testUnsaveResource_WrongUser() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        when(savedResourcesRepository.findById(savedResourceId)).thenReturn(Optional.of(savedResource));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> savedResourceService.unsaveResource(differentUserId, savedResourceId));

        verify(savedResourcesRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should update notes successfully")
    void testUpdateNotes_Success() {
        // Arrange
        UpdateSavedResourceNotesRequest request = new UpdateSavedResourceNotesRequest();
        request.setNotes("Updated notes");

        when(savedResourcesRepository.findById(savedResourceId)).thenReturn(Optional.of(savedResource));
        when(inputSanitizer.sanitizeHtml("Updated notes")).thenReturn("Updated notes");
        when(savedResourcesRepository.save(savedResource)).thenReturn(savedResource);
        when(savedResourceMapper.toDTO(savedResource)).thenReturn(savedResourceDTO);

        // Act
        SavedResourceDTO result = savedResourceService.updateNotes(userId, savedResourceId, request);

        // Assert
        assertNotNull(result);

        verify(savedResourcesRepository).save(savedResource);
        verify(inputSanitizer).sanitizeHtml("Updated notes");
    }

    @Test
    @DisplayName("Should throw exception when updating notes on non-existent resource")
    void testUpdateNotes_NotFound() {
        // Arrange
        UpdateSavedResourceNotesRequest request = new UpdateSavedResourceNotesRequest();
        request.setNotes("Updated notes");

        when(savedResourcesRepository.findById(savedResourceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> savedResourceService.updateNotes(userId, savedResourceId, request));

        verify(savedResourcesRepository, never()).save(any());
    }
}
