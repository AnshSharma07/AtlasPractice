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

package com.ansh.atlaspractice.util;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.party.Party;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class InventoryUtil {

    private final AtlasPracticePlugin plugin;

    public InventoryUtil(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void applyLobbyHotbarItems(Player player) {
        player.getInventory().clear();

        giveConfiguredItem(player, "lobby.unranked");
        giveConfiguredItem(player, "lobby.ranked");
        giveConfiguredItem(player, "lobby.party");
        giveConfiguredItem(player, "lobby.kit-editor");
        giveConfiguredItem(player, "lobby.cosmetics");
        giveConfiguredItem(player, "lobby.settings");

        player.updateInventory();
    }

    public void applyQueueHotbarItems(Player player) {
        player.getInventory().clear();

        giveConfiguredItem(player, "queue.leave");

        player.updateInventory();
    }

    public void applyPartyHotbarItems(Player player) {
        player.getInventory().clear();

        giveConfiguredItem(player, "party.ffa");
        giveConfiguredItem(player, "party.split");
        giveConfiguredItem(player, "party.pvp");
        giveConfiguredItem(player, "party.leave");
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile != null && profile.getPartyId() != null) {
            plugin.getPartyManager().getParty(profile.getPartyId()).ifPresent(party -> {
                if (party.isLeader(player.getUniqueId())) {
                    giveConfiguredItem(player, "party.disband");
                }
            });
        }

        player.updateInventory();
    }

    public void applySpectateHotbarItems(Player player) {
        player.getInventory().clear();

        giveConfiguredItem(player, "spectate.teleport");
        giveConfiguredItem(player, "spectate.inventory");
        giveConfiguredItem(player, "spectate.leave");

        player.updateInventory();
    }

    public ItemStack getConfiguredItem(String path) {
        String base = "hotbar." + path;

        Material material = Material.matchMaterial(
                plugin.getConfig().getString(base + ".material", "STONE")
        );

        if (material == null) {
            material = Material.STONE;
        }

        int amount = plugin.getConfig().getInt(base + ".amount", 1);

        short durability = (short) plugin.getConfig().getInt(base + ".durability", 0);

        boolean enchanted = plugin.getConfig().getBoolean(base + ".enchanted", false);

        String name = plugin.getConfig().getString(base + ".name", "");

        List<String> lore = plugin.getConfig().getStringList(base + ".lore");

        return buildItem(
                material,
                amount,
                durability,
                enchanted,
                name,
                lore
        );
    }

    public static String serializeItemStacks(ItemStack[] items) {
        if (items == null) {
            return "";
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            BukkitObjectOutputStream dataOutput =
                    new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();

            return Base64.getEncoder().encodeToString(
                    outputStream.toByteArray()
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to serialize ItemStacks.",
                    exception
            );
        }
    }

    public static ItemStack[] deserializeItemStacks(String data) {
        if (data == null || data.isEmpty()) {
            return new ItemStack[0];
        }

        try {
            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(
                            Base64.getDecoder().decode(data)
                    );

            BukkitObjectInputStream dataInput =
                    new BukkitObjectInputStream(inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                Object object = dataInput.readObject();

                if (object instanceof ItemStack) {
                    items[i] = (ItemStack) object;
                } else {
                    items[i] = null;
                }
            }

            dataInput.close();

            return items;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to deserialize ItemStacks.",
                    exception
            );
        }
    }

    private void giveConfiguredItem(Player player, String path) {
        String base = "hotbar." + path;

        int slot = plugin.getConfig().getInt(base + ".slot");

        player.getInventory().setItem(
                slot,
                getConfiguredItem(path)
        );
    }

    private ItemStack buildItem(Material material,
                                int amount,
                                short durability,
                                boolean enchanted,
                                String name,
                                List<String> lore) {

        ItemStack item = new ItemStack(
                material,
                amount,
                durability
        );

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(
                        name.replace("&", "§")
                );
            }

            if (lore != null && !lore.isEmpty()) {

                List<String> coloredLore = new ArrayList<>();

                for (String line : lore) {
                    coloredLore.add(
                            line.replace("&", "§")
                    );
                }

                meta.setLore(coloredLore);
            }

            if (enchanted) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            item.setItemMeta(meta);
        }

        if (enchanted) {
            item.addUnsafeEnchantment(
                    Enchantment.DURABILITY,
                    1
            );
        }

        return item;
    }
}