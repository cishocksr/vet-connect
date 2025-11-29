package com.vetconnect.mapper;

import com.vetconnect.dto.resource.ResourceSummaryDTO;
import com.vetconnect.dto.saved.SavedResourceDTO;
import com.vetconnect.dto.saved.SavedResourceSummaryDTO;
import com.vetconnect.model.Resource;
import com.vetconnect.model.ResourceCategory;
import com.vetconnect.model.SavedResource;
import com.vetconnect.model.User;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SavedResourceMapper Tests")
class SavedResourceMapperTest {

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private SavedResourceMapper savedResourceMapper;

    private SavedResource savedResource;
    private Resource resource;
    private ResourceCategory category;
    private User user;
    private ResourceSummaryDTO resourceSummaryDTO;

    @BeforeEach
    void setUp() {
        category = ResourceCategory.builder()
                .id(1)
                .name("Housing")
                .iconName("home")
                .build();

        resource = Resource.builder()
                .id(UUID.randomUUID())
                .name("Test Resource")
                .category(category)
                .build();

        user = new User();
        user.setId(UUID.randomUUID());

        savedResource = SavedResource.builder()
                .id(UUID.randomUUID())
                .user(user)
                .resource(resource)
                .notes("Test notes")
                .savedAt(LocalDateTime.now().minusHours(2))
                .build();

        resourceSummaryDTO = ResourceSummaryDTO.builder()
                .id(resource.getId())
                .name("Test Resource")
                .categoryName("Housing")
                .categoryIconName("home")
                .build();
    }

    @Test
    @DisplayName("Should convert SavedResource to DTO")
    void testToDTO_Success() {
        // Arrange
        when(resourceMapper.toSummaryDTO(resource)).thenReturn(resourceSummaryDTO);

        // Act
        SavedResourceDTO result = savedResourceMapper.toDTO(savedResource);

        // Assert
        assertNotNull(result);
        assertEquals(savedResource.getId(), result.getId());
        assertEquals("Test notes", result.getNotes());
        assertTrue(result.isHasNotes());
        assertNotNull(result.getResource());
        assertEquals("2 hours ago", result.getFormattedSavedDate());

        verify(resourceMapper).toSummaryDTO(resource);
    }

    @Test
    @DisplayName("Should return null when SavedResource is null")
    void testToDTO_Null() {
        // Act
        SavedResourceDTO result = savedResourceMapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert SavedResource to SummaryDTO")
    void testToSummaryDTO_Success() {
        // Act
        SavedResourceSummaryDTO result = savedResourceMapper.toSummaryDTO(savedResource);

        // Assert
        assertNotNull(result);
        assertEquals(savedResource.getId(), result.getId());
        assertEquals(resource.getId(), result.getResourceId());
        assertEquals("Test Resource", result.getResourceName());
        assertEquals("Housing", result.getCategoryName());
        assertEquals("home", result.getCategoryIconName());
        assertTrue(result.isHasNotes());
    }

    @Test
    @DisplayName("Should return null when converting null to SummaryDTO")
    void testToSummaryDTO_Null() {
        // Act
        SavedResourceSummaryDTO result = savedResourceMapper.toSummaryDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle resource without category in SummaryDTO")
    void testToSummaryDTO_NoCategory() {
        // Arrange
        Resource resourceWithoutCategory = Resource.builder()
                .id(UUID.randomUUID())
                .name("Test Resource")
                .category(null)
                .build();

        SavedResource savedResourceNoCategory = SavedResource.builder()
                .id(UUID.randomUUID())
                .user(user)
                .resource(resourceWithoutCategory)
                .notes("Test notes")
                .savedAt(LocalDateTime.now())
                .build();

        // Act
        SavedResourceSummaryDTO result = savedResourceMapper.toSummaryDTO(savedResourceNoCategory);

        // Assert
        assertNotNull(result);
        assertNull(result.getCategoryName());
        assertNull(result.getCategoryIconName());
    }

    @Test
    @DisplayName("Should convert list to DTO list")
    void testToDTOList_Success() {
        // Arrange
        List<SavedResource> savedResources = Arrays.asList(savedResource);
        when(resourceMapper.toSummaryDTO(resource)).thenReturn(resourceSummaryDTO);

        // Act
        List<SavedResourceDTO> result = savedResourceMapper.toDTOList(savedResources);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(savedResource.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Should return null when list is null")
    void testToDTOList_Null() {
        // Act
        List<SavedResourceDTO> result = savedResourceMapper.toDTOList(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should convert list to SummaryDTO list")
    void testToSummaryDTOList_Success() {
        // Arrange
        List<SavedResource> savedResources = Arrays.asList(savedResource);

        // Act
        List<SavedResourceSummaryDTO> result = savedResourceMapper.toSummaryDTOList(savedResources);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(savedResource.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Should return null when summary list is null")
    void testToSummaryDTOList_Null() {
        // Act
        List<SavedResourceSummaryDTO> result = savedResourceMapper.toSummaryDTOList(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should format time as 'Just now'")
    void testFormatRelativeTime_JustNow() {
        // Arrange
        savedResource.setSavedAt(LocalDateTime.now());
        when(resourceMapper.toSummaryDTO(resource)).thenReturn(resourceSummaryDTO);

        // Act
        SavedResourceDTO result = savedResourceMapper.toDTO(savedResource);

        // Assert
        assertEquals("Just now", result.getFormattedSavedDate());
    }

    @Test
    @DisplayName("Should format time in minutes")
    void testFormatRelativeTime_Minutes() {
        // Arrange
        savedResource.setSavedAt(LocalDateTime.now().minusMinutes(5));
        when(resourceMapper.toSummaryDTO(resource)).thenReturn(resourceSummaryDTO);

        // Act
        SavedResourceDTO result = savedResourceMapper.toDTO(savedResource);

        // Assert
        assertTrue(result.getFormattedSavedDate().contains("minute"));
    }

    @Test
    @DisplayName("Should format time in days")
    void testFormatRelativeTime_Days() {
        // Arrange
        savedResource.setSavedAt(LocalDateTime.now().minusDays(3));
        when(resourceMapper.toSummaryDTO(resource)).thenReturn(resourceSummaryDTO);

        // Act
        SavedResourceDTO result = savedResourceMapper.toDTO(savedResource);

        // Assert
        assertEquals("3 days ago", result.getFormattedSavedDate());
    }

    @Test
    @DisplayName("Should format time in weeks")
    void testFormatRelativeTime_Weeks() {
        // Arrange
        savedResource.setSavedAt(LocalDateTime.now().minusWeeks(2));
        when(resourceMapper.toSummaryDTO(resource)).thenReturn(resourceSummaryDTO);

        // Act
        SavedResourceDTO result = savedResourceMapper.toDTO(savedResource);

        // Assert
        assertEquals("2 weeks ago", result.getFormattedSavedDate());
    }

    @Test
    @DisplayName("Should format time in months")
    void testFormatRelativeTime_Months() {
        // Arrange
        savedResource.setSavedAt(LocalDateTime.now().minusMonths(3));
        when(resourceMapper.toSummaryDTO(resource)).thenReturn(resourceSummaryDTO);

        // Act
        SavedResourceDTO result = savedResourceMapper.toDTO(savedResource);

        // Assert
        assertEquals("3 months ago", result.getFormattedSavedDate());
    }

    @Test
    @DisplayName("Should format time as date for old dates")
    void testFormatRelativeTime_OldDate() {
        // Arrange
        LocalDateTime oldDate = LocalDateTime.of(2023, 1, 15, 10, 30);
        savedResource.setSavedAt(oldDate);
        when(resourceMapper.toSummaryDTO(resource)).thenReturn(resourceSummaryDTO);

        // Act
        SavedResourceDTO result = savedResourceMapper.toDTO(savedResource);

        // Assert
        assertTrue(result.getFormattedSavedDate().contains("2023"));
    }
}
