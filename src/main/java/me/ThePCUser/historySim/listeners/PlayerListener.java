package me.ThePCUser.historySim.listeners;

import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.eras.Era;
import me.ThePCUser.historySim.eras.EraType;
import me.ThePCUser.historySim.progression.ProgressionManager;
import me.ThePCUser.historySim.progression.Achievement;
import me.ThePCUser.historySim.utils.MessageUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;

/**
 * Handles player-related events and manages era-specific interactions
 */
public class PlayerListener implements Listener {

    private final HistorySim plugin;
    private final Map<Material, Integer> resourcePoints = new HashMap<>();
    private final Map<Material, List<EraType>> eraRestrictedItems = new HashMap<>();
    private final ProgressionManager progressionManager;

    public PlayerListener(HistorySim plugin) {
        this.plugin = plugin;
        this.progressionManager = plugin.getProgressionManager();
        initResourcePoints();
        initEraRestrictions();
    }

    private void initResourcePoints() {
        // Points awarded for gathering different resources
        resourcePoints.put(Material.STONE, 1);
        resourcePoints.put(Material.COAL_ORE, 2);
        resourcePoints.put(Material.IRON_ORE, 3);
        resourcePoints.put(Material.GOLD_ORE, 5);
        resourcePoints.put(Material.DIAMOND_ORE, 10);
        resourcePoints.put(Material.OAK_LOG, 1);
        resourcePoints.put(Material.BIRCH_LOG, 1);
        resourcePoints.put(Material.SPRUCE_LOG, 1);
        resourcePoints.put(Material.JUNGLE_LOG, 1);
        resourcePoints.put(Material.ACACIA_LOG, 1);
        resourcePoints.put(Material.DARK_OAK_LOG, 1);

        // Agricultural resources
        resourcePoints.put(Material.WHEAT, 2);
        resourcePoints.put(Material.POTATO, 2);
        resourcePoints.put(Material.CARROT, 2);
        resourcePoints.put(Material.BEETROOT, 2);

        // Building materials
        resourcePoints.put(Material.CLAY, 2);
        resourcePoints.put(Material.BRICK, 3);
    }

    private void initEraRestrictions() {
        // Materials restricted by era
        eraRestrictedItems.put(Material.IRON_INGOT, Arrays.asList(EraType.PREHISTORIC));
        eraRestrictedItems.put(Material.IRON_SWORD, Arrays.asList(EraType.PREHISTORIC));
        eraRestrictedItems.put(Material.IRON_PICKAXE, Arrays.asList(EraType.PREHISTORIC));
        eraRestrictedItems.put(Material.IRON_AXE, Arrays.asList(EraType.PREHISTORIC));
        eraRestrictedItems.put(Material.IRON_HOE, Arrays.asList(EraType.PREHISTORIC));
        eraRestrictedItems.put(Material.IRON_HELMET, Arrays.asList(EraType.PREHISTORIC));
        eraRestrictedItems.put(Material.IRON_CHESTPLATE, Arrays.asList(EraType.PREHISTORIC));
        eraRestrictedItems.put(Material.IRON_LEGGINGS, Arrays.asList(EraType.PREHISTORIC));
        eraRestrictedItems.put(Material.IRON_BOOTS, Arrays.asList(EraType.PREHISTORIC));

        // Advanced materials restricted to later eras
        eraRestrictedItems.put(Material.DIAMOND, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT));
        eraRestrictedItems.put(Material.DIAMOND_SWORD, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT));
        eraRestrictedItems.put(Material.DIAMOND_PICKAXE, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT));

        // Redstone components restricted to Renaissance and later
        eraRestrictedItems.put(Material.REDSTONE, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT, EraType.MEDIEVAL));
        eraRestrictedItems.put(Material.REDSTONE_TORCH, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT, EraType.MEDIEVAL));
        eraRestrictedItems.put(Material.PISTON, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT, EraType.MEDIEVAL));
        eraRestrictedItems.put(Material.OBSERVER, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT, EraType.MEDIEVAL));
        eraRestrictedItems.put(Material.COMPARATOR, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT, EraType.MEDIEVAL));

        // Industrial and Modern era items
        eraRestrictedItems.put(Material.RAIL, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT, EraType.MEDIEVAL, EraType.RENAISSANCE));
        eraRestrictedItems.put(Material.POWERED_RAIL, Arrays.asList(EraType.PREHISTORIC, EraType.ANCIENT, EraType.MEDIEVAL, EraType.RENAISSANCE));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Load player's era
        EraType era = plugin.getDatabaseManager().getPlayerEra(playerId);
        if (era == null) {
            // New player, set default era
            era = EraType.PREHISTORIC;
            plugin.getDatabaseManager().updatePlayerEra(playerId, era);
        }

        // Store player era in memory
        plugin.getPlayerEra(playerId); // This loads and caches from DB

        // Apply era effects
        Era playerEra = plugin.getEra(era);
        if (playerEra != null) {
            playerEra.applyToPlayer(player);

            // Welcome message with era info
            MessageUtils.sendFormattedMessage(player,
                    "&6Welcome to the &b" + era.getDisplayName() + " &6era!");
            MessageUtils.sendFormattedMessage(player,
                    "&7Current global era: &b" + plugin.getGlobalEra().getDisplayName());
        }

        // Check for completed achievements
        progressionManager.updatePlayerAchievements(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Save player data
        plugin.getDatabaseManager().savePlayerData(playerId);

        // Save era-specific data
        EraType era = plugin.getPlayerEra(playerId);
        if (era != null) {
            Era playerEra = plugin.getEra(era);
            if (playerEra != null) {
                playerEra.savePlayerData(player);
            }
        }

        // Save progression data
        progressionManager.savePlayerProgress(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material material = event.getBlock().getType();

        // Award research points for gathering resources
        if (resourcePoints.containsKey(material)) {
            UUID playerId = player.getUniqueId();
            EraType era = plugin.getPlayerEra(playerId);

            int points = resourcePoints.get(material);

            // Add to player's research points
            progressionManager.addResearchPoints(player, points);

            // Log for debugging
            plugin.getLogger().info(player.getName() + " earned " + points +
                    " research points in " + era.getDisplayName());

            // Check for resource gathering achievements
            checkResourceAchievements(player, material);
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        EraType era = plugin.getPlayerEra(playerId);

        ItemStack result = event.getRecipe().getResult();
        Material craftedMaterial = result.getType();

        // Check if this material is restricted in player's era
        if (eraRestrictedItems.containsKey(craftedMaterial) &&
                eraRestrictedItems.get(craftedMaterial).contains(era)) {
            event.setCancelled(true);
            MessageUtils.sendFormattedMessage(player,
                    "&cYou cannot craft this item in the " + era.getDisplayName() + " era!");
            return;
        }

        // Award crafting progress
        progressionManager.addCraftingProgress(player, craftedMaterial, result.getAmount());

        // Check for crafting achievements
        checkCraftingAchievements(player, craftedMaterial);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        EraType era = plugin.getPlayerEra(playerId);
        Material placedMaterial = event.getBlock().getType();

        // Check if this material is restricted in player's era
        if (eraRestrictedItems.containsKey(placedMaterial) &&
                eraRestrictedItems.get(placedMaterial).contains(era)) {
            event.setCancelled(true);
            MessageUtils.sendFormattedMessage(player,
                    "&cYou cannot use this block in the " + era.getDisplayName() + " era!");
            return;
        }

        // Track building progress
        progressionManager.addBlockPlaced(player, placedMaterial);

        // Check for building achievements
        checkBuildingAchievements(player, placedMaterial);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();
        EraType era = plugin.getPlayerEra(playerId);

        // Check if player is using era-appropriate weapons
        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (weapon != null && eraRestrictedItems.containsKey(weapon.getType()) &&
                eraRestrictedItems.get(weapon.getType()).contains(era)) {
            event.setCancelled(true);
            MessageUtils.sendFormattedMessage(player,
                    "&cYou cannot use this weapon in the " + era.getDisplayName() + " era!");
            return;
        }

        // Track combat progress
        progressionManager.addCombatProgress(player, event.getEntityType(), event.getDamage());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;

        Player player = event.getEntity().getKiller();
        EntityType entityType = event.getEntityType();

        // Award hunting progress
        progressionManager.addHuntingProgress(player, entityType);

        // Check for hunting achievements
        checkHuntingAchievements(player, entityType);
    }

    @EventHandler
    public void onHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        Material crop = event.getHarvestedBlock().getType();

        // Track farming progress
        progressionManager.addFarmingProgress(player, crop);

        // Check for farming achievements
        checkFarmingAchievements(player, crop);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        EraType era = plugin.getPlayerEra(playerId);

        // Handle era-specific interactions
        if (event.hasBlock()) {
            Material blockType = event.getClickedBlock().getType();

            // Era-specific interaction handling
            switch (era) {
                case PREHISTORIC:
                    handlePrehistoricInteraction(player, event, blockType);
                    break;
                case ANCIENT:
                    handleAncientInteraction(player, event, blockType);
                    break;
                case MEDIEVAL:
                    handleMedievalInteraction(player, event, blockType);
                    break;
                case RENAISSANCE:
                    handleRenaissanceInteraction(player, event, blockType);
                    break;
                case INDUSTRIAL:
                    handleIndustrialInteraction(player, event, blockType);
                    break;
                case MODERN:
                    handleModernInteraction(player, event, blockType);
                    break;
            }
        }
    }

    // Era-specific interaction handlers
    private void handlePrehistoricInteraction(Player player, PlayerInteractEvent event, Material blockType) {
        // Implement prehistoric-specific interactions
        // For example, fire discovery mechanics
        if (blockType == Material.CAMPFIRE || blockType == Material.SOUL_CAMPFIRE) {
            progressionManager.recordMilestone(player, "fire_discovery");
            if (!progressionManager.hasMilestone(player, "fire_discovery")) {
                MessageUtils.sendFormattedMessage(player, "&6You have discovered fire! New crafting recipes unlocked.");
            }
        }
    }

    private void handleAncientInteraction(Player player, PlayerInteractEvent event, Material blockType) {
        // Implement ancient civilization-specific interactions
        if (blockType == Material.LOOM) {
            progressionManager.recordMilestone(player, "textile_development");
        } else if (blockType == Material.SMITHING_TABLE) {
            progressionManager.recordMilestone(player, "metallurgy_advancement");
        }
    }

    private void handleMedievalInteraction(Player player, PlayerInteractEvent event, Material blockType) {
        // Implement medieval-specific interactions
        if (blockType == Material.ANVIL) {
            progressionManager.recordMilestone(player, "advanced_smithing");
        } else if (blockType == Material.LECTERN) {
            progressionManager.recordMilestone(player, "literacy_development");
        }
    }

    private void handleRenaissanceInteraction(Player player, PlayerInteractEvent event, Material blockType) {
        // Implement renaissance-specific interactions
        if (blockType == Material.CARTOGRAPHY_TABLE) {
            progressionManager.recordMilestone(player, "cartography_advancement");
        }
    }

    private void handleIndustrialInteraction(Player player, PlayerInteractEvent event, Material blockType) {
        // Implement industrial-specific interactions
        if (blockType == Material.BLAST_FURNACE) {
            progressionManager.recordMilestone(player, "industrial_smelting");
        } else if (blockType == Material.RAIL || blockType == Material.POWERED_RAIL) {
            progressionManager.recordMilestone(player, "railroad_development");
        }
    }

    private void handleModernInteraction(Player player, PlayerInteractEvent event, Material blockType) {
        // Implement modern-specific interactions
        if (blockType == Material.REDSTONE_COMPARATOR || blockType == Material.OBSERVER) {
            progressionManager.recordMilestone(player, "advanced_electronics");
        }
    }

    // Achievement check methods
    private void checkResourceAchievements(Player player, Material material) {
        // Check resource-gathering related achievements
        progressionManager.checkResourceAchievements(player, material);
    }

    private void checkCraftingAchievements(Player player, Material material) {
        // Check crafting related achievements
        progressionManager.checkCraftingAchievements(player, material);
    }

    private void checkBuildingAchievements(Player player, Material material) {
        // Check building related achievements
        progressionManager.checkBuildingAchievements(player, material);
    }

    private void checkHuntingAchievements(Player player, EntityType entityType) {
        // Check hunting related achievements
        progressionManager.checkHuntingAchievements(player, entityType);
    }

    private void checkFarmingAchievements(Player player, Material crop) {
        // Check farming related achievements
        progressionManager.checkFarmingAchievements(player, crop);
    }

    /**
     * Checks if a player is eligible to advance to the next era
     * @param player The player to check
     * @return true if player can advance, false otherwise
     */
    public boolean canAdvanceEra(Player player) {
        UUID playerId = player.getUniqueId();
        EraType currentEra = plugin.getPlayerEra(playerId);

        // Get requirements for advancement
        List<Achievement> requirements = progressionManager.getEraAdvancementRequirements(currentEra);

        // Check if all requirements are met
        for (Achievement achievement : requirements) {
            if (!progressionManager.hasAchievement(player, achievement)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Advances a player to the next era if eligible
     * @param player The player to advance
     * @return true if successful, false otherwise
     */
    public boolean advancePlayerEra(Player player) {
        if (!canAdvanceEra(player)) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        EraType currentEra = plugin.getPlayerEra(playerId);
        EraType nextEra = getNextEra(currentEra);

        if (nextEra == null) {
            return false; // Already at the latest era
        }

        // Update player's era
        plugin.getDatabaseManager().updatePlayerEra(playerId, nextEra);

        // Apply new era effects
        Era newEra = plugin.getEra(nextEra);
        if (newEra != null) {
            newEra.applyToPlayer(player);
        }

        // Announce era advancement
        MessageUtils.broadcastFormattedMessage(
                "&b" + player.getName() + " &6has advanced to the &b" + nextEra.getDisplayName() + " &6era!");

        // Award era advancement achievement
        progressionManager.awardEraAdvancementAchievement(player, nextEra);

        return true;
    }

    /**
     * Gets the next era in progression
     * @param currentEra The current era
     * @return The next era, or null if already at the latest
     */
    private EraType getNextEra(EraType currentEra) {
        switch (currentEra) {
            case PREHISTORIC:
                return EraType.ANCIENT;
            case ANCIENT:
                return EraType.MEDIEVAL;
            case MEDIEVAL:
                return EraType.RENAISSANCE;
            case RENAISSANCE:
                return EraType.INDUSTRIAL;
            case INDUSTRIAL:
                return EraType.MODERN;
            case MODERN:
                return null; // Already at latest era
            default:
                return null;
        }
    }
}