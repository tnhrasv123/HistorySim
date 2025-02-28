package me.ThePCUser.historySim.eras;

import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.crafting.CraftingManager;
import me.ThePCUser.historySim.technology.Technology;
import me.ThePCUser.historySim.technology.TechnologyManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for all eras
 */
public abstract class AbstractEra implements Era {
    protected final HistorySim plugin;
    protected CraftingManager craftingManager;
    protected TechnologyManager techManager;

    public AbstractEra(HistorySim plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        craftingManager = new CraftingManager(plugin, getType());
        techManager = new TechnologyManager(plugin, getType());

        registerCustomRecipes();
        registerTechnologies();
    }

    /**
     * Register custom crafting recipes for this era
     */
    protected abstract void registerCustomRecipes();

    /**
     * Register technologies available in this era
     */
    protected abstract void registerTechnologies();

    @Override
    public void configureWorld(World world) {
        // Common world settings can go here
        world.setGameRuleValue("doDaylightCycle", "true");
        world.setGameRuleValue("doWeatherCycle", "true");
    }

    @Override
    public String getDisplayName() {
        return getType().getDisplayName();
    }
}