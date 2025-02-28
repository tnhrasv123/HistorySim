package me.ThePCUser.historySim.technology;

import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.eras.EraType;
import me.ThePCUser.historySim.technology.Technology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages technologies for a specific era
 */
public class TechnologyManager {
    private final HistorySim plugin;
    private final EraType eraType;
    private final Map<String, Technology> technologies = new HashMap<>();

    public TechnologyManager(HistorySim plugin, EraType eraType) {
        this.plugin = plugin;
        this.eraType = eraType;
    }

    /**
     * Register a technology for this era
     */
    public void registerTechnology(Technology technology) {
        technologies.put(technology.getId(), technology);
    }

    /**
     * Get a technology by ID
     */
    public Technology getTechnology(String id) {
        return technologies.get(id);
    }

    /**
     * Get all technologies for this era
     */
    public List<Technology> getAllTechnologies() {
        return new ArrayList<>(technologies.values());
    }

    /**
     * Get technologies available for a player to research
     */
    public List<Technology> getAvailableTechnologies(UUID playerId) {
        List<Technology> available = new ArrayList<>();
        List<String> playerTechs = plugin.getDatabaseManager().getPlayerTechnologies(playerId);

        for (Technology tech : technologies.values()) {
            // Skip already unlocked
            if (playerTechs.contains(tech.getId())) {
                continue;
            }

            // Check prerequisites
            if (!tech.hasPrerequisite() || playerTechs.contains(tech.getPrerequisite())) {
                available.add(tech);
            }
        }

        return available;
    }

    /**
     * Check if a player can research a technology
     */
    public boolean canResearch(UUID playerId, String techId) {
        Technology tech = technologies.get(techId);
        if (tech == null) return false;

        // Check if already unlocked
        if (plugin.getDatabaseManager().hasPlayerTechnology(playerId, techId)) {
            return false;
        }

        // Check prerequisite
        if (tech.hasPrerequisite()) {
            return plugin.getDatabaseManager().hasPlayerTechnology(playerId, tech.getPrerequisite());
        }

        return true;
    }

    /**
     * Unlock a technology for a player
     */
    public boolean unlockTechnology(UUID playerId, String techId) {
        if (!canResearch(playerId, techId)) {
            return false;
        }

        plugin.getDatabaseManager().unlockTechnology(playerId, techId);
        return true;
    }
}