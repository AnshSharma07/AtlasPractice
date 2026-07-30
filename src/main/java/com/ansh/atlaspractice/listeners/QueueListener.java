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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Material;
import com.ansh.atlaspractice.profile.ProfileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class QueueListener implements Listener {

    private final AtlasPracticePlugin plugin;

    public QueueListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // Only run on right clicks
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        Profile profile = plugin.getProfileManager()
                .getProfile(player.getUniqueId());

        if (profile == null) {
            return;
        }

        /*
         * Don't allow lobby items during matches.
         */
        if (profile.getState() == ProfileState.MATCH
                || profile.getState() == ProfileState.SPECTATE
                || profile.getState() == ProfileState.SPECTATING) {
            return;
        }

        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        Material type = item.getType();

        /*
         * Leave Queue
         */
        if (type == Material.REDSTONE) {

            if (profile.getState() != ProfileState.QUEUE
                    && profile.getState() != ProfileState.QUEUING) {
                return;
            }

            event.setCancelled(true);

            plugin.getQueueManager().leaveQueue(player);
            return;
        }

        /*
         * Open Unranked Menu
         */
        if (type == Material.IRON_SWORD) {

            if (profile.getState() != ProfileState.LOBBY
                    && profile.getState() != ProfileState.PARTY) {
                return;
            }

            event.setCancelled(true);

            plugin.getMenuManager()
                    .openUnrankedMenu(player, null);

            return;
        }

        /*
         * Ranked Queue
         */
        if (type == Material.DIAMOND_SWORD) {

            if (profile.getState() != ProfileState.LOBBY) {
                return;
            }

            event.setCancelled(true);

            new com.ansh.atlaspractice.menus.RankedMenu(plugin).openMenu(player);
        }
    }
}