package com.nico.compressedblocks;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final CompressedBlocksPlugin plugin;
    private final CompressionManager manager;

    public ReloadCommand(CompressedBlocksPlugin plugin, CompressionManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            manager.reload(plugin.getConfig());
            sender.sendMessage(ChatColor.GREEN + "[CompressedBlocks] Configuracion recargada.");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "Uso: /" + label + " reload");
        return true;
    }
}
