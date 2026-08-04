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

package com.ansh.atlaspractice.team;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public final class KitColorUtil {

    private KitColorUtil() {}

    public static ItemStack color(ItemStack item, TeamColor team) {
        if (item == null || team == null) {
            return item;
        }

        ItemStack clone = item.clone();
        Material material = clone.getType();

        // Wool
        if (material == Material.WOOL) {
            clone.setDurability(team.getWoolData());
            return clone;
        }

        // Bridge blocks
        if (material == Material.STAINED_CLAY) {
            clone.setDurability(team.getWoolData());
            return clone;
        }

        // Leather armour
        if (material == Material.LEATHER_HELMET
                || material == Material.LEATHER_CHESTPLATE
                || material == Material.LEATHER_LEGGINGS
                || material == Material.LEATHER_BOOTS) {

            ItemMeta meta = clone.getItemMeta();

            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta leather = (LeatherArmorMeta) meta;
                leather.setColor(team.getLeatherColor());
                clone.setItemMeta(leather);
            }

            return clone;
        }

        return clone;
    }

    public static ItemStack[] colorInventory(
            ItemStack[] items,
            TeamColor team
    ) {

        if (items == null)
            return null;

        ItemStack[] result = new ItemStack[items.length];

        for (int i = 0; i < items.length; i++) {
            result[i] = color(items[i], team);
        }

        return result;
    }

    public static ItemStack[] colorArmor(
            ItemStack[] armor,
            TeamColor team
    ) {

        if (armor == null)
            return null;

        ItemStack[] result = new ItemStack[armor.length];

        for (int i = 0; i < armor.length; i++) {
            result[i] = color(armor[i], team);
        }

        return result;
    }

}