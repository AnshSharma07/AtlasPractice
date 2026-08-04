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
import com.ansh.atlaspractice.listeners.HealthSoupListener;
import com.ansh.atlaspractice.preset.PresetKit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SoupPreset implements PresetKit {

    @Override
    public String getId() {
        return "SOUP";
    }

    @Override
    public String getDisplayName() {
        return "Soup";
    }

    @Override
    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.MUSHROOM_SOUP);
    }

    @Override
    public void setup(Kit kit) {

        // Armor
        ItemStack[] armor = new ItemStack[4];
        armor[0] = new ItemStack(Material.IRON_BOOTS);
        armor[1] = new ItemStack(Material.IRON_LEGGINGS);
        armor[2] = new ItemStack(Material.IRON_CHESTPLATE);
        armor[3] = new ItemStack(Material.IRON_HELMET);

        // Inventory
        ItemStack[] contents = new ItemStack[36];

        // Sharpness I Diamond Sword
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        contents[0] = sword;

        // Speed II (1:30 Drinkable)
        ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
        PotionMeta speedMeta = (PotionMeta) speed.getItemMeta();
        speedMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.SPEED, 1800, 1),
                true
        );
        speed.setItemMeta(speedMeta);

        // 3 Speed Potions
        contents[1] = speed.clone();
        contents[34] = speed.clone();
        contents[35] = speed.clone();

        // Fill remaining slots with Health Soup
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) {
                contents[i] = HealthSoupListener.createHealthSoup();
            }
        }

        kit.setArmorContents(armor);
        kit.setMainContents(contents); kit.setHungerLossEnabled(false);

    }
}