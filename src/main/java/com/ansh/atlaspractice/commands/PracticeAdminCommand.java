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
 * Guide: https://modularboyansh.xyz/atlas_help
 */

package com.ansh.atlaspractice.commands;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.ArenaRepository;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public final class PracticeAdminCommand implements CommandExecutor {

    private final AtlasPracticePlugin plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("atlaspractice.admin")) {
            sender.sendMessage("§cNo authorization clearance mapped for administrative commands.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§7§m--------------------------------------------------");
            sender.sendMessage("§c§lAtlasPractice Administrative help:");
            sender.sendMessage("§e/practiceadmin reload §7- Reloads Config");
            sender.sendMessage("§e/practiceadmin status §7- Returns no. of active matches.");
            sender.sendMessage("§7§m--------------------------------------------------");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            this.plugin.getKitManager().loadKitsFromDisk();
            this.plugin.getArenaManager().resetAllArenas();
            this.plugin.getArenaManager().clear();
            new ArenaRepository(plugin).loadArenas(plugin.getArenaManager());
            sender.sendMessage("§aSuccessfully reloaded configurations");
            return true;
        }

        if (args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("§e[Engine Metrics] Live Active Matches: §a" + this.plugin.getMatchManager().getLiveMatches().size());
            sender.sendMessage("§e[Engine Metrics] Registered Cache Profiles: §a" + this.plugin.getProfileManager().getProfilesCount());
            return true;
        }

        sender.sendMessage("§cUnrecognized action argument.");
        return true;
    }
}
