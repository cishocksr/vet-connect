package com.vetconnect.mapper;

import com.vetconnect.dto.saved.SavedResourceDTO;
import com.vetconnect.dto.saved.SavedResourceSummaryDTO;
import com.vetconnect.model.SavedResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for SavedResource entity and SavedResource DTOs
 *
 * SPECIAL HANDLING:
 * - Formats "saved date" as relative time ("2 days ago")
 * - Includes nested resource information
 * - Handles notes properly
 */
@Component
@RequiredArgsConstructor
public class SavedResourceMapper {

    private final ResourceMapper resourceMapper;

    /**
     * Convert SavedResource entity to SavedResourceDTO
     *
     * @param savedResource The saved resource entity
     * @return SavedResourceDTO with full information
     */
    public SavedResourceDTO toDTO(SavedResource savedResource) {
        if (savedResource == null) {
            return null;
        }

        return SavedResourceDTO.builder()
                .id(savedResource.getId())
                .resource(resourceMapper.toSummaryDTO(savedResource.getResource()))
                .notes(savedResource.getNotes())
                .hasNotes(savedResource.hasNotes())
                .savedAt(savedResource.getSavedAt())
                .formattedSavedDate(formatRelativeTime(savedResource.getSavedAt()))
                .build();
    }

    /**
     * Convert SavedResource entity to SavedResourceSummaryDTO (lightweight)
     *
     * @param savedResource The saved resource entity
     * @return SavedResourceSummaryDTO with minimal information
     */
    public SavedResourceSummaryDTO toSummaryDTO(SavedResource savedResource) {
        if (savedResource == null) {
            return null;
        }

        return SavedResourceSummaryDTO.builder()
                .id(savedResource.getId())
                .resourceId(savedResource.getResource().getId())
                .resourceName(savedResource.getResource().getName())
                .categoryName(savedResource.getResource().getCategory() != null ?
                        savedResource.getResource().getCategory().getName() : null)
                .categoryIconName(savedResource.getResource().getCategory() != null ?
                        savedResource.getResource().getCategory().getIconName() : null)
                .hasNotes(savedResource.hasNotes())
                .savedAt(savedResource.getSavedAt())
                .build();
    }

    /**
     * Convert list of saved resources to full DTOs
     *
     * @param savedResources List of saved resource entities
     * @return List of SavedResourceDTOs
     */
    public List<SavedResourceDTO> toDTOList(List<SavedResource> savedResources) {
        if (savedResources == null) {
            return null;
        }

        return savedResources.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of saved resources to summary DTOs
     *
     * @param savedResources List of saved resource entities
     * @return List of SavedResourceSummaryDTOs
     */
    public List<SavedResourceSummaryDTO> toSummaryDTOList(List<SavedResource> savedResources) {
        if (savedResources == null) {
            return null;
        }

        return savedResources.stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Format LocalDateTime as relative time string
     *
     * Examples:
     * - "Just now" (< 1 minute)
     * - "5 minutes ago"
     * - "2 hours ago"
     * - "3 days ago"
     * - "2 weeks ago"
     * - "1 month ago"
     * - "Jan 15, 2024" (> 1 year)
     *
     * @param dateTime The datetime to format
     * @return Human-readable relative time string
     */
    private String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        if (minutes < 1) {
            return "Just now";
        }
        if (minutes < 60) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }

        long hours = ChronoUnit.HOURS.between(dateTime, now);
        if (hours < 24) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }

        long days = ChronoUnit.DAYS.between(dateTime, now);
        if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        }

        long months = ChronoUnit.MONTHS.between(dateTime, now);
        if (months < 12) {
            return months + (months == 1 ? " month ago" : " months ago");
        }

        // For dates > 1 year ago, show actual date
        return dateTime.getMonthValue() + "/" +
                dateTime.getDayOfMonth() + "/" +
                dateTime.getYear();
    }
}