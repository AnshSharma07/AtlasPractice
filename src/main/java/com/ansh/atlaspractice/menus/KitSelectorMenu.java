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
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class KitSelectorMenu implements Listener {

    private static final String MENU_TITLE = ChatColor.DARK_GRAY + "Kit Editor";
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

    public KitSelectorMenu(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        openPage(player, 1);
    }

    public void openPage(Player player, int page) {
        List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
        int totalKits = kits.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) totalKits / KITS_PER_PAGE));
        int currentPage = Math.min(Math.max(1, page), maxPages);

        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, MENU_TITLE);

        applyGlassBorder(inventory);
        setupNavigationAndFooter(inventory, currentPage, maxPages);

        int startIndex = (currentPage - 1) * KITS_PER_PAGE;
        int endIndex = Math.min(startIndex + KITS_PER_PAGE, totalKits);

        int slotIndex = 0;
        for (int i = startIndex; i < endIndex; i++) {
            Kit kit = kits.get(i);
            int slot = CENTER_GRID_SLOTS[slotIndex++];
            inventory.setItem(slot, createKitItem(kit));
        }

        player.openInventory(inventory);
    }

    private void applyGlassBorder(Inventory inventory) {
        ItemStack borderGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta meta = borderGlass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            borderGlass.setItemMeta(meta);
        }

        for (int slot : CENTER_GRID_SLOTS) {
            inventory.setItem(slot, null);
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, borderGlass);
            }
        }
    }

    private void setupNavigationAndFooter(Inventory inventory, int page, int maxPages) {
        // Back Button
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.RED + "Back");
            backMeta.setLore(Arrays.asList(ChatColor.GRAY + "Close this menu."));
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(SLOT_BACK, backButton);

        // Page Indicator
        ItemStack paperIndicator = new ItemStack(Material.PAPER);
        ItemMeta paperMeta = paperIndicator.getItemMeta();
        if (paperMeta != null) {
            paperMeta.setDisplayName(ChatColor.YELLOW + "Page " + page + "/" + maxPages);
            paperMeta.setLore(Arrays.asList(ChatColor.GRAY + "Viewing available kits."));
            paperIndicator.setItemMeta(paperMeta);
        }
        inventory.setItem(SLOT_PAGE_INDICATOR, paperIndicator);

        // Previous Page Arrow
        if (page > 1) {
            ItemStack prevArrow = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevArrow.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
                prevMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to go back."));
                prevArrow.setItemMeta(prevMeta);
            }
            inventory.setItem(SLOT_PREVIOUS_PAGE, prevArrow);
        }

        // Next Page Arrow
        if (page < maxPages) {
            ItemStack nextArrow = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextArrow.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
                nextMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to continue."));
                nextArrow.setItemMeta(nextMeta);
            }
            inventory.setItem(SLOT_NEXT_PAGE, nextArrow);
        }
    }

    private ItemStack createKitItem(Kit kit) {
        Material material = kit.getDisplayMaterial() != null
                ? kit.getDisplayMaterial()
                : Material.DIAMOND_SWORD;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));

            List<String> lore = new ArrayList<>();

            lore.addAll(kit.getColoredLore());

            meta.setLore(lore);

            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null ||
                !clickedItem.hasItemMeta() ||
                clickedItem.getItemMeta() == null ||
                !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        int clickedSlot = event.getRawSlot();

        // Handle Back button
        if (clickedSlot == SLOT_BACK) {
            player.closeInventory();
            return;
        }

        // Get current page from indicator item
        ItemStack indicator = event.getInventory().getItem(SLOT_PAGE_INDICATOR);
        int currentPage = 1;
        if (indicator != null && indicator.hasItemMeta() && indicator.getItemMeta().hasDisplayName()) {
            String title = indicator.getItemMeta().getDisplayName(); // §ePage X/Y
            try {
                String pagePart = title.replace(ChatColor.YELLOW + "Page ", "").split("/")[0];
                currentPage = Integer.parseInt(pagePart);
            } catch (Exception ignored) {}
        }

        // Handle Previous Page button
        if (clickedSlot == SLOT_PREVIOUS_PAGE && clickedItem.getType() == Material.ARROW) {
            openPage(player, currentPage - 1);
            return;
        }

        // Handle Next Page button
        if (clickedSlot == SLOT_NEXT_PAGE && clickedItem.getType() == Material.ARROW) {
            openPage(player, currentPage + 1);
            return;
        }

        // Handle Kit selection
        String displayName = ChatColor.stripColor(
                clickedItem.getItemMeta().getDisplayName()
        );

        Kit targetKit = plugin.getKitManager()
                .getAllKits()
                .stream()
                .filter(kit -> kit.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElse(null);

        if (targetKit == null) {
            return;
        }

        player.closeInventory();

        // Global kit editing only
        plugin.getKitEditorListener();
        plugin.getKitEditorListener().openEditor(
                player,
                targetKit.getId(),
                targetKit.getDisplayName(),
                targetKit.getMainContents().clone(),
                targetKit.getArmorContents().clone()
        );
    }
}