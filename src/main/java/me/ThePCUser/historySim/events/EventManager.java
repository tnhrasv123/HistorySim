package me.ThePCUser.historySim.events;

import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.eras.EraType;
import me.ThePCUser.historySim.utils.MessageUtils;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages historical events that can occur in the game
 */
public class EventManager {

    private final HistorySim plugin;
    private final Map<String, HistoricalEvent> events = new HashMap<>();

    public EventManager(HistorySim plugin) {
        this.plugin = plugin;
        loadEvents();
    }

    /**
     * Load events from configuration
     */
    private void loadEvents() {
        FileConfiguration config = plugin.getConfigManager().getEventsConfig();
        ConfigurationSection eventsSection = config.getConfigurationSection("events");

        if (eventsSection == null) {
            MessageUtils.logWarning("No events defined in configuration!");
            return;
        }

        for (String eventId : eventsSection.getKeys(false)) {
            ConfigurationSection eventConfig = eventsSection.getConfigurationSection(eventId);

            if (eventConfig == null) continue;

            String name = eventConfig.getString("name", "Unknown Event");
            String description = eventConfig.getString("description", "");
            String eraName = eventConfig.getString("era", "PREHISTORIC");

            try {
                HistoricalEvent event = new HistoricalEvent(
                        eventId,
                        name,
                        description,
                        EraType.valueOf(eraName)
                );

                events.put(eventId, event);
                MessageUtils.logInfo("Loaded historical event: " + name);
            } catch (IllegalArgumentException e) {
                MessageUtils.logWarning("Invalid era for event " + eventId + ": " + eraName);
            }
        }
    }

    /**
     * Trigger a historical event in a world
     */
    public boolean triggerEvent(String eventId, World world) {
        HistoricalEvent event = events.get(eventId);
        if (event == null) {
            return false;
        }

        // Execute the event effects
        for (Player player : world.getPlayers()) {
            MessageUtils.sendMessage(player, "§6Historical Event: §e" + event.getName());
            MessageUtils.sendMessage(player, "§7" + event.getDescription());

            // Apply event effects (would be expanded in full implementation)
            // For now, just give some experience
            player.giveExp(50);
        }

        return true;
    }

    /**
     * Get event by ID
     */
    public HistoricalEvent getEvent(String eventId) {
        return events.get(eventId);
    }
}
