package com.nico.compressedblocks;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CompressedBlocksPlugin extends JavaPlugin {

    private CompressionManager compressionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        compressionManager = new CompressionManager(this);
        compressionManager.reload(getConfig());

        getServer().getPluginManager().registerEvents(new BlockTrackerListener(this, compressionManager), this);

        PluginCommand command = getCommand("compressedblocks");
        if (command != null) {
            command.setExecutor(new ReloadCommand(this, compressionManager));
        }
    }

    @Override
    public void onDisable() {
        if (compressionManager != null) {
            compressionManager.unregisterRecipes();
        }
    }
}
