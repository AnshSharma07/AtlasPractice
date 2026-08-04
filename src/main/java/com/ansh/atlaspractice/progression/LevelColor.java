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

package com.ansh.atlaspractice.progression;

import org.bukkit.ChatColor;

public final class LevelColor {
    private LevelColor() {}
    public static ChatColor getColor(int level) {
        if (level >= 100) return ChatColor.DARK_RED;
        if (level >= 90) return ChatColor.BLUE;
        if (level >= 80) return ChatColor.LIGHT_PURPLE;
        if (level >= 70) return ChatColor.DARK_PURPLE;
        if (level >= 60) return ChatColor.RED;
        if (level >= 50) return ChatColor.GOLD;
        if (level >= 40) return ChatColor.YELLOW;
        if (level >= 30) return ChatColor.AQUA;
        if (level >= 20) return ChatColor.GREEN;
        if (level >= 10) return ChatColor.GRAY;
        return ChatColor.WHITE;
    }
    public static String formatLevel(int level) {
        return getColor(level).toString() + (level >= 100 ? ChatColor.BOLD.toString() : "") + level;
    }
}
