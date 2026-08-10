package com.ansh.atlaspractice.commands;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.menus.RankedMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RankedCommand implements CommandExecutor {
    private final AtlasPracticePlugin plugin;

    public RankedCommand(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        new RankedMenu(plugin).openMenu(player);
        return true;
    }
}