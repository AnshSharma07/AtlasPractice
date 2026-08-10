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

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.preset.PresetKit;

public class HCFPreset implements PresetKit {
    @Override
    public String getId() { return "HCF"; }
    public ItemStack getDisplayIcon() {
        // Material.RAW_FISH, amount 1, short data value 3 (Pufferfish)
        return new ItemStack(Material.FENCE);
    }
    @Override
    public String getDisplayName() { return "HCF"; }

    @Override
    public void setup(Kit kit) {
        // Armor (Protection 2, Unbreaking 3)
        ItemStack[] armor = new ItemStack[4];

        armor[0] = new ItemStack(Material.DIAMOND_BOOTS);
        armor[0].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        armor[0].addEnchantment(Enchantment.DURABILITY, 3);

        armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS);
        armor[1].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        armor[1].addEnchantment(Enchantment.DURABILITY, 3);

        armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE);
        armor[2].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        armor[2].addEnchantment(Enchantment.DURABILITY, 3);

        armor[3] = new ItemStack(Material.DIAMOND_HELMET);
        armor[3].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        armor[3].addEnchantment(Enchantment.DURABILITY, 3);

        // Inventory Setup
        ItemStack[] contents = new ItemStack[36];

        // Slot 0: Diamond Sword (Sharpness 3, Unbreaking 3, Fire Aspect 2)
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
        sword.addEnchantment(Enchantment.DURABILITY, 3);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
        contents[0] = sword;

        // Slot 1: 16 Ender Pearls
        contents[1] = new ItemStack(Material.ENDER_PEARL, 16);

        // Base Splash Potion templates for HCF
        // Data value 16421 is standard Splash Healing II, 16418 is Splash Speed II, 16427 is Splash Fire Resistance
        ItemStack splashHealII = new ItemStack(Material.POTION, 1, (short) 16421);

        ItemStack speedPot = new ItemStack(Material.POTION, 1, (short) 16418);
        PotionMeta speedMeta = (PotionMeta) speedPot.getItemMeta();
        speedMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 1800, 1), true); // 1:30 = 1800 ticks
        speedPot.setItemMeta(speedMeta);

        ItemStack fireResPot = new ItemStack(Material.POTION, 1, (short) 16427);
        PotionMeta fireMeta = (PotionMeta) fireResPot.getItemMeta();
        fireMeta.addCustomEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 9600, 0), true); // 8:00 = 9600 ticks
        fireResPot.setItemMeta(fireMeta);

        // Slot 2: Speed II (1 min 30 sec)
        contents[2] = speedPot.clone();

        // Slot 3: Fire Resistance (8 min)
        contents[3] = fireResPot.clone();

        // Slot 4: 16 Golden Apples (Regular)
        contents[4] = new ItemStack(Material.GOLDEN_APPLE, 16);

        // Slot 5: Fishing Rod
        contents[5] = new ItemStack(Material.FISHING_ROD);

        // Slot 6 & 7: Instant Health 2 Potions
        contents[6] = splashHealII.clone();
        contents[7] = splashHealII.clone();

        // Slot 8: 64 Steak
        contents[8] = new ItemStack(Material.COOKED_BEEF, 64);

        // Inventory Rows (Slots 9 to 11): 3 Speed II Potions
        contents[9] = speedPot.clone();
        contents[10] = speedPot.clone();
        contents[11] = speedPot.clone();

        // Fill remaining empty inventory slots (12 to 35) with Instant Health II
        for (int i = 12; i < contents.length; i++) {
            if (contents[i] == null) {
                contents[i] = splashHealII.clone();
            }
        }

        // Apply to Kit
        kit.setArmorContents(armor);
        kit.setMainContents(contents); kit.setHungerLossEnabled(true);
    }
}
