package me.ThePCUser.historySim.events;


import me.ThePCUser.historySim.eras.EraType;

/**
 * Represents a historical event that can occur in the game
 */
public class HistoricalEvent {
    private final String id;
    private final String name;
    private final String description;
    private final EraType era;

    public HistoricalEvent(String id, String name, String description, EraType era) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.era = era;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EraType getEra() {
        return era;
    }
}