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
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import lombok.RequiredArgsConstructor;
import com.ansh.atlaspractice.profile.ProfileState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public final class DuelCommand implements CommandExecutor {

    private final AtlasPracticePlugin plugin;
    private final ProfileManager profileManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can issue challenge executions.");
            return true;
        }

        Profile senderProfile = this.profileManager.getProfile(player.getUniqueId());
        if (senderProfile == null) return true;

        if (senderProfile.getState() != ProfileState.LOBBY) {
            player.sendMessage("§cYou must be standing inside the lobby to send a duel.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /duel <playerName>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cTargeted player is currently unreachable or offline.");
            return true;
        }


        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot duel yourself.");
            return true;
        }

        Profile targetProfile = this.profileManager.getProfile(target.getUniqueId());
        if (targetProfile == null || targetProfile.getState() != ProfileState.LOBBY) {
            player.sendMessage("§cThat player is currently busy or inside an active match.");
            return true;
        }
        if (!targetProfile.isAllowDuels()) {
            player.sendMessage("§cThat player is not accepting duel requests.");
            return true;
        }
        // Open unranked menu
        this.plugin.getMenuManager().openUnrankedMenu(player, target.getUniqueId());
        player.sendMessage("§eSelect a kit to request the duel.");
        return true;
    }
}
