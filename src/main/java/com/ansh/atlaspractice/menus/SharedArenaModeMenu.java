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

package com.ansh.atlaspractice.menus;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.SharedArenaGroup;
import com.ansh.atlaspractice.arena.SharedArenaService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GUI that lets an admin assign or remove {@code arena} from each shared mode
 * independently.
 *
 * <p>Every paper in the inventory represents exactly one mode. Clicking it
 * toggles <em>only that mode's</em> pool — all other modes are unaffected.
 * The menu refreshes after every click without closing.</p>
 */
public final class SharedArenaModeMenu extends Menu {

    private final AtlasPracticePlugin plugin;
    private final Arena arena;

    public SharedArenaModeMenu(AtlasPracticePlugin plugin, Arena arena) {
        super(ChatColor.DARK_GREEN + "Shared Arena Modes", 54);
        this.plugin = plugin;
        this.arena = arena;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        SharedArenaService service = plugin.getSharedArenaService();

        int slot = 0;
        for (String mode : service.getEnabledModes()) {
            if (slot >= 54) {
                break;
            }

            SharedArenaGroup group = service.getGroup(mode);
            String displayName = group != null ? group.getDisplayName() : mode;

            // Capture the per-mode state at the time the button is created.
            final String capturedMode = mode;
            final boolean selected = service.isAssigned(mode, arena.getId());

            buttons.put(slot++, new Button(
                    buildItem(displayName, selected),
                    (clicker, clickType) -> {
                        if (selected) {
                            service.removeArena(capturedMode, arena.getId());
                            clicker.sendMessage(ChatColor.RED + "Removed arena "
                                    + ChatColor.YELLOW + arena.getId()
                                    + ChatColor.RED + " from "
                                    + ChatColor.YELLOW + displayName + ChatColor.RED + ".");
                        } else {
                            service.assignArena(capturedMode, arena.getId());
                            clicker.sendMessage(ChatColor.GREEN + "Added arena "
                                    + ChatColor.YELLOW + arena.getId()
                                    + ChatColor.GREEN + " to "
                                    + ChatColor.YELLOW + displayName + ChatColor.GREEN + ".");
                        }
                        // Refresh the menu without closing it.
                        new SharedArenaModeMenu(plugin, arena).openMenu(clicker);
                    }
            ));
        }

        return buttons;
    }

    private ItemStack buildItem(String displayName, boolean selected) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + displayName);

        List<String> lore = new ArrayList<>();
        if (selected) {
            // Enchantment glow to indicate selection.
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            lore.add(ChatColor.GRAY + "Status: " + ChatColor.GREEN + "Selected");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to remove");
        } else {
            lore.add(ChatColor.GRAY + "Status: " + ChatColor.RED + "Not Selected");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to add");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
