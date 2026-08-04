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

package com.ansh.atlaspractice.party;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.menus.Button;
import com.ansh.atlaspractice.menus.Menu;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays all available parties that can be challenged via /party duel.
 * <p>
 * Each entry is the leader's player-head. Clicking runs
 * {@code /party duel <leaderName>} exactly as if the player typed it,
 * handing off to the existing kit-selection and duel flow without
 * duplicating any matchmaking logic.
 */
public final class PartyListMenu extends Menu {

    private final AtlasPracticePlugin plugin;

    public PartyListMenu(AtlasPracticePlugin plugin) {
        super("§8Challenge a Party", 6 * 9);
        this.plugin = plugin;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return buttons;

        int slot = 0;

        for (Party party : plugin.getPartyManager().getActiveParties()) {
            if (slot >= getSize()) break;

            // Skip the viewer's own party.
            if (party.contains(player.getUniqueId())) continue;

            // Only show parties whose leader is currently online.
            Player leader = party.getLeader();
            if (leader == null || !leader.isOnline()) continue;

            // Skip empty or single-member parties that have no real opponents.
            if (party.size() == 0) continue;

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwner(leader.getName());
                meta.setDisplayName("§e§l" + leader.getName());
                meta.setLore(Arrays.asList(
                        "§7Members: §f" + party.size(),
                        "",
                        "§aClick to challenge"
                ));
                skull.setItemMeta(meta);
            }

            final String leaderName = leader.getName();

            buttons.put(slot++, new Button(skull, (p, click) -> {
                p.closeInventory();
                // Delegate entirely to the existing /party duel command,
                // which opens kit selection and handles all matchmaking.
                p.performCommand("party duel " + leaderName);
            }));
        }

        // If no challengeable parties were found, show a glass-pane placeholder.
        if (buttons.isEmpty()) {
            ItemStack empty = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
            org.bukkit.inventory.meta.ItemMeta emptyMeta = empty.getItemMeta();
            if (emptyMeta != null) {
                emptyMeta.setDisplayName("§cNo parties available");
                emptyMeta.setLore(Arrays.asList("§7No other parties are currently in the lobby."));
                empty.setItemMeta(emptyMeta);
            }
            buttons.put(22, new Button(empty, (p, click) -> { /* no-op */ }));
        }

        return buttons;
    }
}