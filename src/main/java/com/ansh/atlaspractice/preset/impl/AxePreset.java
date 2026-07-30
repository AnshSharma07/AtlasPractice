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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AxePreset implements PresetKit {

    @Override
    public String getId() {
        return "AXE";
    }

    @Override
    public String getDisplayName() {
        return "Axe";
    }

    @Override
    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.IRON_AXE);
    }

    @Override
    public void setup(Kit kit) {

        // Armor
        ItemStack[] armor = {
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_HELMET)
        };

        ItemStack[] contents = new ItemStack[36];

        // Iron Axe
        contents[0] = new ItemStack(Material.IRON_AXE);

        // Speed II (1:30 Drinkable)
        ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);
        PotionMeta speedMeta = (PotionMeta) speed.getItemMeta();
        speedMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.SPEED, 1800, 1),
                true
        );
        speed.setItemMeta(speedMeta);

        // Splash Heal II
        ItemStack heal = new ItemStack(Material.POTION, 1, (short) 16421);

        contents[1] = speed.clone();
        contents[2] = new ItemStack(Material.GOLDEN_APPLE, 16);

        // 6 heals in hotbar
        for (int i = 3; i <= 8; i++) {
            contents[i] = heal.clone();
        }

        // 2 extra speeds
        contents[25] = speed.clone();
        contents[26] = speed.clone();

        // Fill rest with heals
        for (int i = 9; i < 36; i++) {
            if (contents[i] == null) {
                contents[i] = heal.clone();
            }
        }

        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setHungerLossEnabled(true);
    }
}