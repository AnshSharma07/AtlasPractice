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
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PartyDuelKitMenu extends Menu {

    private final AtlasPracticePlugin plugin;
    private final UUID targetLeader;

    public PartyDuelKitMenu(AtlasPracticePlugin plugin, UUID targetLeader) {
        super("Choose Challenge Kit", 3 * 9);
        this.plugin = plugin;
        this.targetLeader = targetLeader;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int slot = 0;
        
        for (Kit kit : this.plugin.getKitManager().getRegistry().getAllKits()) {
            if (slot >= getSize()) break;
            
            ItemStack icon = new ItemStack(kit.getDisplayMaterial() != null ? kit.getDisplayMaterial() : Material.STONE_SWORD);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e§l" + kit.getDisplayName());
                meta.setLore(java.util.Arrays.asList("§7Active Fighting Context: §fParty PvP", "", "§aClick to select format."));
                icon.setItemMeta(meta);
            }

            buttons.put(slot++, new Button(icon, (p, clickType) -> {
                p.closeInventory();
                new PartyDuelRoundMenu(this.plugin, this.targetLeader, kit).openMenu(p);
            }));
        }
        return buttons;
    }
}