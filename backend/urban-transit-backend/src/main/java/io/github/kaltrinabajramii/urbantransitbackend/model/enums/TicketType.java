package io.github.kaltrinabajramii.urbantransitbackend.model.enums;

public enum TicketType {
    RIDE("Single Ride Ticket", 1, "Valid for one ride on any bus, train, metro, or tram"),
    MONTHLY("Monthly Pass", 30, "Unlimited rides on all transit types for 30 days"),
    YEARLY("Yearly Pass", 365, "Unlimited rides on all transit types for 365 days");

    private final String displayName;
    private final int validityDays;
    private final String description;

    TicketType(String displayName, int validityDays, String description) {
        this.displayName = displayName;
        this.validityDays = validityDays;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getValidityDays() {
        return validityDays;
    }

    public String getDescription() {
        return description;
    }

    // Helper method to check if this ticket type allows unlimited rides
    public boolean isUnlimitedRides() {
        return this == MONTHLY || this == YEARLY;
    }
}
