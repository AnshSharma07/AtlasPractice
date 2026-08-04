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
import com.ansh.atlaspractice.kit.KitEditorSession;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class KitEditorListener implements Listener {

    private final AtlasPracticePlugin plugin;
    private final Map<UUID, KitEditorSession> sessions = new HashMap<>();

    // Tracks the last slot index where a player picked up an item from the editor setup
    private final Map<UUID, Integer> lastPickedSlot = new HashMap<>();

    public KitEditorListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void openEditor(Player player, String kitId, String displayName, ItemStack[] main, ItemStack[] armor) {
        openInventoryStructure(player, kitId, displayName, main, armor);
    }

    private void openInventoryStructure(Player player, String kitId, String displayName, ItemStack[] main, ItemStack[] armor) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Kit Editor: " + displayName);

        if (main != null) {
            for (int i = 0; i < Math.min(36, main.length); i++) inventory.setItem(i, main[i]);
        }
        if (armor != null && armor.length >= 4) {
            inventory.setItem(36, armor[0]);
            inventory.setItem(37, armor[1]);
            inventory.setItem(38, armor[2]);
            inventory.setItem(39, armor[3]);
        }

        for (int i = 40; i <= 51; i++) {
            inventory.setItem(i, new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7));
        }

        ItemStack save = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Layout");
        save.setItemMeta(saveMeta);
        inventory.setItem(52, save);

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancel.setItemMeta(cancelMeta);
        inventory.setItem(53, cancel);

        sessions.put(player.getUniqueId(), new KitEditorSession(player, kitId, inventory));
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        KitEditorSession session = sessions.get(uuid);

        if (session == null) return;
        if (!event.getView().getTopInventory().equals(session.getInventory())) return;

        int slot = event.getRawSlot();

        // Completely lock interactions with the player's actual bottom inventory
        if (slot >= event.getView().getTopInventory().getSize()) {
            event.setCancelled(true);
            return;
        }

        // Handle out of bounds / illegal click actions safely
        if (slot < 0 || slot >= 54 || event.isShiftClick() || event.getClick() == ClickType.DOUBLE_CLICK
                || event.getClick() == ClickType.NUMBER_KEY || event.getAction() == InventoryAction.HOTBAR_SWAP) {
            event.setCancelled(true);
            player.updateInventory();
            return;
        }

        // Armor slot safety
        if (slot >= 36 && slot <= 39) {
            event.setCancelled(true);
            player.updateInventory();
            player.sendMessage(ChatColor.RED + "You cannot modify your armor slots!");
            return;
        }

        // Track when items are picked up out of editable layout slots (0-35)
        if (slot >= 0 && slot <= 35) {
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            // Action: Picking up an item or swapping an item onto an empty cursor
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                if (cursorItem == null || cursorItem.getType() == Material.AIR) {
                    lastPickedSlot.put(uuid, slot);
                }
            }
        }

        // SAVE BUTTON
        if (slot == 52) {
            event.setCancelled(true);

            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                int targetSlot = lastPickedSlot.getOrDefault(uuid, -1);

                // Fallback to first open workspace slot if the last tracked slot is full or occupied
                if (targetSlot == -1 || session.getInventory().getItem(targetSlot) != null) {
                    targetSlot = -1;
                    for (int i = 0; i < 36; i++) {
                        if (session.getInventory().getItem(i) == null || session.getInventory().getItem(i).getType() == Material.AIR) {
                            targetSlot = i;
                            break;
                        }
                    }
                }

                if (targetSlot != -1) {
                    session.getInventory().setItem(targetSlot, cursorItem.clone());
                }
                Bukkit.getScheduler().runTask(plugin, () -> player.setItemOnCursor(new ItemStack(Material.AIR)));
            }

            Inventory inventory = session.getInventory();
            ItemStack[] contents = new ItemStack[36];
            ItemStack[] armor = new ItemStack[4];

            for (int i = 0; i < 36; i++) contents[i] = inventory.getItem(i);
            for (int i = 0; i < 4; i++) armor[i] = inventory.getItem(36 + i);

            Profile profile = plugin.getProfileManager().getProfile(uuid);
            if (profile != null) {
                profile.setCustomLayout(session.getKitId(), contents);
                profile.setCustomArmor(session.getKitId(), armor);

                plugin.getDatabaseService().saveProfileData(profile);
                player.sendMessage(ChatColor.GREEN + "Personal layout and armor saved.");
            }

            sessions.remove(uuid);
            lastPickedSlot.remove(uuid);
            Bukkit.getScheduler().runTask(plugin, player::closeInventory);
            return;
        }

        // CANCEL BUTTON
        if (slot == 53) {
            event.setCancelled(true);

            sessions.remove(uuid);
            lastPickedSlot.remove(uuid);
            player.sendMessage(ChatColor.RED + "Changes discarded.");

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.setItemOnCursor(new ItemStack(Material.AIR));
                player.closeInventory();
            });
            return;
        }

        // Decorative glass pane protection
        if (slot > 35) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (sessions.containsKey(event.getWhoClicked().getUniqueId())) {
            for (int slot : event.getRawSlots()) {
                if (slot >= 36 || slot < 0) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        KitEditorSession session = sessions.remove(uuid);
        Integer returnSlot = lastPickedSlot.remove(uuid);

        if (session != null) {
            ItemStack cursorItem = event.getPlayer().getItemOnCursor();

            // If the window closes (ESC pressed) while an item is on the cursor, safely restore it
            if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                int targetSlot = (returnSlot != null) ? returnSlot : -1;

                // Safe fallback check if original slot was somehow filled
                if (targetSlot == -1 || (session.getInventory().getItem(targetSlot) != null &&
                        session.getInventory().getItem(targetSlot).getType() != Material.AIR)) {
                    targetSlot = -1;
                    for (int i = 0; i < 36; i++) {
                        if (session.getInventory().getItem(i) == null || session.getInventory().getItem(i).getType() == Material.AIR) {
                            targetSlot = i;
                            break;
                        }
                    }
                }

                // Put item back into inventory grid before it can drop down into player's main inventory
                if (targetSlot != -1) {
                    session.getInventory().setItem(targetSlot, cursorItem.clone());
                }
            }

            // Wipe out remaining ghost desync cursor tracking instances
            Bukkit.getScheduler().runTask(plugin, () -> event.getPlayer().setItemOnCursor(new ItemStack(Material.AIR)));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (sessions.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
