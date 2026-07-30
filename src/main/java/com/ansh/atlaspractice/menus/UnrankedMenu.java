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

package com.ansh.atlaspractice.menus;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class UnrankedMenu extends Menu {

    private final AtlasPracticePlugin plugin;
    private final UUID explicitDuelTarget;

    public UnrankedMenu(AtlasPracticePlugin plugin, UUID explicitDuelTarget) {
        super(explicitDuelTarget == null ? "§8Unranked Queue" : "§8Select Kit", 6 * 9);
        this.plugin = plugin;
        this.explicitDuelTarget = explicitDuelTarget;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Fill every slot with gray glass
        ItemStack filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }

        for (int i = 0; i < getSize(); i++) {
            buttons.put(i, new Button(filler, (p, click) -> {
                // Decorative filler - does nothing
            }));
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

            ItemStack icon = new ItemStack(
                    kit.getDisplayMaterial() != null
                            ? kit.getDisplayMaterial()
                            : Material.STONE_SWORD
            );

            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§f" + kit.getDisplayName());

                List<String> lore = new ArrayList<>();

                if (explicitDuelTarget == null) {
                    lore.add("§7Fight other players");
                    lore.add("§7using this kit.");
                    lore.add("");
                    lore.add("§eClick to join the queue.");
                } else {
                    lore.add("§7Challenge a player");
                    lore.add("§7using this kit.");
                    lore.add("");
                    lore.add("§eClick to continue.");
                }

                meta.setLore(lore);
                icon.setItemMeta(meta);
            }

            buttons.put(slots.get(index++), new Button(icon, (p, clickType) -> {
                p.closeInventory();

                if (p.hasMetadata("party-split")) {
                    p.removeMetadata("party-split", plugin);
                    plugin.getPartyManager().executePartySplit(p, kit);
                    return;
                }

                if (explicitDuelTarget == null) {
                    plugin.getQueueManager().joinUnrankedQueue(p, kit);
                } else {
                    new DuelRoundSettingsMenu(plugin, explicitDuelTarget, kit).openMenu(p);
                }
            }));
        }

        return buttons;
    }
}