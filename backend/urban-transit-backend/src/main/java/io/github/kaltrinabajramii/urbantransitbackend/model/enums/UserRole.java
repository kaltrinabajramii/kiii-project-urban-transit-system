package io.github.kaltrinabajramii.urbantransitbackend.model.enums;

public enum UserRole {
    USER("Regular User"),
    ADMIN("System Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
