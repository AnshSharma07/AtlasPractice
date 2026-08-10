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
// THESE KITS CODES ARE COMPLETED WITH THE HELP OF AI TO SKIP BASIC REPEATED STUFF
package com.ansh.atlaspractice.preset.impl;

import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.preset.PresetKit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class TopFightPreset implements PresetKit {

    @Override
    public String getId() {
        return "TOP_FIGHT";
    }

    @Override
    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.WOOL);
    }

    @Override
    public String getDisplayName() {
        return "TopFight";
    }

    @Override
    public void setup(Kit kit) {

        // ==========================================
        // Worn Armor (Iron Boots & Helmet, Leather Chestplate & Leggings)
        // ==========================================
        ItemStack[] armor = new ItemStack[4];
        armor[0] = new ItemStack(Material.IRON_BOOTS);
        armor[1] = new ItemStack(Material.IRON_LEGGINGS);
        armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
        armor[3] = new ItemStack(Material.LEATHER_HELMET);

        // ==========================================
        // Inventory Contents (36 Slots)
        // ==========================================
        ItemStack[] contents = new ItemStack[36];

        // --- HOTBAR (Slots 0-8) ---
        
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        contents[0] = sword;

        // 5 Wool blocks
        contents[1] = new ItemStack(Material.WOOL, 5);

        // Apply arrays to kit
        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        
        kit.setHungerLossEnabled(false);
        kit.setDurabilityEnabled(false);
    }
}