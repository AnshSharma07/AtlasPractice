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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class FinalUHCPreset implements PresetKit {

    @Override
    public String getId() {
        return "FINAL_UHC";
    }

    @Override
    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.DIAMOND_PICKAXE);
    }

    @Override
    public String getDisplayName() {
        return "Final UHC";
    }

    @Override
    public void setup(Kit kit) {

// ==========================================
        // Worn Armor
        // ==========================================
        ItemStack[] armor = new ItemStack[4];

        // Boots: 429 max - 189 remaining = 240 damage, Prot 2
        ItemStack wornBoots = new ItemStack(Material.DIAMOND_BOOTS, 1, (short) 240);
        wornBoots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        armor[0] = wornBoots;

        // Leggings: 495 max - 220 remaining = 275 damage, Prot 3
        ItemStack wornLegs = new ItemStack(Material.DIAMOND_LEGGINGS, 1, (short) 275);
        wornLegs.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        armor[1] = wornLegs;

        // Chestplate: 528 max - 253 remaining = 275 damage, Prot 3
        ItemStack wornChest = new ItemStack(Material.DIAMOND_CHESTPLATE, 1, (short) 275);
        wornChest.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        armor[2] = wornChest;

        // Helmet: 363 max - 155 remaining = 208 damage, Prot 2
        ItemStack wornHelmet = new ItemStack(Material.DIAMOND_HELMET, 1, (short) 208);
        wornHelmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        armor[3] = wornHelmet;

        // ==========================================
        // Inventory Contents (36 Slots)
        // ==========================================
        ItemStack[] contents = new ItemStack[36];

        // --- HOTBAR (Slots 0-8) ---
        
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
        contents[0] = sword;

        contents[1] = new ItemStack(Material.FISHING_ROD);

        // Golden Head (10s Regen variant) - Count: 4
        ItemStack goldenHead = new ItemStack(Material.GOLDEN_APPLE, 4);
        ItemMeta headMeta = goldenHead.getItemMeta();
        headMeta.setDisplayName(ChatColor.GOLD + "Golden Head");
        headMeta.setLore(Arrays.asList(ChatColor.BLACK + "10s_Regen"));
        goldenHead.setItemMeta(headMeta);
        contents[2] = goldenHead;

        contents[3] = new ItemStack(Material.GOLDEN_APPLE, 24);
        contents[4] = new ItemStack(Material.COBBLESTONE, 64);
        contents[5] = new ItemStack(Material.WATER_BUCKET);
        contents[6] = new ItemStack(Material.LAVA_BUCKET);
        
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        axe.addEnchantment(Enchantment.DIG_SPEED, 1);
        contents[7] = axe;
        
        contents[8] = new ItemStack(Material.WOOD, 64); // Oak Planks

        // --- INVENTORY MIDDLE ROW (Slots 18-26) ---

        // Secondary Armor with specific durability damages (Max - Remaining = Damage)
        // Helmet: 363 max - 263 remaining = 100 damage
        ItemStack invHelmet = new ItemStack(Material.DIAMOND_HELMET, 1, (short) 100);
        invHelmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        contents[18] = invHelmet;

        // Chestplate: 528 max - 383 remaining = 145 damage
        ItemStack invChest = new ItemStack(Material.DIAMOND_CHESTPLATE, 1, (short) 145);
        invChest.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        contents[19] = invChest;

        // Leggings: 495 max - 350 remaining = 145 damage
        ItemStack invLegs = new ItemStack(Material.DIAMOND_LEGGINGS, 1, (short) 145);
        invLegs.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        contents[20] = invLegs;

        // Boots: 429 max - 309 remaining = 120 damage
        ItemStack invBoots = new ItemStack(Material.DIAMOND_BOOTS, 1, (short) 120);
        invBoots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        contents[21] = invBoots;

        contents[22] = new ItemStack(Material.COOKED_BEEF, 64);

        // Flint and Steel (64 max - 14 uses remaining = 50 damage)
        contents[26] = new ItemStack(Material.FLINT_AND_STEEL, 1, (short) 50);


        // --- INVENTORY BOTTOM ROW (Slots 27-35) ---
        
        contents[30] = new ItemStack(Material.COBBLESTONE, 64);
        contents[31] = new ItemStack(Material.WOOD, 64); // Oak Planks
        contents[33] = new ItemStack(Material.WATER_BUCKET);
        contents[34] = new ItemStack(Material.LAVA_BUCKET);

        ItemStack pickaxe = new ItemStack(Material.DIAMOND_PICKAXE);
        pickaxe.addEnchantment(Enchantment.DIG_SPEED, 1);
        contents[35] = pickaxe;

        // Apply arrays to kit
        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        
        // Final UHC specific settings
        kit.setHungerLossEnabled(true);
        kit.setDurabilityEnabled(true);
    }
}