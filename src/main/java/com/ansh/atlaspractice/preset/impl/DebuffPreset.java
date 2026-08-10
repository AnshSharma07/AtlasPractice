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

public class DebuffPreset implements PresetKit {

    @Override
    public String getId() {
        return "DEBUFF";
    }

    @Override
    public String getDisplayName() {
        return "Debuff";
    }

    @Override
    public ItemStack getDisplayIcon() {
        return new ItemStack(Material.POTION, 1, (short) 16428); // Poison Splash
    }

    @Override
    public void setup(Kit kit) {

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

        ItemStack[] contents = new ItemStack[36];

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);
        sword.addEnchantment(Enchantment.DURABILITY, 3);
        sword.addEnchantment(Enchantment.FIRE_ASPECT, 2);
        contents[0] = sword;

        contents[1] = new ItemStack(Material.ENDER_PEARL, 16);

        ItemStack speed = new ItemStack(Material.POTION, 1, (short) 8226);

        PotionMeta speedMeta = (PotionMeta) speed.getItemMeta();
        speedMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.SPEED, 1800, 1),
                true);
        speed.setItemMeta(speedMeta);

        ItemStack fireRes = new ItemStack(Material.POTION, 1, (short) 8259);

        PotionMeta fireMeta = (PotionMeta) fireRes.getItemMeta();
        fireMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 3600, 0),
                true);
        fireRes.setItemMeta(fireMeta);


        ItemStack heal = new ItemStack(Material.POTION, 1, (short) 16421);

        ItemStack slow = new ItemStack(Material.POTION, 1, (short) 16394);

        PotionMeta slowMeta = (PotionMeta) slow.getItemMeta();
        slowMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.SLOW, 1340, 0),
                true);
        slow.setItemMeta(slowMeta);

        ItemStack poison = new ItemStack(Material.POTION, 1, (short) 16420);

        PotionMeta poisonMeta = (PotionMeta) poison.getItemMeta();
        poisonMeta.addCustomEffect(
                new PotionEffect(PotionEffectType.POISON, 660, 0),
                true);
        poison.setItemMeta(poisonMeta);


        contents[2] = speed.clone();
        contents[3] = fireRes.clone();
        contents[4] = new ItemStack(Material.GOLDEN_APPLE, 16);
        contents[5] = slow.clone();
        contents[6] = poison.clone();
        contents[7] = heal.clone();
        contents[8] = new ItemStack(Material.COOKED_BEEF, 64);

        // 3 more Speed II (Total = 4)
        contents[9] = speed.clone();
        contents[10] = speed.clone();
        contents[11] = speed.clone();

        // Remaining 4 Slowness (Total = 5)
        contents[12] = slow.clone();
        contents[13] = slow.clone();
        contents[14] = slow.clone();
        contents[15] = slow.clone();

        // Fill remaining slots with Instant Health II
        for (int i = 16; i < 36; i++) {
            contents[i] = heal.clone();
        }

        kit.setArmorContents(armor);
        kit.setMainContents(contents);
        kit.setHungerLossEnabled(true);
    }
}