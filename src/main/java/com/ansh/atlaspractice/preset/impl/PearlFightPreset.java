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

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.preset.PresetKit;

public class PearlFightPreset implements PresetKit {
    @Override
    public String getId() { return "PEARLFIGHT"; }

    @Override
    public String getDisplayName() { return "PearlFight"; }
    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.ENDER_PEARL);
    }
    @Override
    public void setup(Kit kit) {
        // Armor (Left empty as per your setup)
        ItemStack[] armor = new ItemStack[4];

        // Inventory
        ItemStack[] contents = new ItemStack[36];

        // Slot 0: Stick (Knockback 1)
        ItemStack kbStick = new ItemStack(Material.STICK);
        kbStick.addEnchantment(Enchantment.KNOCKBACK, 1);
        contents[0] = kbStick;

        // Slot 1: 8 Ender Pearls
        contents[1] = new ItemStack(Material.ENDER_PEARL, 8);

        // Slot 2: 16 Wool
        contents[2] = new ItemStack(Material.WOOL, 16);

        // Slot 3: 1 Shears
        contents[3] = new ItemStack(Material.SHEARS, 1);

        // Effects
        // Metadata

        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setHungerLossEnabled(false);
        kit.setDamageEnabled(false);
        kit.setDurabilityEnabled(false);
    }
}
