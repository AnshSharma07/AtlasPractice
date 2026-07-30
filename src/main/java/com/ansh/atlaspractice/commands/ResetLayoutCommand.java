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
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class ResetLayoutCommand implements CommandExecutor {

    private final AtlasPracticePlugin plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Guard check: Command must be issued by a player
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cFailed To Execute Command!.");
            return true;
        }

        // Validate arguments
        if (args.length < 1) {
            player.sendMessage("§cUsage: /resetlayout <kitName>");
            return true;
        }

        String kitId = args[0].toLowerCase();
        Kit kit = plugin.getKitManager().getKit(kitId).orElse(null);

        if (kit == null) {
            player.sendMessage("§cThat kit does not exist.");
            return true;
        }

        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return true;

        // Optional safety check: disable kit editing in game
        if (profile.getState() == ProfileState.MATCH) {
            player.sendMessage("§cYou cannot reset your layout while inside a live match.");
            return true;
        }

        // Execute the reset
        profile.resetCustomLayout(kitId);
        
        // Queue database persistence task asynchronously to protect the main thread
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Call your database profile save routine here to commit changes to atlas_custom_layouts
            // e.g., plugin.getDatabaseService().saveProfileLayouts(profile);
        });

        player.sendMessage("§aYour custom layout for §e" + kit.getDisplayName() + " §ahas been reset to default.");
        return true;
    }
}
