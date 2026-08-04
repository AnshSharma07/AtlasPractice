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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class Menu implements InventoryHolder {

    private final String title;
    private final int size;
    private final Map<Integer, Button> currentOpenButtons = new ConcurrentHashMap<>();

    public Menu(String title, int size){this.title=title;this.size=size;}
    public String getTitle(){return title;} public int getSize(){return size;} public Map<Integer, Button> getCurrentOpenButtons(){return currentOpenButtons;}

    public abstract Map<Integer, Button> getButtons(Player player);

    
    public void openMenu(Player player) {
        Inventory inventory = getInventory();

        this.currentOpenButtons.clear();
        Map<Integer, Button> dynamicButtons = getButtons(player);

        dynamicButtons.forEach((slot, button) -> {
            this.currentOpenButtons.put(slot, button);
            inventory.setItem(slot, button.getItemStack());
        });

        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Bukkit.createInventory(this, this.size, this.title);
    }
}
