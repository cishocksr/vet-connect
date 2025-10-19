package com.vetconnect.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="saved_resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public String getFormattedSavedDate() {
        return savedAt.toString(); // TODO: Implement relative time formatting
    }

    public boolean hasNotes() {
        return notes != null && !notes.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedResource that = (SavedResource) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SavedResource{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", resourceId=" + (resource != null ? resource.getId() : "null") +
                ", savedAt=" + savedAt +
                ", hasNotes=" + hasNotes() +
                '}';
    }

}
