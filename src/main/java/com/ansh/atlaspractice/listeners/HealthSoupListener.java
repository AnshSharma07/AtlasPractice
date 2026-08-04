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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HealthSoupListener implements Listener {

    public static ItemStack createHealthSoup() {
        ItemStack soup = new ItemStack(Material.MUSHROOM_SOUP, 1);
        ItemMeta meta = soup.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Health Soup");
            soup.setItemMeta(meta);
        }
        return soup;
    }

    @EventHandler
    public void onSoupRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        // Check if the player rclicked
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = player.getItemInHand();

            if (item != null && item.getType() == Material.MUSHROOM_SOUP && item.hasItemMeta()) {
                if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Health Soup")) {
                     event.setCancelled(true);
  if (player.getHealth() < player.getMaxHealth()) {
                        double newHealth = player.getHealth() + 5.0;

                        player.setHealth(Math.min(newHealth, player.getMaxHealth()));

                        // Remove the soup
                        player.setItemInHand(new ItemStack(Material.AIR));
                        
                        // Update inventory to prevent ghost items in 1.8
                        player.updateInventory();
                    }
                }
            }
        }
    }
}