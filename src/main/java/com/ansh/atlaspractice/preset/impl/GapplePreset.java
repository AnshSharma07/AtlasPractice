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

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.preset.PresetKit;

public class GapplePreset implements PresetKit {
    @Override
    public String getId() { return "GAPPLE"; }

    @Override
    public String getDisplayName() { return "Gapple"; }
    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.GOLDEN_APPLE);
    }
    @Override
    public void setup(Kit kit) {
        // Armor (Protection 4, Unbreaking 3)
        ItemStack[] armor = new ItemStack[4];

        armor[0] = new ItemStack(Material.DIAMOND_BOOTS);
        armor[0].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        armor[0].addEnchantment(Enchantment.DURABILITY, 3);

        armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS);
        armor[1].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        armor[1].addEnchantment(Enchantment.DURABILITY, 3);

        armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE);
        armor[2].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        armor[2].addEnchantment(Enchantment.DURABILITY, 3);

        armor[3] = new ItemStack(Material.DIAMOND_HELMET);
        armor[3].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        armor[3].addEnchantment(Enchantment.DURABILITY, 3);

        // Inventory Initialization
        ItemStack[] contents = new ItemStack[36];

        // Sword (Sharpness 5, Unbreaking 3, Fire Aspect 2)
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 5);
        sword.addEnchantment(Enchantment.DURABILITY, 3);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
        contents[0] = sword;

        contents[1] = new ItemStack(Material.GOLDEN_APPLE, 64, (short) 1);
 ItemStack speedPot = new ItemStack(Material.POTION);
        PotionMeta speedMeta = (PotionMeta) speedPot.getItemMeta();
        speedMeta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 3600, 1), true);
        speedPot.setItemMeta(speedMeta);
        contents[2] = speedPot;
   ItemStack strengthPot = new ItemStack(Material.POTION);
        PotionMeta strengthMeta = (PotionMeta) strengthPot.getItemMeta();
        strengthMeta.addCustomEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3600, 1), true);
        strengthPot.setItemMeta(strengthMeta);
        contents[3] = strengthPot;
        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setHungerLossEnabled(true);
    }
}
