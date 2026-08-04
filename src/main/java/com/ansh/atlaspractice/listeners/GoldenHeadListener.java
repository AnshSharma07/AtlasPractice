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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.Plugin;

public class GoldenHeadListener implements Listener {

    private final Plugin plugin;

    public GoldenHeadListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        org.bukkit.inventory.ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.GOLDEN_APPLE && item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Golden Head")) {

                boolean is10sVariant = item.getItemMeta().hasLore() &&
                        item.getItemMeta().getLore().get(0).contains("10s_Regen");

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                  player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0), true);

                    if (is10sVariant) {
                         player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1), true);

                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1), true);

                        // Instantly fill the player's health
                        player.setHealth(player.getMaxHealth());
                    }

                }, 1L);
            }
        }
    }
}