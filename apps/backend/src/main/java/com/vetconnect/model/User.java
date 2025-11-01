package com.vetconnect.model;

import com.vetconnect.model.enums.BranchOfService;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "First name is required")
    @Size(max =  100, message = "Last name must be less than 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be less than 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull(message = "Branch of service is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "branch_of_service", nullable = false, length = 50)
    private BranchOfService branchOfService;


    @Size(max = 255, message = "Address line 1 must be less than 255 characters")
    @Column(name = "address_line1", length =255)
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must be less than 255 characters")
    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Size(max = 100, message = "City must be less than 100 chatacters")
    @Column(name = "city", length = 100)
    private String city;

    @Size(min = 2, max = 2, message = "State must be exactly 2 chatacters")
    @Column(name="state", length = 2)
    private String state;

    @Size(max = 10, message = "Zip code must be less than 10 characters")
    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "is_homeless", nullable = false)
    @Builder.Default
    private boolean isHomeless = false;

    @Size(max = 500, message = "Profile picture URL must be less than 500 characters")
    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasCompleteAddress() {
        return addressLine1 != null && !addressLine1.isBlank()
                && city != null && !city.isBlank()
                && state != null && !state.isBlank()
                && zipCode != null && !zipCode.isBlank();
    }





}
