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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.preset.PresetKit;
import org.bukkit.inventory.meta.ItemMeta;

public class BuildUHCPreset implements PresetKit {

    @Override
    public String getId() { return "BUILDUHC"; }
    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.LAVA_BUCKET);
    }
    @Override
    public String getDisplayName() { return "BuildUHC"; }

    @Override
    public void setup(Kit kit) {
        // Armor (Protection 2)
        ItemStack[] armor = new ItemStack[4];

        armor[0] = new ItemStack(Material.DIAMOND_BOOTS);
        armor[0].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS);
        armor[1].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE);
        armor[2].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        armor[3] = new ItemStack(Material.DIAMOND_HELMET);
        armor[3].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);

        // Inventory
        ItemStack[] contents = new ItemStack[36];

        // Weapons & Tools
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 3); // Sharpness 3
        contents[0] = sword;

        contents[1] = new ItemStack(Material.FISHING_ROD);

        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 3); // Power 3
        contents[2] = bow;

        contents[3] = new ItemStack(Material.DIAMOND_AXE);
        contents[4] = new ItemStack(Material.DIAMOND_PICKAXE);

        // Food & Golden Apples
        contents[5] = new ItemStack(Material.COOKED_BEEF, 64); // 64 Steak
        contents[6] = new ItemStack(Material.GOLDEN_APPLE, 6);  // 6 Golden Apples

        // ==========================================
// Golden Head (10s Regen, No Instant Heal)
// ==========================================
        ItemStack goldenHead10s = new ItemStack(Material.GOLDEN_APPLE, 3);
        ItemMeta head10sMeta = goldenHead10s.getItemMeta();
        head10sMeta.setDisplayName(ChatColor.GOLD + "Golden Head");

// Add a hidden lore line to tell the listener to use the 10-second rule
        head10sMeta.setLore(java.util.Arrays.asList(ChatColor.BLACK + "10s_Regen"));
        goldenHead10s.setItemMeta(head10sMeta);

        contents[7] = goldenHead10s;

        // Building Blocks
        contents[8] = new ItemStack(Material.COBBLESTONE, 64);
        contents[9] = new ItemStack(Material.WOOD, 64); // Oak Planks in 1.8

        // Buckets
        contents[10] = new ItemStack(Material.LAVA_BUCKET);
        contents[11] = new ItemStack(Material.LAVA_BUCKET);
        contents[12] = new ItemStack(Material.WATER_BUCKET);
        contents[13] = new ItemStack(Material.WATER_BUCKET);
  contents[35] = new ItemStack(Material.ARROW, 16);

        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setHungerLossEnabled(true);
    }
}
