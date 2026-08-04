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

package com.ansh.atlaspractice.arena.setup;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class ArenaAutoSetupCommand implements CommandExecutor {

    private final AtlasPracticePlugin plugin;
    public ArenaAutoSetupCommand(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }
        if (!player.hasPermission("atlas.admin")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;}
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /arenaautosetup <arena> <yes|no>");
            return true;
        }

        String arenaName = args[0];
        String action = args[1];

        Optional<Arena> optionalArena = plugin.getArenaManager().getArena(arenaName);

        if (optionalArena.isEmpty()) {
            player.sendMessage(ChatColor.RED + "Arena not found.");
            return true;
        }

        Arena arena = optionalArena.get();

        if (action.equalsIgnoreCase("yes")) {
            player.sendMessage(ChatColor.GREEN + "Starting auto setup wizard in 1 second...");
            new BukkitRunnable() {
                @Override
                public void run() {
                    new ArenaAutoSetupSession(
                            player,
                            arena,
                            plugin,
                            plugin.getArenaManager()
                    ).start();
                }
            }.runTaskLater(plugin, 20L);

        } else if (action.equalsIgnoreCase("no")) {

            player.sendMessage(ChatColor.YELLOW + "Auto setup cancelled for " + arena.getId() + ".");

        } else {

            player.sendMessage(ChatColor.RED + "Usage: /arenaautosetup <arena> <yes|no>");
        }

        return true;
    }
}