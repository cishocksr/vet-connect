package com.vetconnect.model.enums;

public enum UserRole {
    USER("User", "Standard user with basic access"),
    ADMIN("Administrator", "Full system access with management capabilities");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}