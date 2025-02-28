package me.ThePCUser.historySim.eras;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Interface representing a historical era
 */
public interface Era {
    /**
     * Initialize the era's systems
     */
    void initialize();

    /**
     * Apply era-specific effects to a player
     */
    void applyToPlayer(Player player);

    /**
     * Save a player's era-specific data
     */
    void savePlayerData(Player player);

    /**
     * Configure world settings for this era
     */
    void configureWorld(World world);

    /**
     * Get the era type
     */
    EraType getType();

    /**
     * Get display name of the era
     */
    String getDisplayName();
}