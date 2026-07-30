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

import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class StatsCommand implements CommandExecutor {

    private final ProfileManager profileManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 && !(sender instanceof Player)) {
            sender.sendMessage("§cUsage: /stats <playerName>");
            return true;
        }

        Player target = (args.length > 0) ? Bukkit.getPlayer(args[0]) : (Player) sender;
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cTargeted player is currently unreachable or offline.");
            return true;
        }

        Profile profile = this.profileManager.getProfile(target.getUniqueId());
        if (profile == null) {
            sender.sendMessage("§cFailed to resolve stats for that player.");
            return true;
        }

        double kdr = profile.getDeaths() == 0 ? profile.getKills() : (double) profile.getKills() / profile.getDeaths();

        sender.sendMessage("§7§m--------------------------------------------------");
        sender.sendMessage("§6§lStats for: §e" + target.getName());
        sender.sendMessage("§7§m--------------------------------------------------");
        sender.sendMessage(" §7Â» §eTotal Wins: §a" + profile.getWins());
        sender.sendMessage(" §7Â» §eTotal Kills: §a" + profile.getKills());
        sender.sendMessage(" §7Â» §eTotal Deaths: §c" + profile.getDeaths());
        sender.sendMessage(" §7Â» §eKDR: §b" + String.format("%.2f", kdr));
        sender.sendMessage("§7§m--------------------------------------------------");
        return true;
    }
}
