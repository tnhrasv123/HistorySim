package me.ThePCUser.historySim;

import me.ThePCUser.historySim.commands.AdminCommands;
import me.ThePCUser.historySim.commands.PlayerCommands;
import me.ThePCUser.historySim.config.ConfigManager;
import me.ThePCUser.historySim.database.DatabaseManager;
import me.ThePCUser.historySim.eras.*;
import me.ThePCUser.historySim.events.EventManager;
import me.ThePCUser.historySim.listeners.PlayerListener;
import me.ThePCUser.historySim.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HistorySim extends JavaPlugin {

    private static HistorySim instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private EventManager eventManager;

    // Era managers
    private Map<EraType, Era> eras = new HashMap<>();
    private Map<UUID, EraType> playerEras = new HashMap<>();

    // World management
    private Map<EraType, World> eraWorlds = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        this.saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.loadAllConfigs();

        // Initialize database
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();
        databaseManager.createTables();

        // Register eras
        registerEras();

        // Initialize worlds
        initializeWorlds();

        // Initialize event manager
        eventManager = new EventManager(this);

        // Register commands
        getCommand("historyera").setExecutor(new PlayerCommands(this));
        getCommand("historyadmin").setExecutor(new AdminCommands(this));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        MessageUtils.logInfo("History Simulator has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        for (Player player : Bukkit.getOnlinePlayers()) {
            databaseManager.savePlayerData(player.getUniqueId());
        }

        // Close database connection
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        MessageUtils.logInfo("History Simulator has been disabled!");
    }

    private void registerEras() {
        // Register all historical eras
        eras.put(EraType.PREHISTORIC, new PrehistoricEra(this));
        eras.put(EraType.ANCIENT, new AncientEra(this));
        eras.put(EraType.MEDIEVAL, new MedievalEra(this));
        eras.put(EraType.RENAISSANCE, new RenaissanceEra(this));
        eras.put(EraType.INDUSTRIAL, new IndustrialEra(this));
        eras.put(EraType.MODERN, new ModernEra(this));

        // Initialize each era
        for (Era era : eras.values()) {
            era.initialize();
        }
    }

    private void initializeWorlds() {
        FileConfiguration config = getConfig();

        // Create or load worlds for each era
        for (EraType eraType : EraType.values()) {
            String worldName = "history_" + eraType.name().toLowerCase();

            // Check if world exists or should be created
            if (config.getBoolean("worlds." + worldName + ".enabled", true)) {
                MessageUtils.logInfo("Creating/Loading world: " + worldName);

                WorldCreator creator = new WorldCreator(worldName);
                // Set custom generator if specified
                if (config.contains("worlds." + worldName + ".generator")) {
                    creator.generator(config.getString("worlds." + worldName + ".generator"));
                }

                World world = Bukkit.createWorld(creator);
                eraWorlds.put(eraType, world);

                // Apply era-specific world settings
                eras.get(eraType).configureWorld(world);
            }
        }
    }

    /**
     * Changes a player's era and teleports them to the corresponding world
     * @param player The player to change era for
     * @param targetEra The era to move the player to
     * @return True if successful, false otherwise
     */
    public boolean changePlayerEra(Player player, EraType targetEra) {
        // Verify era exists and is enabled
        if (!eras.containsKey(targetEra) || !eraWorlds.containsKey(targetEra)) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        EraType currentEra = getPlayerEra(playerId);

        // Save player's current era data
        if (currentEra != null) {
            eras.get(currentEra).savePlayerData(player);
        }

        // Update player's era
        playerEras.put(playerId, targetEra);
        databaseManager.updatePlayerEra(playerId, targetEra);

        // Teleport player to the new era world
        World targetWorld = eraWorlds.get(targetEra);
        player.teleport(targetWorld.getSpawnLocation());

        // Apply era-specific effects and abilities
        eras.get(targetEra).applyToPlayer(player);

        MessageUtils.sendMessage(player, "You have entered the " + targetEra.getDisplayName() + " era!");
        return true;
    }

    /**
     * Get a player's current era
     * @param playerId The UUID of the player
     * @return The player's current era, or default if not set
     */
    public EraType getPlayerEra(UUID playerId) {
        // Check cache first
        if (playerEras.containsKey(playerId)) {
            return playerEras.get(playerId);
        }

        // Load from database
        EraType era = databaseManager.getPlayerEra(playerId);

        // If not found, set to default
        if (era == null) {
            era = EraType.PREHISTORIC; // Default starting era
        }

        // Cache the result
        playerEras.put(playerId, era);
        return era;
    }

    /**
     * Get an era by type
     * @param type The era type
     * @return The era instance
     */
    public Era getEra(EraType type) {
        return eras.get(type);
    }

    /**
     * Get the world for a specific era
     * @param eraType The era type
     * @return The world for that era
     */
    public World getEraWorld(EraType eraType) {
        return eraWorlds.get(eraType);
    }

    public static HistorySimulator getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}