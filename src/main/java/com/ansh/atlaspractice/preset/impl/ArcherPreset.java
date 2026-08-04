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
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArcherPreset implements PresetKit {

    @Override
    public String getId() {
        return "ARCHER";
    }
    @Override
    public ItemStack getDisplayIcon() {
        // Material.POTION, amount 1, short data value 16421 (Splash Instant Health II)
        return new ItemStack(Material.BOW);
    }
    @Override
    public String getDisplayName() {
        return "Archer";
    }

    @Override
    public void setup(Kit kit) {

        // Armor
        ItemStack[] armor = new ItemStack[4];

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta bootsMeta = boots.getItemMeta();
        bootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        boots.setItemMeta(bootsMeta);

        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemMeta leggingsMeta = leggings.getItemMeta();
        leggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        leggings.setItemMeta(leggingsMeta);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemMeta chestMeta = chestplate.getItemMeta();
        chestMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        chestplate.setItemMeta(chestMeta);

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta helmetMeta = helmet.getItemMeta();
        helmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        helmet.setItemMeta(helmetMeta);

        armor[0] = boots;
        armor[1] = leggings;
        armor[2] = chestplate;
        armor[3] = helmet;

        ItemStack[] contents = new ItemStack[36];

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        bow.setItemMeta(bowMeta);

        contents[0] = bow;
        contents[1] = new ItemStack(Material.ARROW, 1);
        contents[8] = new ItemStack(Material.COOKED_BEEF, 8);

        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setHungerLossEnabled(true);

    }
}