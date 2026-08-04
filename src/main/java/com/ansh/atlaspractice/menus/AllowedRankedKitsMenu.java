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

package com.ansh.atlaspractice.menus;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.config.RankedConfig;
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class AllowedRankedKitsMenu extends Menu {

    private final AtlasPracticePlugin plugin;

    public AllowedRankedKitsMenu(AtlasPracticePlugin plugin) {
        super("§8Ranked Kit Toggle", 6 * 9);
        this.plugin = plugin;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        RankedConfig rankedConfig = plugin.getRankedConfig();

        // Filler
        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) { fillerMeta.setDisplayName(" "); filler.setItemMeta(fillerMeta); }
        for (int i = 0; i < getSize(); i++) {
            buttons.put(i, new Button(filler, (p, c) -> {}));
        }

        List<Integer> slots = Arrays.asList(
                10,11,12,13,14,15,16,
                19,20,21,22,23,24,25,
                28,29,30,31,32,33,34,
                37,38,39,40,41,42,43
        );

        int index = 0;
        for (Kit kit : plugin.getKitManager().getRegistry().getAllKits()) {
            if (index >= slots.size()) break;

            boolean enabled = rankedConfig.isKitEnabled(kit.getId());
            ItemStack icon = new ItemStack(
                    kit.getDisplayMaterial() != null ? kit.getDisplayMaterial() : Material.STONE_SWORD
            );
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName((enabled ? "§a" : "§c") + kit.getDisplayName());
                meta.setLore(Arrays.asList(
                        enabled ? "§aâœ” Enabled for Ranked" : "§câœ˜ Disabled for Ranked",
                        "",
                        "§eClick to toggle."
                ));
                if (enabled) {
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
                icon.setItemMeta(meta);
            }

            final String kitId = kit.getId();
            buttons.put(slots.get(index++), new Button(icon, (p, clickType) -> {
                boolean nowEnabled = !rankedConfig.isKitEnabled(kitId);
                rankedConfig.setKitEnabled(kitId, nowEnabled);
                p.sendMessage("§e" + kit.getDisplayName() + " §7ranked: " + (nowEnabled ? "§aEnabled" : "§cDisabled"));
                // Reopen to refresh icons
                new AllowedRankedKitsMenu(plugin).openMenu(p);
            }));
        }

        return buttons;
    }
}