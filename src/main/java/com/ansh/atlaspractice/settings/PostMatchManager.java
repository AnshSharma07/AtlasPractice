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

package com.ansh.atlaspractice.settings;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;

public class PostMatchManager {

    private final AtlasPracticePlugin plugin;

    public PostMatchManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void executePostMatchTasks(Player player) {
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return;

        // Auto GG (Fires 1 second after returning to lobby)
        if (profile.isAutoGg()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.chat("gg");
                }
            }, 20L);
        }

        // Auto Requeue (Fires 1 second after returning to lobby)
        if (profile.isAutoRequeue() && profile.getLastQueuedKitId() != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                // Ensure player hasn't already entered a match, another queue, or an inventory menu
                if (plugin.getMatchManager().getPlayerMatchId(player.getUniqueId()) != null) return;
                
                // Safely execute the unranked queue command for the remembered kit
                plugin.getKitManager().getKit(profile.getLastQueuedKitId()).ifPresent(kit -> {
                    plugin.getQueueManager().joinUnrankedQueue(player, kit);
                });
                
            }, 20L);
        }
    }
}