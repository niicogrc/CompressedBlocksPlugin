package com.nico.compressedblocks;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;
import java.util.Optional;

public class BlockTrackerListener implements Listener {

    private final CompressedBlocksPlugin plugin;
    private final CompressionManager manager;

    public BlockTrackerListener(CompressedBlocksPlugin plugin, CompressionManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack placed = event.getItemInHand();
        Optional<CompressedInfo> info = manager.getInfo(placed);
        if (info.isEmpty()) {
            return;
        }
        Block block = event.getBlockPlaced();
        NamespacedKey key = new NamespacedKey(plugin, tagKey(block));
        String value = info.get().material().name().toLowerCase(Locale.ROOT) + ":" + info.get().tier();
        block.getChunk().getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        NamespacedKey key = new NamespacedKey(plugin, tagKey(block));
        PersistentDataContainer chunkPdc = block.getChunk().getPersistentDataContainer();

        if (!chunkPdc.has(key, PersistentDataType.STRING)) {
            return;
        }
        String value = chunkPdc.get(key, PersistentDataType.STRING);
        chunkPdc.remove(key);

        if (!event.isDropItems()) {
            return;
        }

        String[] parts = value.split(":");
        Material material = Material.matchMaterial(parts[0]);
        int tier = Integer.parseInt(parts[1]);
        if (material == null) {
            return;
        }

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(
                block.getLocation().add(0.5, 0.5, 0.5),
                manager.createTierItem(material, tier));
    }

    private String tagKey(Block block) {
        return "b_" + block.getX() + "_" + block.getY() + "_" + block.getZ();
    }
}
