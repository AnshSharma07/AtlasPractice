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

import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public final class PracticeCommandExecutor implements CommandExecutor {

    private final ProfileManager profileManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cStandard user utilities require live in-game player sessions.");
            return true;
        }

        Profile profile = this.profileManager.getProfile(player.getUniqueId());
        if (profile == null) return true;

        if (args.length > 0 && args[0].equalsIgnoreCase("lobby")) {
            if (profile.getState() == com.ansh.atlaspractice.profile.ProfileState.MATCH) {
                player.sendMessage("§cYou cannot flee from an un-concluded competitive match footprint.");
                return true;
            }
            player.teleport(player.getWorld().getSpawnLocation());
            player.sendMessage("§aTeleported to the central lobby area.");
            return true;
        }

        player.sendMessage("§7§m--------------------------------------------------");
        player.sendMessage("§6§lAtlasPractice Platform v1.0.0");
        player.sendMessage("§eType §6/duel <name> §eto challenge an active lobby participant.");
        player.sendMessage("§eType §6/party §eto look up multi-client grouping systems.");
        player.sendMessage("§eType §6/stats §eto view localized ranking calculations.");
        player.sendMessage("§7§m--------------------------------------------------");
        return true;
    }
}
