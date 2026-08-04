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

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;


public final class Button {

    private final ItemStack itemStack;
    private final BiConsumer<Player, ClickType> clickAction;

    public Button(ItemStack itemStack, BiConsumer<Player, ClickType> clickAction)
    {
        this.itemStack=itemStack;
        this.clickAction=clickAction;
    }
    public ItemStack getItemStack()
    {
        return itemStack;
    }
    public BiConsumer<Player, ClickType> getClickAction()
    {
        return clickAction;
    }

    
    public void trigger(Player player, ClickType clickType) {
        if (this.clickAction != null) {
            this.clickAction.accept(player, clickType);
        }
    }
}
