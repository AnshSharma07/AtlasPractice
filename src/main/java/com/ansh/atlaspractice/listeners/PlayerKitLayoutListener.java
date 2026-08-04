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
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.kit.PlayerLayoutSession;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerKitLayoutListener implements Listener {

    private static final String MENU_TITLE = ChatColor.DARK_GRAY + "Kit Layout Editor";
    private static final int INVENTORY_SIZE = 54;
    private static final int KITS_PER_PAGE = 28;

    private static final int SLOT_PREVIOUS_PAGE = 0;
    private static final int SLOT_NEXT_PAGE = 8;
    private static final int SLOT_BACK = 45;
    private static final int SLOT_PAGE_INDICATOR = 49;

    private static final int[] CENTER_GRID_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final AtlasPracticePlugin plugin;
    private final Map<UUID, PlayerLayoutSession> layoutEditors = new ConcurrentHashMap<>();

    public PlayerKitLayoutListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void openSelectionMenu(Player player) {
        openPage(player, 1);
    }

    public void openPage(Player player, int page) {

        List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());

        int totalKits = kits.size();
        int maxPages = Math.max(1,
                (int) Math.ceil((double) totalKits / KITS_PER_PAGE));

        int currentPage = Math.min(Math.max(1, page), maxPages);

        Inventory inventory =
                Bukkit.createInventory(null, INVENTORY_SIZE, MENU_TITLE);

        applyGlassBorder(inventory);
        setupNavigationAndFooter(inventory, currentPage, maxPages);

        int startIndex = (currentPage - 1) * KITS_PER_PAGE;
        int endIndex = Math.min(startIndex + KITS_PER_PAGE, totalKits);

        int slotIndex = 0;

        for (int i = startIndex; i < endIndex; i++) {

            Kit kit = kits.get(i);

            inventory.setItem(
                    CENTER_GRID_SLOTS[slotIndex++],
                    createKitItem(kit)
            );
        }

        player.openInventory(inventory);
    }

    private void applyGlassBorder(Inventory inventory) {

        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);

        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }

        for (int slot : CENTER_GRID_SLOTS) {
            inventory.setItem(slot, null);
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, glass);
            }
        }
    }
    private void setupNavigationAndFooter(Inventory inventory, int page, int maxPages) {

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back");
            backMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Close this menu."));
            back.setItemMeta(backMeta);
        }
        inventory.setItem(SLOT_BACK, back);

        ItemStack pageItem = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageItem.getItemMeta();
        if (pageMeta != null) {
            pageMeta.setDisplayName(ChatColor.YELLOW + "Page " + page + "/" + maxPages);
            pageMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Viewing available kits."));
            pageItem.setItemMeta(pageMeta);
        }
        inventory.setItem(SLOT_PAGE_INDICATOR, pageItem);

        if (page > 1) {
            ItemStack previous = new ItemStack(Material.ARROW);
            ItemMeta previousMeta = previous.getItemMeta();
            if (previousMeta != null) {
                previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
                previous.setItemMeta(previousMeta);
            }
            inventory.setItem(SLOT_PREVIOUS_PAGE, previous);
        }

        if (page < maxPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
                next.setItemMeta(nextMeta);
            }
            inventory.setItem(SLOT_NEXT_PAGE, next);
        }
    }

    private ItemStack createKitItem(Kit kit) {

        Material material = kit.getDisplayMaterial() != null
                ? kit.getDisplayMaterial()
                : Material.DIAMOND_SWORD;

        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + kit.getDisplayName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to edit your layout.");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to open.");

            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(MENU_TITLE)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName())
            return;

        int slot = event.getRawSlot();

        if (slot == SLOT_BACK) {
            player.closeInventory();
            return;
        }

        int currentPage = 1;

        ItemStack indicator = event.getInventory().getItem(SLOT_PAGE_INDICATOR);

        if (indicator != null && indicator.hasItemMeta()) {
            try {
                String page = ChatColor.stripColor(indicator.getItemMeta().getDisplayName())
                        .replace("Page ", "")
                        .split("/")[0];

                currentPage = Integer.parseInt(page);

            } catch (Exception ignored) {
            }
        }

        if (slot == SLOT_PREVIOUS_PAGE && clicked.getType() == Material.ARROW) {
            openPage(player, currentPage - 1);
            return;
        }

        if (slot == SLOT_NEXT_PAGE && clicked.getType() == Material.ARROW) {
            openPage(player, currentPage + 1);
            return;
        }

        String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        Kit kit = plugin.getKitManager()
                .getAllKits()
                .stream()
                .filter(k -> k.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElse(null);

        if (kit == null)
            return;

        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());

        ItemStack[] contents;

        if (profile != null && profile.getCustomLayout(kit.getId()) != null) {
            contents = profile.getCustomLayout(kit.getId()).clone();
        } else {
            contents = kit.getMainContents() == null
                    ? new ItemStack[36]
                    : kit.getMainContents().clone();
        }

        ItemStack[] armor;

        if (profile != null && profile.getCustomArmor(kit.getId()) != null) {
            armor = profile.getCustomArmor(kit.getId()).clone();
        } else {
            armor = kit.getArmorContents() == null
                    ? new ItemStack[4]
                    : kit.getArmorContents().clone();
        }

        layoutEditors.put(
                player.getUniqueId(),
                new PlayerLayoutSession(
                        kit.getId(),
                        contents,
                        armor
                )
        );

        player.closeInventory();

        Bukkit.getScheduler().runTask(plugin, () ->
                plugin.getKitEditorListener().openEditor(
                        player,
                        kit.getId(),
                        kit.getDisplayName(),
                        contents,
                        armor
                ));
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player player))
            return;

        if (!layoutEditors.containsKey(player.getUniqueId()))
            return;

        ItemStack cursor = player.getItemOnCursor();

        if (cursor != null && cursor.getType() != Material.AIR) {
            player.setItemOnCursor(new ItemStack(Material.AIR));
        }

        layoutEditors.remove(player.getUniqueId());
    }
}