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

package com.ansh.atlaspractice.preset.impl;

import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.preset.PresetKit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BedfightPreset implements PresetKit {

    @Override
    public String getId() {
        return "BEDFIGHT";
    }

    @Override
    public String getDisplayName() {
        return "Bedfight";
    }
    public ItemStack getDisplayIcon() {
        // Material.POTION, amount 1, short data value 16421 (Splash Instant Health II)
        return new ItemStack(Material.BED);
    }
    @Override
    public void setup(Kit kit) {

        // Armor
        ItemStack[] armor = new ItemStack[4];
        armor[0] = new ItemStack(Material.LEATHER_BOOTS);
        armor[1] = new ItemStack(Material.LEATHER_LEGGINGS);
        armor[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
        armor[3] = new ItemStack(Material.LEATHER_HELMET);

        // Inventory Contents
        ItemStack[] contents = new ItemStack[36];
        
        // Weapons & Tools
        contents[0] = new ItemStack(Material.WOOD_SWORD);
        contents[1] = new ItemStack(Material.SHEARS);

        // Efficiency 1 Wooden Pickaxe
        ItemStack pickaxe = new ItemStack(Material.WOOD_PICKAXE);
        ItemMeta pickMeta = pickaxe.getItemMeta();
        if (pickMeta != null) {
            pickMeta.addEnchant(Enchantment.DIG_SPEED, 1, true);
            pickaxe.setItemMeta(pickMeta);
        }
        contents[2] = pickaxe;

        // Efficiency 1 Wooden Axe
        ItemStack axe = new ItemStack(Material.WOOD_AXE);
        ItemMeta axeMeta = axe.getItemMeta();
        if (axeMeta != null) {
            axeMeta.addEnchant(Enchantment.DIG_SPEED, 1, true);
            axe.setItemMeta(axeMeta);
        }
        contents[3] = axe;

        // Blocks
        contents[4] = new ItemStack(Material.WOOL, 64);

        // Apply to Kit
        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setHungerLossEnabled(false);
        kit.setDurabilityEnabled(false);
    }
}
