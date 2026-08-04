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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BotKitMenu extends Menu {
    private static final String[] SUPPORTED_KITS = {
            "Boxing", "Debuff", "NoDebuff", "Combo", "BuildUHC", "FinalUHC", "Iron", "Gapple",
            "Sumo", "StickFight", "PearlFight", "BattleRush", "Bridge"
    };

    public BotKitMenu() {
        super("&8Select Bot Kit", 54);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int slot = 10;
        for (String kitId : SUPPORTED_KITS) {
            Optional<Kit> kitOptional = AtlasPracticePlugin.getInstance().getKitManager().getKit(kitId);
            if (!kitOptional.isPresent()) continue;
            Kit kit = kitOptional.get();
            buttons.put(slot, new Button(createItem(kit), (p, clickType) -> new BotDifficultyMenu(kit).openMenu(p)));
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        return buttons;
    }

    private ItemStack createItem(Kit kit) {
        Material material = kit.getIconMaterial() == null || kit.getIconMaterial() == Material.AIR
                ? (kit.isBoxingMode() ? Material.DIAMOND_SWORD : kit.getDisplayMaterial()) : kit.getIconMaterial();
        if (material == null || material == Material.AIR) material = Material.IRON_SWORD;
        ItemStack item = new ItemStack(material, 1, (short) kit.getIconData());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', kit.getDisplayName()));
            meta.setLore(Arrays.asList("§7Kit-aware PvP bot", "", "§eClick to choose difficulty!"));
            item.setItemMeta(meta);
        }
        return item;
    }
}
