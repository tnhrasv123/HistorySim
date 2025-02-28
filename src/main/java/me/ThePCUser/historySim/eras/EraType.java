package me.ThePCUser.historySim.eras;

/**
 * Enum of available historical eras
 */
public enum EraType {
    PREHISTORIC("Prehistoric Era"),
    ANCIENT("Ancient Era"),
    MEDIEVAL("Medieval Era"),
    RENAISSANCE("Renaissance Era"),
    INDUSTRIAL("Industrial Era"),
    MODERN("Modern Era");

    private final String displayName;

    EraType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}