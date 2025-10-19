package com.vetconnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "resource_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be less than 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    @Column(name= "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 50, message = "Icon name must be less than 50 characters")
    @Column(name = "icon_name", length = 50)
    private String iconName;

    @Override
    public String toString() {
        return "ResourceCategory{" +
                "id=" + id +
                ", name=' " + name + '\'' +
                ", iconName='" + iconName + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
