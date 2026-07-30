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

package com.ansh.atlaspractice.cosmetics.menu;

import com.ansh.atlaspractice.cosmetics.CosmeticType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsMenu {

    private final Player player;

    public CosmeticsMenu(Player player) {
        this.player = player;
    }

    public void open() {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Cosmetics");

        int[] slots = {10, 11, 12, 13, 14, 15, 16};
        CosmeticType[] types = CosmeticType.values();

        for (int i = 0; i < types.length && i < slots.length; i++) {
            CosmeticType type = types[i];
            ItemStack item = new ItemStack(getIconForType(type));
            ItemMeta meta = item.getItemMeta();

            String name = getDisplayNameForType(type);
            meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + name);

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.GRAY + "Click to browse all available");
            lore.add(ChatColor.GRAY + name + " cosmetics.");
            lore.add("");
            lore.add(ChatColor.YELLOW + "â–º Click to view");

            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(slots[i], item);
        }

        fillBorders(inventory);
        player.openInventory(inventory);
    }

    private Material getIconForType(CosmeticType type) {
        switch (type) {
            case KILL_EFFECT: return Material.REDSTONE;
            case VICTORY_EFFECT: return Material.FIREWORK;
            case PROJECTILE_TRAIL: return Material.ARROW;
            case WALKING_TRAIL: return Material.DIAMOND_BOOTS;
            case AURA: return Material.BEACON;
            case CHAT_COLOR: return Material.NAME_TAG;
            case KILL_MESSAGE: return Material.PAPER;
            default: return Material.CHEST;
        }
    }

    private String getDisplayNameForType(CosmeticType type) {
        switch (type) {
            case KILL_EFFECT: return "Kill Effects";
            case VICTORY_EFFECT: return "Victory Effects";
            case PROJECTILE_TRAIL: return "Projectile Trails";
            case WALKING_TRAIL: return "Walking Trails";
            case AURA: return "Auras";
            case CHAT_COLOR: return "Chat Colors";
            case KILL_MESSAGE: return "Kill Messages";
            default: return "Cosmetics";
        }
    }

    private void fillBorders(Inventory inventory) {
        ItemStack darkGlass = createGlass((short) 15, " ");
        ItemStack cyanGlass = createGlass((short) 9, " ");

        // Corner accents
        int[] cyanSlots = {0, 8, 18, 26};
        for (int slot : cyanSlots) {
            inventory.setItem(slot, cyanGlass);
        }

        // Fill remaining empty slots
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, darkGlass);
            }
        }
    }

    private ItemStack createGlass(short data, String name) {
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, data);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(name);
        glass.setItemMeta(meta);
        return glass;
    }
}