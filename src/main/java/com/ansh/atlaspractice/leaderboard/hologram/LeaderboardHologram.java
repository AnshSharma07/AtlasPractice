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

package com.ansh.atlaspractice.leaderboard.hologram;

import com.ansh.atlaspractice.leaderboard.LeaderboardCache;
import com.ansh.atlaspractice.leaderboard.StatType;
import com.ansh.atlaspractice.profile.KitStats;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardHologram {

    private final String name;
    private final Location location;
    private final StatType type;
    private final String kit; // "NONE" or kit name
    private final LeaderboardCache cache;
    private final Hologram hologram;

    public LeaderboardHologram(String name, Location location, StatType type, String kit, LeaderboardCache cache) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.kit = (kit == null || kit.equalsIgnoreCase("NONE")) ? null : kit;
        this.cache = cache;
        this.hologram = new Hologram(location);
    }

    public void updateLines() {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.AQUA + ChatColor.BOLD.toString() + "Top " + type.name().replace("_", " "));
        if (kit != null) {
            lines.add(ChatColor.GRAY + "(" + kit + ")");
        }
        lines.add("");

        List<Profile> top = cache.getTopProfiles(type, kit);
        for (int i = 0; i < 10; i++) {
            if (i < top.size()) {
                Profile p = top.get(i);
                lines.add(ChatColor.YELLOW + "#" + (i + 1) + " " + ChatColor.WHITE + p.getLastKnownName() + ChatColor.GRAY + " - " + ChatColor.AQUA + getStat(p));
            } else {
                lines.add(ChatColor.YELLOW + "#" + (i + 1) + " " + ChatColor.GRAY + "N/A");
            }
        }

        hologram.destroy();
        hologram.setLines(lines);
        spawn();
    }

    private String getStat(Profile p) {
        if (kit != null) {
            KitStats ks = p.getKitStats(kit);
            switch (type) {
                case WINS: return String.valueOf(ks.getWins());
                case LOSSES: return String.valueOf(ks.getLosses());
                case KILLS: return String.valueOf(ks.getKills());
                case DEATHS: return String.valueOf(ks.getDeaths());
                case WINSTREAK: return String.valueOf(ks.getWinstreak());
                case BEST_WINSTREAK: return String.valueOf(ks.getBestWinstreak());
                case MATCHES: return String.valueOf(ks.getMatches());
                case GLOBAL_ELO:
                default:
                    return String.valueOf(ks.getElo());
            }
        }

        switch (type) {
            case WINS: return String.valueOf(p.getWins());
            case LOSSES: return String.valueOf(p.getLosses());
            case KILLS: return String.valueOf(p.getKills());
            case DEATHS: return String.valueOf(p.getDeaths());
            case WINSTREAK: return String.valueOf(p.getWinStreak());
            case BEST_WINSTREAK: return String.valueOf(p.getBestWinStreak());
            case MATCHES: return String.valueOf(p.getMatchesPlayed());
            case GLOBAL_ELO:
            default:
                return String.format("%.0f", p.getAllKitStats().values().stream()
                        .mapToInt(KitStats::getElo)
                        .average()
                        .orElse(1000.0));
        }
    }

    public void spawn() {
        if (location.getWorld() == null) return;
        for (Player p : location.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(location) < 2500) { // 50 blocks
                hologram.spawn(p);
            }
        }
    }

    public void destroy() {
        hologram.destroy();
    }

    public String getName() { return name; }
}