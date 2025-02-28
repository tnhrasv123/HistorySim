package me.ThePCUser.historySim.eras;

import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.technology.TechnologyManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Implements the Ancient Civilizations Era (3,000 BCE - 500 CE)
 */
public class AncientEra extends AbstractEra {

    public AncientEra(HistorySim plugin) {
        super(plugin);
    }

    @Override
    public EraType getType() {
        return EraType.ANCIENT;
    }

    @Override
    public void applyToPlayer(Player player) {
        // Give ancient era starter kit to new players
        if (!plugin.getDatabaseManager().hasPlayerVisitedEra(player.getUniqueId(), getType())) {
            player.getInventory().addItem(
                    new ItemStack(Material.IRON_AXE, 1),
                    new ItemStack(Material.IRON_PICKAXE, 1),
                    new ItemStack(Material.TRIDENT, 1),
                    new ItemStack(Material.BREAD, 8)
            );
        }
    }

    @Override
    public void savePlayerData(Player player) {
        plugin.getDatabaseManager().savePlayerEraData(player.getUniqueId(), getType());
    }

    @Override
    protected void registerCustomRecipes() {
        // Register bronze/iron working recipes
        craftingManager.addMetalWorkingRecipes();
        craftingManager.addIrrigationEquipmentRecipes();
    }

    @Override
    protected void registerTechnologies() {
        // Register ancient technologies
        techManager.registerTechnology(new Technology("bronze_working", "Bronze Working",
                "The ability to forge bronze tools and weapons", 300));
        techManager.registerTechnology(new Technology("iron_working", "Iron Working",
                "Advanced metalworking with iron", 500, "bronze_working"));
        techManager.registerTechnology(new Technology("writing", "Writing",
                "Record keeping and written language", 400));
        techManager.registerTechnology(new Technology("irrigation", "Irrigation",
                "Advanced farming with irrigation systems", 350));
    }

    @Override
    public void configureWorld(World world) {
        super.configureWorld(world);

        // Set specific Ancient world settings
        world.setGameRuleValue("naturalRegeneration", "true");

        // Ancient world has more structured terrain with early cities
    }
}
