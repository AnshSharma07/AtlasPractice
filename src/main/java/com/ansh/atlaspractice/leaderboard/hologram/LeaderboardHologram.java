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

import com.ansh.atlaspractice.leaderboard.LeaderboardCache;
import com.ansh.atlaspractice.leaderboard.StatType;
import com.ansh.atlaspractice.profile.KitStats;
import com.ansh.atlaspractice.profile.Profile;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardHologram {

    private final String name;
    private final Location location;

    @Getter
    private final StatType type;

    private final String kit;
    private final LeaderboardCache cache;
    private final LeaderboardDisplaySettings settings;
    private final Hologram hologram;

    public LeaderboardHologram(String name, Location location, StatType type, String kit,
                               LeaderboardCache cache, LeaderboardDisplaySettings settings) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.kit = kit == null || kit.equalsIgnoreCase("NONE") ? null : kit;
        this.cache = cache;
        this.settings = settings;
        this.hologram = new Hologram(location);
    }

    public void updateLines() {
        List<String> lines = new ArrayList<>();
        String stat = getStatName();
        String kitName = kit == null ? "Overall" : kit;

        String title = settings.apply(settings.getTitle(), 0, "", "", stat, kitName);
        if (!title.isEmpty()) {
            lines.add(title);
        }

        if (kit != null && !settings.getKitLine().isEmpty()) {
            lines.add(settings.apply(settings.getKitLine(), 0, "", "", stat, kitName));
        }

        List<Profile> top = cache.getTopProfiles(type, kit);

        for (int i = 0; i < settings.getMaxEntries(); i++) {
            if (i < top.size()) {
                Profile profile = top.get(i);

                lines.add(settings.apply(
                        settings.getEntry(),
                        i + 1,
                        profile.getLastKnownName(),
                        getStat(profile),
                        stat,
                        kitName
                ));
            } else {
                lines.add(settings.apply(
                        settings.getEmpty(),
                        i + 1,
                        "",
                        "",
                        stat,
                        kitName
                ));
            }
        }

        if (!settings.getFooter().isEmpty()) {
            lines.add(settings.apply(settings.getFooter(), 0, "", "", stat, kitName));
        }

        hologram.destroy();
        hologram.setLines(lines);
        spawn();
    }

    private String getStatName() {
        switch (type) {
            case KIT_ELO:
            case GLOBAL_ELO:
                return "ELO";
            default:
                return type.name().replace('_', ' ');
        }
    }

    private String getStat(Profile profile) {
        if (kit != null) {
            KitStats stats = profile.findKitStats(kit);
            if (stats == null) {
                return "0";
            }

            switch (type) {
                case WINS:
                    return String.valueOf(stats.getWins());
                case LOSSES:
                    return String.valueOf(stats.getLosses());
                case KILLS:
                    return String.valueOf(stats.getKills());
                case DEATHS:
                    return String.valueOf(stats.getDeaths());
                case WINSTREAK:
                    return String.valueOf(stats.getWinstreak());
                case BEST_WINSTREAK:
                    return String.valueOf(stats.getBestWinstreak());
                case MATCHES:
                    return String.valueOf(stats.getMatches());
                case KIT_ELO:
                case GLOBAL_ELO:
                default:
                    return String.valueOf(stats.getElo());
            }
        }

        switch (type) {
            case WINS:
                return String.valueOf(profile.getWins());
            case LOSSES:
                return String.valueOf(profile.getLosses());
            case KILLS:
                return String.valueOf(profile.getKills());
            case DEATHS:
                return String.valueOf(profile.getDeaths());
            case WINSTREAK:
                return String.valueOf(profile.getWinStreak());
            case BEST_WINSTREAK:
                return String.valueOf(profile.getBestWinStreak());
            case MATCHES:
                return String.valueOf(profile.getMatchesPlayed());
            case GLOBAL_ELO:
                return String.format("%.0f", profile.getAllKitStats().values().stream()
                        .filter(stats -> stats.getMatches() > 0)
                        .mapToInt(KitStats::getElo)
                        .average()
                        .orElse(1000.0));
            default:
                return "0";
        }
    }

    public void spawn() {
        if (location.getWorld() == null) {
            return;
        }

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) < 2500) {
                hologram.spawn(player);
            }
        }
    }

    public void spawnForPlayer(Player player) {
        if (!location.getWorld().getUID().equals(player.getWorld().getUID())) {
            return;
        }

        if (player.getLocation().distanceSquared(location) < 2500) {
            hologram.spawn(player);
        }
    }

    public void destroy() {
        hologram.destroy();
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location.clone();
    }

    public String getKit() {
        return kit;
    }
}