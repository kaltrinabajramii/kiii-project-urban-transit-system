package io.github.kaltrinabajramii.urbantransitbackend.model.enums;

public enum TransportType {
    BUS("Bus", "🚌"),
    METRO("Metro/Subway", "🚇"),
    TRAM("Tram", "🚋"),
    TRAIN("Train", "🚆");

    private final String displayName;
    private final String icon;

    TransportType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}