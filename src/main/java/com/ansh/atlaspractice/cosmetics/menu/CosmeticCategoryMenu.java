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

package com.ansh.atlaspractice.cosmetics.menu;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.cosmetics.Cosmetic;
import com.ansh.atlaspractice.cosmetics.CosmeticPlayerData;
import com.ansh.atlaspractice.cosmetics.CosmeticRegistry;
import com.ansh.atlaspractice.cosmetics.CosmeticType;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class CosmeticCategoryMenu {

    private final Player player;
    private final CosmeticType type;
    private final List<Cosmetic> cosmetics;
    private Inventory inventory;

    public CosmeticCategoryMenu(Player player, CosmeticType type) {
        this.player = player;
        this.type = type;
        this.cosmetics = CosmeticRegistry.getCosmetics(type);
    }

    public void open() {
        inventory = Bukkit.createInventory(
                null,
                54,
                ChatColor.DARK_AQUA + "" + ChatColor.BOLD + getTitle()
        );

        populate();
        fillBorders();

        player.openInventory(inventory);
    }

    private void populate() {
        int slot = 10;

        for (Cosmetic cosmetic : cosmetics) {
            inventory.setItem(slot, createItem(cosmetic));
            slot++;

            if (slot == 17) slot = 19;
            if (slot == 26) slot = 28;
            if (slot == 35) slot = 37;
            if (slot >= 44) break;
        }

        inventory.setItem(45, createBackItem());
        inventory.setItem(49, createCloseItem());
    }

    private void fillBorders() {
        ItemStack darkGlass = createGlass((short) 15, " ");
        ItemStack cyanGlass = createGlass((short) 9, " ");

        // Corner accents
        int[] cyanSlots = {0, 8, 53};
        for (int slot : cyanSlots) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, cyanGlass);
            }
        }

        // Fill remaining background slots
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

    private String getTitle() {
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

    private ItemStack createItem(Cosmetic cosmetic) {
        ItemStack item = new ItemStack(cosmetic.getIcon());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + cosmetic.getDisplayName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "------------------------");

        Profile profile = AtlasPracticePlugin.getInstance().getProfileManager().getProfile(player.getUniqueId());
        boolean owned = profile != null && profile.getCosmetics().owns(type, cosmetic.getId());

        lore.add(ChatColor.GRAY + "Price: " + ChatColor.GOLD + cosmetic.getPrice() + " coins");
        if (profile != null) lore.add(ChatColor.GRAY + "Your Coins: " + ChatColor.YELLOW + profile.getCoins());
        lore.add("");

        if (isSelected(cosmetic)) {
            lore.add(ChatColor.GRAY + "Status: " + ChatColor.GREEN + ChatColor.BOLD + "EQUIPPED");
            lore.add("");
            lore.add(ChatColor.GREEN + "âœ” Currently Active");
        } else if (owned) {
            lore.add(ChatColor.GRAY + "Status: " + ChatColor.AQUA + "OWNED");
            lore.add("");
            lore.add(ChatColor.YELLOW + "â–º Click to equip");
        } else {
            lore.add(ChatColor.GRAY + "Status: " + ChatColor.RED + ChatColor.BOLD + "LOCKED");
            lore.add("");
            lore.add(ChatColor.YELLOW + "â–º Click to purchase");
        }

        lore.add(ChatColor.DARK_GRAY + "------------------------");
        lore.add(ChatColor.DARK_GRAY + "ID: " + cosmetic.getId());

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Â« Back");
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "âœ– Close");
        item.setItemMeta(meta);
        return item;
    }

    private boolean isSelected(Cosmetic cosmetic) {
        Profile profile = AtlasPracticePlugin.getInstance()
                .getProfileManager()
                .getProfile(player.getUniqueId());

        if (profile == null) return false;

        CosmeticPlayerData data = profile.getCosmetics();
        if (data == null) return false;

        switch (type) {
            case KILL_EFFECT: return cosmetic.getId().equalsIgnoreCase(data.getKillEffect());
            case VICTORY_EFFECT: return cosmetic.getId().equalsIgnoreCase(data.getVictoryEffect());
            case PROJECTILE_TRAIL: return cosmetic.getId().equalsIgnoreCase(data.getProjectileTrail());
            case WALKING_TRAIL: return cosmetic.getId().equalsIgnoreCase(data.getWalkingTrail());
            case AURA: return cosmetic.getId().equalsIgnoreCase(data.getAura());
            case CHAT_COLOR: return cosmetic.getId().equalsIgnoreCase(data.getChatColor());
            case KILL_MESSAGE: return cosmetic.getId().equalsIgnoreCase(data.getKillMessage());
            default: return false;
        }
    }

    public void handleClick(int slot) {
        if (slot == 45) {
            new CosmeticsMenu(player).open();
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        Cosmetic cosmetic = getCosmetic(slot);
        if (cosmetic == null) {
            return;
        }

        select(cosmetic);
        open();
    }

    private Cosmetic getCosmetic(int slot) {
        int index = -1;

        switch (slot) {
            case 10: index = 0; break;
            case 11: index = 1; break;
            case 12: index = 2; break;
            case 13: index = 3; break;
            case 14: index = 4; break;
            case 15: index = 5; break;
            case 16: index = 6; break;

            case 19: index = 7; break;
            case 20: index = 8; break;
            case 21: index = 9; break;
            case 22: index = 10; break;
            case 23: index = 11; break;
            case 24: index = 12; break;
            case 25: index = 13; break;

            case 28: index = 14; break;
            case 29: index = 15; break;
            case 30: index = 16; break;
            case 31: index = 17; break;
            case 32: index = 18; break;
            case 33: index = 19; break;
            case 34: index = 20; break;

            case 37: index = 21; break;
            case 38: index = 22; break;
            case 39: index = 23; break;
            case 40: index = 24; break;
            case 41: index = 25; break;
            case 42: index = 26; break;
            case 43: index = 27; break;
        }

        if (index == -1 || index >= cosmetics.size()) {
            return null;
        }

        return cosmetics.get(index);
    }

    private void select(Cosmetic cosmetic) {
        Profile profile = AtlasPracticePlugin.getInstance()
                .getProfileManager()
                .getProfile(player.getUniqueId());

        if (profile == null) return;

        CosmeticPlayerData data = profile.getCosmetics();
        if (data == null) return;

        if (!data.owns(type, cosmetic.getId())) {
            int price = cosmetic.getPrice();
            if (profile.getCoins() < price) {
                player.sendMessage(ChatColor.RED + "You need " + price + " coins to buy this cosmetic.");
                return;
            }
            AtlasPracticePlugin.getInstance().getLevelManager().removeCoins(profile, price);
            AtlasPracticePlugin.getInstance().getLevelManager().sendPurchaseActionBar(profile, price);

            data.addOwned(type, cosmetic.getId());

            player.sendMessage(ChatColor.GREEN + "Purchased " + cosmetic.getDisplayName() + " for " + price + " coins.");
        }

        switch (type) {
            case KILL_EFFECT: data.setKillEffect(cosmetic.getId()); break;
            case VICTORY_EFFECT: data.setVictoryEffect(cosmetic.getId()); break;
            case PROJECTILE_TRAIL: data.setProjectileTrail(cosmetic.getId()); break;
            case WALKING_TRAIL: data.setWalkingTrail(cosmetic.getId()); break;
            case AURA: data.setAura(cosmetic.getId()); break;
            case CHAT_COLOR: data.setChatColor(cosmetic.getId()); break;
            case KILL_MESSAGE: data.setKillMessage(cosmetic.getId()); break;
        }

        AtlasPracticePlugin.getInstance().getDatabaseService().saveProfileDataAsync(profile);
        player.sendMessage(ChatColor.GREEN + "Selected " + cosmetic.getDisplayName() + ".");
    }
}