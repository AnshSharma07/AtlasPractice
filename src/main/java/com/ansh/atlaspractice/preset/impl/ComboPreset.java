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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ComboPreset implements PresetKit {

    @Override
    public String getId() {
        return "COMBO";
    }

    @Override
    public ItemStack getDisplayIcon() {
        // Pufferfish
        return new ItemStack(Material.RAW_FISH, 1, (short) 3);
    }

    @Override
    public String getDisplayName() {
        return "Combo";
    }

    @Override
    public void setup(Kit kit) {

        // =========================
        // Armor
        // =========================
        ItemStack[] armor = new ItemStack[4];

        armor[0] = new ItemStack(Material.DIAMOND_BOOTS);
        armor[0].addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
        armor[0].addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        armor[1] = new ItemStack(Material.DIAMOND_LEGGINGS);
        armor[1].addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
        armor[1].addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        armor[2] = new ItemStack(Material.DIAMOND_CHESTPLATE);
        armor[2].addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
        armor[2].addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        armor[3] = new ItemStack(Material.DIAMOND_HELMET);
        armor[3].addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
        armor[3].addUnsafeEnchantment(Enchantment.DURABILITY, 10);

        // =========================
        // Inventory
        // =========================
        ItemStack[] contents = new ItemStack[36];

        // Diamond Sword
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
        sword.addEnchantment(Enchantment.DURABILITY, 3);
        contents[0] = sword;

        // 64 Notch Apples
        contents[1] = new ItemStack(Material.GOLDEN_APPLE, 64, (short) 1);

        // Backup Armor
        ItemStack extraBoots = new ItemStack(Material.DIAMOND_BOOTS);
        extraBoots.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
        extraBoots.addEnchantment(Enchantment.DURABILITY, 3);
        contents[2] = extraBoots;

        ItemStack extraLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        extraLeggings.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
        extraLeggings.addEnchantment(Enchantment.DURABILITY, 3);
        contents[3] = extraLeggings;

        ItemStack extraChest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        extraChest.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
        extraChest.addEnchantment(Enchantment.DURABILITY, 3);
        contents[4] = extraChest;

        ItemStack extraHelmet = new ItemStack(Material.DIAMOND_HELMET);
        extraHelmet.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 10);
        extraHelmet.addEnchantment(Enchantment.DURABILITY, 3);
        contents[5] = extraHelmet;

        // =========================
        // Speed II Potion (3:00)
        // =========================
        ItemStack speedPot = new ItemStack(Material.POTION, 1, (short) 8226);
        PotionMeta speedMeta = (PotionMeta) speedPot.getItemMeta();
        speedMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.SPEED, 3600, 1),
                true
        );
        speedPot.setItemMeta(speedMeta);
        contents[6] = speedPot;

        // =========================
        // Strength II Potion (3:00)
        // =========================
        ItemStack strengthPot = new ItemStack(Material.POTION, 1, (short) 8233);
        PotionMeta strengthMeta = (PotionMeta) strengthPot.getItemMeta();
        strengthMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 3600, 1),
                true
        );
        strengthPot.setItemMeta(strengthMeta);
        contents[7] = strengthPot;

        // =========================
        // Apply Kit
        // =========================
        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setComboMode(true);
        kit.setHungerLossEnabled(true);
    }
}