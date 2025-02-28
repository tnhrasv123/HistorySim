package me.ThePCUser.historySim.eras;

import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.technology.TechnologyManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

/**
 * Implements the Prehistoric Era (10,000 BCE - 3,000 BCE)
 */
public class PrehistoricEra extends AbstractEra {

    // Materials available in prehistoric era
    private final List<Material> availableMaterials = Arrays.asList(
            Material.STONE, Material.COBBLESTONE, Material.DIRT, Material.GRASS_BLOCK,
            Material.OAK_WOOD, Material.OAK_LOG, Material.SAND, Material.GRAVEL,
            Material.LEATHER
    );

    public PrehistoricEra(HistorySim plugin) {
        super(plugin);
    }

    @Override
    public EraType getType() {
        return EraType.PREHISTORIC;
    }

    @Override
    public void applyToPlayer(Player player) {
        // Give primitive tools starter kit to new players
        if (!plugin.getDatabaseManager().hasPlayerVisitedEra(player.getUniqueId(), getType())) {
            player.getInventory().addItem(
                    new ItemStack(Material.STONE_AXE, 1),
                    new ItemStack(Material.STONE_PICKAXE, 1),
                    new ItemStack(Material.STONE_SPADE, 1),
                    new ItemStack(Material.TORCH, 4)
            );
        }

        // Apply era-specific effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 0, false, false));
    }

    @Override
    public void savePlayerData(Player player) {
        // Save prehistoric era specific player data
        plugin.getDatabaseManager().savePlayerEraData(player.getUniqueId(), getType());
    }

    @Override
    protected void registerCustomRecipes() {
        // Register primitive tool recipes
        craftingManager.addStoneToolRecipes();
        craftingManager.addPrimitiveWeaponRecipes();
    }

    @Override
    protected void registerTechnologies() {
        // Register prehistoric technologies
        techManager.registerTechnology(new Technology("fire_making", "Fire Making",
                "The ability to create and control fire", 100));
        techManager.registerTechnology(new Technology("basic_shelter", "Basic Shelter",
                "Knowledge of building simple shelters", 150));
        techManager.registerTechnology(new Technology("hunting", "Hunting",
                "Advanced hunting techniques", 200));
        techManager.registerTechnology(new Technology("agriculture", "Basic Agriculture",
                "Simple farming techniques", 300, "hunting"));
    }

    @Override
    public void configureWorld(World world) {
        super.configureWorld(world);

        // Set specific Prehistoric world settings
        world.setGameRuleValue("doMobSpawning", "true");
        world.setGameRuleValue("naturalRegeneration", "false"); // Harder survival

        // Configure world spawn with caves and basic resources
        // (This would use a custom world generator or terrain modification)
    }
}