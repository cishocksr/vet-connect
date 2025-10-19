package com.vetconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private ResourceCategory category;

    /**
     *  RESOURCE NAME
     *
     * Examples: "VA Greater Los Angeles", "Homeless Veterans Reintegration Program"
     */
    @NotBlank(message = "Resource name is required")
    @Size(max = 255, message = "Resource name must be less than 255 characters")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * DESCRIPTION
     *
     * Detailed information about what this resource provides
     * Example: "Provides emergency shelter, meals, case management, and job placement
     *          services for homeless veterans in the Los Angeles area."
     *
     * columnDefinition = "TEXT" allows unlimited length (PostgreSQL TEXT type)
     */
    @NotBlank(message = "Description is required")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * CONTACT INFORMATION
     *
     * These fields are all OPTIONAL because:
     * - Some resources only have a website
     * - Some only have a phone number
     * - Some may be walk-in only
     *
     * But at least ONE should be provided (validated in service layer)
     */
    @Size(max = 500, message = "Website URL must be less than 500 characters")
    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Size(max = 20, message = "Phone number must be less than 20 characters")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    @Column(name = "email", length = 255)
    private String email;

    /**
     * LOCATION INFORMATION
     *
     * Physical address of the resource (if applicable)
     *
     * WHY OPTIONAL?
     * Some resources are:
     * - Online only (hotlines, websites)
     * - National programs (no single address)
     * - Multiple locations
     */

    @Size(max = 255, message = "Address line 1 must be less than 255 characters")
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Size(max = 100, message = "City must be less than 100 characters")
    @Column(name = "city", length = 100)
    private String city;

    @Size(min = 2, max = 2, message = "State must be exactly 2 characters")
    @Column(name = "state", length = 2)
    private String state;

    @Size(max = 10, message = "Zip code must be less than 10 characters")
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    /**
     * IS NATIONAL?
     *
     * TRUE = Available nationwide (VA Benefits Hotline)
     * FALSE = Local resource (specific city/state)
     *
     *
     */
    @Column(name = "is_national", nullable = false)
    @Builder.Default
    private Boolean isNational = false;

    /**
     * ELIGIBILITY CRITERIA
     *
     * Who can use this resource?
     *
     * Stored as TEXT for flexibility
     */
    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    public boolean hasContactInfo() {
        return (websiteUrl != null && !websiteUrl.isBlank()) ||
                (phoneNumber != null && !phoneNumber.isBlank()) ||
                (email != null && !email.isBlank()) ||
                (addressLine1 != null && !addressLine1.isBlank());
    }

    public boolean hasCompleteAddress() {
        return addressLine1 != null && !addressLine1.isBlank() &&
                city != null && !city.isBlank() &&
                state != null && !state.isBlank() &&
                zipCode != null && !zipCode.isBlank();
    }

    public String getLocationDisplay() {
        if (isNational) {
            return "National";
        }

        if (city != null && state != null) {
            String location = city + ", " + state;
            if (zipCode != null) {
                location += " " + zipCode;
            }
            return location;
        }

        return "Location not specified";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Resource resource = (Resource) o;
        return id != null && id.equals(resource.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + (category != null ? category.getName() : "null") +
                ", isNational=" + isNational +
                '}';
    }


}
