package me.ThePCUser.historySim.crafting;

import me.ThePCUser.historySim.HistorySim;
import me.ThePCUser.historySim.eras.EraType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages custom crafting recipes for each era
 */
public class CraftingManager {
    private final HistorySim plugin;
    private final EraType eraType;
    private final List<String> registeredRecipes = new ArrayList<>();

    public CraftingManager(HistorySim plugin, EraType eraType) {
        this.plugin = plugin;
        this.eraType = eraType;
    }

    /**
     * Add prehistoric stone tool recipes
     */
    public void addStoneToolRecipes() {
        // In a full implementation, this would add custom variants
        // of stone tools with special properties

        // For example, a primitive stone axe
        ItemStack primitiveAxe = new ItemStack(Material.STONE_AXE);
        ItemMeta meta = primitiveAxe.getItemMeta();
        meta.setDisplayName("§aPrimitive Stone Axe");
        List<String> lore = new ArrayList<>();
        lore.add("§7A crude stone tool from the prehistoric era");
        meta.setLore(lore);
        primitiveAxe.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(primitiveAxe);
        recipe.shape(" S ", " S ", " W ");
        recipe.setIngredient('S', Material.COBBLESTONE);
        recipe.setIngredient('W', Material.STICK);

        registerRecipe("primitive_axe", recipe);
    }

    /**
     * Add primitive weapon recipes for prehistoric era
     */
    public void addPrimitiveWeaponRecipes() {
        // In a full implementation, this would add primitive weapons
        // like spears, clubs, etc.

        // For example, a simple spear
        ItemStack spear = new ItemStack(Material.WOODEN_SWORD);
        ItemMeta meta = spear.getItemMeta();
        meta.setDisplayName("§aPrimitive Spear");
        List<String> lore = new ArrayList<>();
        lore.add("§7A simple hunting tool");
        meta.setLore(lore);
        spear.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(spear);
        recipe.shape("  F", " S ", "S  ");
        recipe.setIngredient('F', Material.FLINT);
        recipe.setIngredient('S', Material.STICK);

        registerRecipe("primitive_spear", recipe);
    }

    /**
     * Add metal working recipes for ancient era
     */
    public void addMetalWorkingRecipes() {
        // In a full implementation, this would add bronze and iron
        // working recipes for the ancient era

        // For example, a bronze helmet
        ItemStack bronzeHelmet = new ItemStack(Material.CHAINMAIL_HELMET);
        ItemMeta meta = bronzeHelmet.getItemMeta();
        meta.setDisplayName("§aBronze Helmet");
        List<String> lore = new ArrayList<>();
        lore.add("§7A helmet made of bronze");
        meta.setLore(lore);
        bronzeHelmet.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(bronzeHelmet);
        recipe.shape("III", "I I", "   ");
        recipe.setIngredient('I', Material.GOLD_INGOT); // Representing bronze

        registerRecipe("bronze_helmet", recipe);
    }

    /**
     * Add irrigation equipment recipes
     */
    public void addIrrigationEquipmentRecipes() {
        // In a full implementation, this would add irrigation
        // tools for the ancient era

        // For example, a water bucket with enhanced properties
        ItemStack irrigationBucket = new ItemStack(Material.WATER_BUCKET);
        ItemMeta meta = irrigationBucket.getItemMeta();
        meta.setDisplayName("§aIrrigation Bucket");
        List<String> lore = new ArrayList<>();
        lore.add("§7Used for advanced farming techniques");
        meta.setLore(lore);
        irrigationBucket.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(irrigationBucket);
        recipe.shape(" I ", "IWI", " I ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('W', Material.WATER_BUCKET);

        registerRecipe("irrigation_bucket", recipe);
    }

    /**
     * Register a recipe with Bukkit
     */
    private void registerRecipe(String id, ShapedRecipe recipe) {
        // In a full implementation, this would add the recipe to Bukkit
        // and store reference for later use

        id = eraType.name().toLowerCase() + "_" + id;

        // Check if recipe is already registered
        if (registeredRecipes.contains(id)) {
            return;
        }

        try {
            plugin.getServer().addRecipe(recipe);
            registeredRecipes.add(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a list of all registered recipe IDs
     */
    public List<String> getRegisteredRecipes() {
        return new ArrayList<>(registeredRecipes);
    }
}
