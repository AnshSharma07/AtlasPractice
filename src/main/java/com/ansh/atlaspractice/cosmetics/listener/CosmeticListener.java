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

package com.ansh.atlaspractice.cosmetics.listener;

import com.ansh.atlaspractice.cosmetics.CosmeticType;
import com.ansh.atlaspractice.cosmetics.menu.CosmeticCategoryMenu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CosmeticListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getClickedInventory() == null) {
            return;
        }

        String title = event.getView().getTitle();

        if (!title.startsWith(ChatColor.DARK_AQUA.toString())) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (ChatColor.stripColor(title).equalsIgnoreCase("Cosmetics")) {
            handleMainMenu(player, event.getSlot());
            return;
        }

        CosmeticType type = getType(title);

        if (type == null) {
            return;
        }

        new CosmeticCategoryMenu(player, type).handleClick(event.getSlot());
    }

    private void handleMainMenu(Player player, int slot) {
        switch (slot) {
            case 10:
                new CosmeticCategoryMenu(player, CosmeticType.KILL_EFFECT).open();
                break;
            case 11:
                new CosmeticCategoryMenu(player, CosmeticType.VICTORY_EFFECT).open();
                break;
            case 12:
                new CosmeticCategoryMenu(player, CosmeticType.PROJECTILE_TRAIL).open();
                break;
            case 13:
                new CosmeticCategoryMenu(player, CosmeticType.WALKING_TRAIL).open();
                break;
            case 14:
                new CosmeticCategoryMenu(player, CosmeticType.AURA).open();
                break;
            case 15:
                new CosmeticCategoryMenu(player, CosmeticType.CHAT_COLOR).open();
                break;
            case 16:
                new CosmeticCategoryMenu(player, CosmeticType.KILL_MESSAGE).open();
                break;
        }
    }

    private CosmeticType getType(String title) {
        title = ChatColor.stripColor(title);

        if (title.equalsIgnoreCase("Kill Effects"))
            return CosmeticType.KILL_EFFECT;

        if (title.equalsIgnoreCase("Victory Effects"))
            return CosmeticType.VICTORY_EFFECT;

        if (title.equalsIgnoreCase("Projectile Trails"))
            return CosmeticType.PROJECTILE_TRAIL;

        if (title.equalsIgnoreCase("Walking Trails"))
            return CosmeticType.WALKING_TRAIL;

        if (title.equalsIgnoreCase("Auras"))
            return CosmeticType.AURA;

        if (title.equalsIgnoreCase("Chat Colors"))
            return CosmeticType.CHAT_COLOR;

        if (title.equalsIgnoreCase("Kill Messages"))
            return CosmeticType.KILL_MESSAGE;

        return null;
    }
}