package io.github.kaltrinabajramii.urbantransitbackend.model.enums;

public enum TicketStatus {
    ACTIVE("Active - can be used for transit"),
    USED("Used - single ride consumed"),
    EXPIRED("Expired - validity period ended");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}