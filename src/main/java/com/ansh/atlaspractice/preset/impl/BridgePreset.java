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

package com.ansh.atlaspractice.preset.impl;

import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.preset.PresetKit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BridgePreset implements PresetKit {

    @Override
    public String getId() {
        return "BRIDGE";
    }

    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.STAINED_CLAY);
    }

    @Override
    public String getDisplayName() {
        return "Bridge";
    }

    @Override
    public void setup(Kit kit) {

        // Armor
        ItemStack[] armor = new ItemStack[4];

        armor[0] = new ItemStack(Material.LEATHER_BOOTS);
        armor[1] = new ItemStack(Material.LEATHER_LEGGINGS);
        armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
        armor[3] = new ItemStack(Material.LEATHER_HELMET);

        ItemStack[] contents = new ItemStack[36];
        contents[0] = new ItemStack(Material.IRON_SWORD);

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.addEnchant(Enchantment.ARROW_DAMAGE, 3, true);
        bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        bow.setItemMeta(bowMeta);
        contents[1] = bow;

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta pickMeta = pickaxe.getItemMeta();
        pickMeta.addEnchant(Enchantment.DIG_SPEED, 5, true);
        pickMeta.addEnchant(Enchantment.DURABILITY, 3, true);
        pickaxe.setItemMeta(pickMeta);
        contents[2] = pickaxe;

        contents[3] = new ItemStack(Material.STAINED_CLAY, 64);
        ItemStack goldenHead = new ItemStack(Material.GOLDEN_APPLE, 8);
        ItemMeta headMeta = goldenHead.getItemMeta();
        headMeta.setDisplayName(ChatColor.GOLD + "Golden Head");
        goldenHead.setItemMeta(headMeta);
        contents[4] = goldenHead;

        contents[5] = new ItemStack(Material.ARROW, 1);

        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setHungerLossEnabled(false);
        kit.setDurabilityEnabled(false);
    }
}