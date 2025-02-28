package me.ThePCUser.historySim.config;

import me.ThePCUser.historySim.HistorySim;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final HistorySim plugin;
    private FileConfiguration config;

    public ConfigManager(HistorySim plugin) {
        this.plugin = plugin;
    }

    public void loadAllConfigs() {
        // Load configuration settings
        config = plugin.getConfig();
        // Additional configuration loading logic can be added here
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
