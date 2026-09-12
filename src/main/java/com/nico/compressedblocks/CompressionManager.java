package com.nico.compressedblocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CompressionManager {

    private static final String[] TIER_PREFIXES = {
            "Compressed", "Double Compressed", "Triple Compressed",
            "Quadruple Compressed", "Quintuple Compressed", "Sextuple Compressed",
            "Septuple Compressed", "Octuple Compressed"
    };

    private final CompressedBlocksPlugin plugin;
    private final NamespacedKey materialKey;
    private final NamespacedKey tierKey;
    private final List<Material> materials = new ArrayList<>();
    private final List<NamespacedKey> registeredRecipeKeys = new ArrayList<>();
    private int maxTier = 4;

    public CompressionManager(CompressedBlocksPlugin plugin) {
        this.plugin = plugin;
        this.materialKey = new NamespacedKey(plugin, "source_material");
        this.tierKey = new NamespacedKey(plugin, "tier");
    }

    public void reload(FileConfiguration config) {
        unregisterRecipes();

        materials.clear();
        for (String name : config.getStringList("compressible-materials")) {
            try {
                materials.add(Material.valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Material desconocido en config.yml: " + name);
            }
        }
        maxTier = Math.max(1, config.getInt("max-tier", 4));

        registerRecipes();
    }

    private void registerRecipes() {
        for (Material base : materials) {
            for (int tier = 1; tier <= maxTier; tier++) {
                registerCompressRecipe(base, tier);
                registerDecompressRecipe(base, tier);
            }
        }
    }

    private void registerCompressRecipe(Material base, int tier) {
        NamespacedKey key = new NamespacedKey(plugin,
                "compress_" + base.name().toLowerCase(Locale.ROOT) + "_" + tier);
        ItemStack output = createTierItem(base, tier);
        ShapelessRecipe recipe = new ShapelessRecipe(key, output);

        RecipeChoice ingredient = tier == 1
                ? new RecipeChoice.MaterialChoice(base)
                : new RecipeChoice.ExactChoice(createTierItem(base, tier - 1));
        for (int i = 0; i < 9; i++) {
            recipe.addIngredient(ingredient);
        }

        Bukkit.addRecipe(recipe);
        registeredRecipeKeys.add(key);
    }

    private void registerDecompressRecipe(Material base, int tier) {
        NamespacedKey key = new NamespacedKey(plugin,
                "decompress_" + base.name().toLowerCase(Locale.ROOT) + "_" + tier);

        ItemStack output = tier == 1 ? new ItemStack(base, 9) : createTierItem(base, tier - 1);
        output.setAmount(9);

        ShapelessRecipe recipe = new ShapelessRecipe(key, output);
        recipe.addIngredient(new RecipeChoice.ExactChoice(createTierItem(base, tier)));

        Bukkit.addRecipe(recipe);
        registeredRecipeKeys.add(key);
    }

    public void unregisterRecipes() {
        for (NamespacedKey key : registeredRecipeKeys) {
            Bukkit.removeRecipe(key);
        }
        registeredRecipeKeys.clear();
    }

    public ItemStack createTierItem(Material base, int tier) {
        ItemStack item = new ItemStack(base);
        ItemMeta meta = item.getItemMeta();

        String baseName = prettify(base);
        meta.setDisplayName(ChatColor.WHITE + tierPrefix(tier) + " " + baseName);

        long equivalent = (long) Math.pow(9, tier);
        meta.setLore(List.of(ChatColor.GRAY + "Equivale a " + ChatColor.WHITE
                + equivalent + " " + ChatColor.GRAY + baseName));

        meta.getPersistentDataContainer().set(materialKey, PersistentDataType.STRING, base.name());
        meta.getPersistentDataContainer().set(tierKey, PersistentDataType.INTEGER, tier);
        meta.setCustomModelData(tier);

        item.setItemMeta(meta);
        return item;
    }

    public Optional<CompressedInfo> getInfo(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return Optional.empty();
        }
        ItemMeta meta = stack.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        if (!pdc.has(materialKey, PersistentDataType.STRING) || !pdc.has(tierKey, PersistentDataType.INTEGER)) {
            return Optional.empty();
        }
        String materialName = pdc.get(materialKey, PersistentDataType.STRING);
        Integer tier = pdc.get(tierKey, PersistentDataType.INTEGER);
        try {
            return Optional.of(new CompressedInfo(Material.valueOf(materialName), tier));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String tierPrefix(int tier) {
        int index = tier - 1;
        if (index >= 0 && index < TIER_PREFIXES.length) {
            return TIER_PREFIXES[index];
        }
        return "Nivel " + tier + " Comprimido";
    }

    private String prettify(Material material) {
        String[] parts = material.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(part.charAt(0)).append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
