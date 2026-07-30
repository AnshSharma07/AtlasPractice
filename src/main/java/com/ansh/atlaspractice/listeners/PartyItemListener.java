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
import com.ansh.atlaspractice.menus.PartyFFAKitMenu;
import com.ansh.atlaspractice.party.Party;
import com.ansh.atlaspractice.party.PartyListMenu;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles all hotbar-item interactions for the party system.
 *
 * <p>Party actions are mapped directly to hotbar items â€“ there is no intermediate
 * Party Menu GUI.  Every handler reuses the existing {@link com.ansh.atlaspractice.party.PartyManager}
 * methods without duplicating any logic.
 *
 * <p>Hotbar layout (slots defined in config.yml â†’ hotbar.party.*):
 * <pre>
 *   Slot 0  â€“ Party FFA       (hotbar.party.ffa)
 *   Slot 1  â€“ Party Split     (hotbar.party.split)
 *   Slot 2  â€“ Party vs Party  (hotbar.party.pvp)
 *   Slot 7  â€“ Leave Party     (hotbar.party.leave)
 *   Slot 8  â€“ Disband Party   (hotbar.party.disband, leader only)
 * </pre>
 *
 * <p>The lobby Nether Star (hotbar.lobby.party) still creates a new party when
 * clicked from the lobby, then switches the player to party hotbar items.
 */
public final class PartyItemListener implements Listener {

    // Display names must match config.yml (& translated to §).
    private static final String NAME_LOBBY_PARTY   = "§dParty Menu §7(Right Click)";
    private static final String NAME_FFA           = "§6Party FFA §7(Right Click)";
    private static final String NAME_SPLIT         = "§aParty Split §7(Right Click)";
    private static final String NAME_PVP           = "§bParty vs Party §7(Right Click)";
    private static final String NAME_LEAVE         = "§cLeave Party §7(Right Click)";
    private static final String NAME_DISBAND       = "§cDisband Party §7(Right Click)";
    private static final String NAME_KIT_CUSTOMIZER = "§bKit Customizer §7(Right Click)";

    private final AtlasPracticePlugin plugin;

    public PartyItemListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String displayName = item.getItemMeta().getDisplayName();
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return;

        // â”€â”€ Lobby: Nether Star â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // Creates a new party (if the player is not already in one).
        // Once created, applyPartyHotbarItems() replaces this item automatically,
        // so this branch only fires from the lobby.
        if (item.getType() == Material.NETHER_STAR && displayName.equals(NAME_LOBBY_PARTY)) {
            event.setCancelled(true);
            if (profile.getPartyId() == null) {
                plugin.getPartyManager().createParty(player);
            }
            return;
        }

        if (displayName.equals(NAME_FFA)) {
            event.setCancelled(true);

            new PartyFFAKitMenu(plugin).openMenu(player);
            return;
        }

        // â”€â”€ Party Split â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // Delegates entirely to PartyManager.openSplitMenu which validates
        // leader status, sets metadata, and opens the kit selector.
        if (displayName.equals(NAME_SPLIT)) {
            event.setCancelled(true);
            plugin.getPartyManager().openSplitMenu(player);
            return;
        }

        // â”€â”€ Party vs Party â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // Opens the PartyListMenu.  Clicking a head inside it executes
        // /party duel <leaderName>, handing off to the existing duel flow.
        if (displayName.equals(NAME_PVP)) {
            event.setCancelled(true);
            if (profile.getPartyId() == null) {
                player.sendMessage("§cYou are not in a party.");
                return;
            }
            new PartyListMenu(plugin).openMenu(player);
            return;
        }

        // â”€â”€ Leave Party â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (displayName.equals(NAME_LEAVE)) {
            event.setCancelled(true);
            if (profile.getPartyId() != null) {
                plugin.getPartyManager().leaveParty(player);
            } else {
                // Safety fallback â€“ profile out of sync.
                plugin.getInventoryUtil().applyLobbyHotbarItems(player);
                profile.setState(ProfileState.LOBBY);
            }
            return;
        }

        // â”€â”€ Disband Party (leader only) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (displayName.equals(NAME_DISBAND)) {
            event.setCancelled(true);
            plugin.getPartyManager().disbandParty(player);
            return;
        }

        // â”€â”€ Kit Customizer (Book â€“ preserved from original) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (item.getType() == Material.BOOK && displayName.equals(NAME_KIT_CUSTOMIZER)) {
            event.setCancelled(true);
            player.performCommand("kitlayout");
        }
    }
}