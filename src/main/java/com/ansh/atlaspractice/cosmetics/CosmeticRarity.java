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

package com.ansh.atlaspractice.cosmetics;

import org.bukkit.ChatColor;

public enum CosmeticRarity {
    COMMON("Common", ChatColor.WHITE), UNCOMMON("Uncommon", ChatColor.GREEN), RARE("Rare", ChatColor.BLUE),
    EPIC("Epic", ChatColor.DARK_PURPLE), LEGENDARY("Legendary", ChatColor.GOLD), MYTHIC("Mythic", ChatColor.LIGHT_PURPLE);
    private final String displayName; private final ChatColor color;
    CosmeticRarity(String displayName, ChatColor color) { this.displayName = displayName; this.color = color; }
    public String getDisplayName() { return displayName; }
    public ChatColor getColor() { return color; }
    public String getColoredName() { return color + displayName; }
}
