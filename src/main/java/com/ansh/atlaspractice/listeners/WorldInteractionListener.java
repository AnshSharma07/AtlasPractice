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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import lombok.RequiredArgsConstructor;
import com.ansh.atlaspractice.profile.ProfileState;
import org.bukkit.Material;
import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;


@RequiredArgsConstructor
public final class WorldInteractionListener implements Listener {

    private final ProfileManager profileManager;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("atlaspractice.build.bypass") && player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }

        Profile profile = this.profileManager.getProfile(player.getUniqueId());
        if (profile == null || profile.getState() != ProfileState.MATCH) {
            event.setCancelled(true);
            return;
        }
    }
    @EventHandler
    public void onSpecQuit(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        Profile profile =
                profileManager.getProfile(player.getUniqueId());

        ItemStack item = event.getItem();

        if (item != null &&
                item.getType()
                        == Material.REDSTONE_COMPARATOR) {

            event.setCancelled(true);

            AtlasPracticePlugin.getInstance()
                    .getSettingsMenu()
                    .open(player);

            return;
        }

        if (profile == null ||
                profile.getState() != ProfileState.SPECTATE) {
            return;
        }

        item = event.getItem();

        if (item == null ||
                item.getType() != Material.BED) {
            return;
        }

        event.setCancelled(true);

        AtlasPracticePlugin.getInstance()
                .getMatchManager()
                .leaveSpectator(player);
    }
    @EventHandler
    public void onSettingsClick(
            InventoryClickEvent event
    ) {

        if (!(event.getWhoClicked()
                instanceof Player player)) {
            return;
        }

        if (!event.getView()
                .getTitle()
                .equals("Settings")) {
            return;
        }

        event.setCancelled(true);

        Profile profile =
                profileManager.getProfile(
                        player.getUniqueId()
                );

        if (profile == null) {
            return;
        }

        if (event.getSlot() == 3) {

            profile.setAllowDuels(
                    !profile.isAllowDuels()
            );

            AtlasPracticePlugin.getInstance()
                    .getDatabaseService()
                    .saveProfileData(profile);

        } else if (event.getSlot() == 5) {

            profile.setAllowPartyInvites(
                    !profile.isAllowPartyInvites()
            );

            AtlasPracticePlugin.getInstance()
                    .getDatabaseService()
                    .saveProfileData(profile);
        }

        AtlasPracticePlugin.getInstance()
                .getSettingsMenu()
                .open(player);
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("atlaspractice.build.bypass") && player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            return;
        }

        Profile profile = this.profileManager.getProfile(player.getUniqueId());
        if (profile == null || profile.getState() != ProfileState.MATCH) {
            event.setCancelled(true);
            return;
        }

        // Track and log block tracking coordinates inside individual matches to allow precise block reset task runs later
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Profile profile = this.profileManager.getProfile(player.getUniqueId());

        if (profile == null || profile.getState() != ProfileState.MATCH) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Profile profile = this.profileManager.getProfile(player.getUniqueId());

        if (profile == null || profile.getState() != ProfileState.MATCH) {
            event.setCancelled(true);
        }
    }
}
