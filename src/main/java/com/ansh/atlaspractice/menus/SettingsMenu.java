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

package com.ansh.atlaspractice.menus;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.settings.ChatMode;
import com.ansh.atlaspractice.settings.TimeMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class SettingsMenu implements Listener {

    private final AtlasPracticePlugin plugin;
    private static final String MENU_TITLE = ChatColor.DARK_GRAY + "Player Settings";

    public SettingsMenu(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        // Frame and Background fill
        ItemStack borderGlass = createGuiItem(Material.STAINED_GLASS_PANE, (short) 15, " ", null); // Dark Gray
        ItemStack fillerGlass = createGuiItem(Material.STAINED_GLASS_PANE, (short) 7, " ", null);  // Light Gray

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, borderGlass);
            } else {
                inv.setItem(i, fillerGlass);
            }
        }

        // Row 2: Interaction Settings
        inv.setItem(20, buildSettingItem("Duel Requests", Material.DIAMOND_SWORD, profile.isAllowDuels()));
        inv.setItem(22, buildSettingItem("Party Invites", Material.NAME_TAG, profile.isAllowPartyInvites()));
        inv.setItem(24, buildSettingItem("Scoreboard Visibility", Material.SIGN, profile.isScoreboardEnabled()));

        // Row 3: Environment Settings
        boolean timeActive = profile.getTimeMode() != TimeMode.SERVER;
        inv.setItem(29, buildCycleItem("Time Changer", Material.WATCH, profile.getTimeMode().getName(), timeActive));

        boolean chatActive = profile.getChatMode() != ChatMode.NONE;
        inv.setItem(33, buildCycleItem("Chat Visibility", Material.PAPER, profile.getChatMode().getName(), chatActive));

        // Row 4: Quality of Life Settings
        inv.setItem(38, buildSettingItem("Auto GG", Material.GOLD_INGOT, profile.isAutoGg()));
        inv.setItem(42, buildSettingItem("Auto Requeue", Material.EYE_OF_ENDER, profile.isAutoRequeue()));

        player.openInventory(inv);
    }

    private ItemStack buildSettingItem(String name, Material material, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((enabled ? ChatColor.GREEN : ChatColor.RED) + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Status: " + (enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to toggle.");
            meta.setLore(lore);

            if (enabled) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildCycleItem(String name, Material material, String currentVal, boolean activeColor) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Current: " + ChatColor.AQUA + currentVal);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to cycle options.");
            meta.setLore(lore);

            if (activeColor) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, short data, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return;

        int slot = event.getRawSlot();
        boolean updated = false;

        switch (slot) {
            case 20:
                profile.setAllowDuels(!profile.isAllowDuels());
                updated = true;
                break;
            case 22:
                profile.setAllowPartyInvites(!profile.isAllowPartyInvites());
                updated = true;
                break;
            case 24:
                profile.setScoreboardEnabled(!profile.isScoreboardEnabled());
                if (!profile.isScoreboardEnabled()) {
                    plugin.getScoreboardUpdateTask().unregisterPlayer(player);
                } else {if (plugin.getScoreboardUpdateTask() != null) {
                        plugin.getScoreboardUpdateTask().registerPlayer(player);
                    }
                }
                updated = true;
                break;
            case 29:
                profile.setTimeMode(profile.getTimeMode().next());
                if (profile.getTimeMode() == TimeMode.SERVER) {
                    player.resetPlayerTime();
                } else {
                    player.setPlayerTime(profile.getTimeMode().getTime(), false);
                }
                updated = true;
                break;
            case 33:
                profile.setChatMode(profile.getChatMode().next());
                updated = true;
                break;
            case 38:
                profile.setAutoGg(!profile.isAutoGg());
                updated = true;
                break;
            case 42:
                profile.setAutoRequeue(!profile.isAutoRequeue());
                updated = true;
                break;
        }

        if (updated) {
            player.playSound(player.getLocation(), Sound.CLICK, 1.0F, 1.0F);
            plugin.getDatabaseService().saveProfileDataAsync(profile);
            open(player);
        }
    }
}
