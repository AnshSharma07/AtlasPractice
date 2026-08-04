/*
 * AtlasPractice - Open-source Minecraft Practice plugin.
 * Copyright (C) 2026 Ansh Sharma (Modular Boy Ansh)
 *
 * This file is part of AtlasPractice.
 *
 * AtlasPractice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AtlasPractice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AtlasPractice. If not, see <https://www.gnu.org/licenses/>.
 *
 * Project: https://github.com/AnshSharma07/AtlasPractice
 * Documentation: https://modularboyansh.xyz/atlas_docs
 */

package com.ansh.atlaspractice.commands;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.leaderboard.StatType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderboardCommand implements CommandExecutor, TabCompleter {

    private final AtlasPracticePlugin plugin;

    public LeaderboardCommand(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("atlaspractice.admin")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /atlasleaderboard create <name> <type> [kit]");
                    return true;
                }
                String name = args[1];
                StatType type;
                try {
                    type = StatType.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "Invalid type! Use: " + Arrays.toString(StatType.values()));
                    return true;
                }

                String kit = args.length > 3 ? args[3] : "NONE";
                if (!kit.equalsIgnoreCase("NONE") && plugin.getKitManager().getKit(kit) == null) {
                    player.sendMessage(ChatColor.RED + "Invalid kit.");
                    return true;
                }

                if (plugin.getLeaderboardManager().createLeaderboard(name, player.getLocation(), type, kit)) {
                    player.sendMessage(ChatColor.GREEN + "Leaderboard " + name + " created!");
                    plugin.getLeaderboardManager().updateAllHolograms();
                } else {
                    player.sendMessage(ChatColor.RED + "A leaderboard with that name already exists.");
                }
                break;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /atlasleaderboard delete <name>");
                    return true;
                }
                if (plugin.getLeaderboardManager().deleteLeaderboard(args[1])) {
                    player.sendMessage(ChatColor.GREEN + "Leaderboard deleted.");
                } else {
                    player.sendMessage(ChatColor.RED + "Leaderboard not found.");
                }
                break;
            case "list":
                player.sendMessage(ChatColor.AQUA + "Active Leaderboards:");
                plugin.getLeaderboardManager().getHolograms().forEach(h -> {
                    player.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + h.getName());
                });
                break;
            case "reload":
                plugin.getLeaderboardManager().loadHolograms();
                plugin.getLeaderboardManager().updateAllHolograms();
                player.sendMessage(ChatColor.GREEN + "Leaderboards reloaded.");
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.DARK_AQUA + "*** Leaderboard Commands ***");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard create <name> <type> [kit]");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard delete <name>");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard list");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "list", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return Arrays.stream(StatType.values())
                    .map(Enum::name)
                    .filter(s -> s.startsWith(args[2].toUpperCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}