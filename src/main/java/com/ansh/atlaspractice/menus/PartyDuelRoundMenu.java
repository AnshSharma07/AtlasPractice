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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PartyDuelRoundMenu extends Menu {

    private final AtlasPracticePlugin plugin;
    private final UUID targetLeader;
    private final Kit chosenKit;

    public PartyDuelRoundMenu(AtlasPracticePlugin plugin, UUID targetLeader, Kit chosenKit) {
        super("Select Party Format", 9);
        this.plugin = plugin;
        this.targetLeader = targetLeader;
        this.chosenKit = chosenKit;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        
        buttons.put(2, new Button(createFormatIcon(Material.IRON_SWORD, "§b§lBest of 1"), (p, type) -> {
            p.closeInventory();
            sendChallenge(p, 1);
        }));
        
        buttons.put(4, new Button(createFormatIcon(Material.DIAMOND_SWORD, "§b§lBest of 3"), (p, type) -> {
            p.closeInventory();
            sendChallenge(p, 2);
        }));
        
        buttons.put(6, new Button(createFormatIcon(Material.NETHER_STAR, "§b§lBest of 5"), (p, type) -> {
            p.closeInventory();
            sendChallenge(p, 3);
        }));
        
        return buttons;
    }

    private void sendChallenge(Player p, int requiredWins) {
        Player tgt = Bukkit.getPlayer(targetLeader);
        if (tgt != null) {
            plugin.getPartyManager().sendPartyDuel(p, tgt, chosenKit, requiredWins);
        } else {
            p.sendMessage("§cTarget player is no longer online.");
        }
    }

    private ItemStack createFormatIcon(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList("§7Select round requirements."));
            item.setItemMeta(meta);
        }
        return item;
    }
}