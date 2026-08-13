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

package com.ansh.atlaspractice.leaderboard.hologram;

import org.bukkit.ChatColor;

public final class LeaderboardDisplaySettings {
    private final int maxEntries;
    private final String title;
    private final String kitLine;
    private final String entry;
    private final String empty;
    private final String footer;

    public LeaderboardDisplaySettings(int maxEntries, String title, String kitLine, String entry, String empty, String footer) {
        this.maxEntries = Math.max(1, maxEntries);
        this.title = title;
        this.kitLine = kitLine;
        this.entry = entry;
        this.empty = empty;
        this.footer = footer;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public String getTitle() {
        return title;
    }

    public String getKitLine() {
        return kitLine;
    }

    public String getEntry() {
        return entry;
    }

    public String getEmpty() {
        return empty;
    }

    public String getFooter() {
        return footer;
    }

    public String apply(String template, int position, String player, String value, String stat, String kit) {
        if (template == null) {
            return "";
        }

        String result = template
                .replace("{position}", String.valueOf(position))
                .replace("{player}", player)
                .replace("{value}", value)
                .replace("{stat}", stat)
                .replace("{type}", stat)
                .replace("{kit}", kit);

        return ChatColor.translateAlternateColorCodes('&', result);
    }
}
