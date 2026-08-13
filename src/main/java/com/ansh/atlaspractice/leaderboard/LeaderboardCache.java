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

package com.ansh.atlaspractice.leaderboard;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.profile.KitStats;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LeaderboardCache {

    private final AtlasPracticePlugin plugin;
    private final Map<LeaderboardKey, List<Profile>> rankings = new ConcurrentHashMap<>();
    private int taskId = -1;

    public LeaderboardCache(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void startCachingTask() {
        this.taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::refreshCache,
                20L,
                6000L
        ).getTaskId();
    }

    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public void refreshNow() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::refreshCache);
    }

    private void refreshCache() {
        List<Profile> allProfiles = new ArrayList<>(plugin.getProfileManager().getProfiles());
        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());

        for (StatType type : StatType.values()) {
            if (type == StatType.KIT_ELO) {
                for (Kit kit : allKits) {
                    cacheKit(type, kit.getId(), allProfiles);
                }
                continue;
            }

            LeaderboardKey globalKey = new LeaderboardKey(type, null);
            rankings.put(globalKey, buildGlobalRanking(type, allProfiles));

            for (Kit kit : allKits) {
                cacheKit(type, kit.getId(), allProfiles);
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> plugin.getLeaderboardManager().updateAllHolograms());
    }

    private void cacheKit(StatType type, String kitId, List<Profile> profiles) {
        LeaderboardKey key = new LeaderboardKey(type, kitId);
        rankings.put(key, buildKitRanking(type, kitId, profiles));
    }

    private List<Profile> buildGlobalRanking(StatType type, List<Profile> profiles) {
        return profiles.stream()
                .filter(p -> hasGlobalValue(p, type))
                .sorted(getGlobalComparator(type).reversed().thenComparing(Profile::getLastKnownName, String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<Profile> buildKitRanking(StatType type, String kitId, List<Profile> profiles) {
        return profiles.stream()
                .filter(p -> hasKitValue(p, type, kitId))
                .sorted(getKitComparator(type, kitId).reversed().thenComparing(Profile::getLastKnownName, String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .collect(Collectors.toList());
    }

    private boolean hasGlobalValue(Profile profile, StatType type) {
        switch (type) {
            case WINS:
                return profile.getWins() > 0;
            case LOSSES:
                return profile.getLosses() > 0;
            case KILLS:
                return profile.getKills() > 0;
            case DEATHS:
                return profile.getDeaths() > 0;
            case WINSTREAK:
                return profile.getWinStreak() > 0;
            case BEST_WINSTREAK:
                return profile.getBestWinStreak() > 0;
            case MATCHES:
                return profile.getMatchesPlayed() > 0;
            case LEVEL:
                return profile.getLevel() > 1;
            case EXPERIENCE:
                return profile.getExperience() > 0;
            case COINS:
                return profile.getCoins() > 0;
            case GLOBAL_ELO:
                return profile.getAllKitStats().values().stream().anyMatch(stats -> stats.getMatches() > 0);
            case KIT_ELO:
            default:
                return false;
        }
    }

    private boolean hasKitValue(Profile profile, StatType type, String kitId) {
        KitStats stats = profile.findKitStats(kitId);
        if (stats == null) {
            return false;
        }

        switch (type) {
            case WINS:
                return stats.getWins() > 0;
            case LOSSES:
                return stats.getLosses() > 0;
            case KILLS:
                return stats.getKills() > 0;
            case DEATHS:
                return stats.getDeaths() > 0;
            case WINSTREAK:
                return stats.getWinstreak() > 0;
            case BEST_WINSTREAK:
                return stats.getBestWinstreak() > 0;
            case MATCHES:
                return stats.getMatches() > 0;
            case GLOBAL_ELO:
            case KIT_ELO:
                return stats.getMatches() > 0;
            default:
                return false;
        }
    }

    public List<Profile> getTopProfiles(StatType type, String kit) {
        LeaderboardKey key = new LeaderboardKey(type, kit);
        return rankings.getOrDefault(key, Collections.emptyList());
    }

    private Comparator<Profile> getGlobalComparator(StatType type) {
        switch (type) {
            case WINS:
                return Comparator.comparingInt(Profile::getWins);
            case LOSSES:
                return Comparator.comparingInt(Profile::getLosses);
            case KILLS:
                return Comparator.comparingInt(Profile::getKills);
            case DEATHS:
                return Comparator.comparingInt(Profile::getDeaths);
            case WINSTREAK:
                return Comparator.comparingInt(Profile::getWinStreak);
            case BEST_WINSTREAK:
                return Comparator.comparingInt(Profile::getBestWinStreak);
            case MATCHES:
                return Comparator.comparingInt(Profile::getMatchesPlayed);
            case LEVEL:
                return Comparator.comparingInt(Profile::getLevel);
            case EXPERIENCE:
                return Comparator.comparingLong(Profile::getExperience);
            case COINS:
                return Comparator.comparingLong(Profile::getCoins);
            case GLOBAL_ELO:
            default:
                return Comparator.comparingDouble(profile -> profile.getAllKitStats().values().stream()
                        .filter(stats -> stats.getMatches() > 0)
                        .mapToInt(KitStats::getElo)
                        .average()
                        .orElse(0.0));
        }
    }

    private Comparator<Profile> getKitComparator(StatType type, String kitId) {
        switch (type) {
            case WINS:
                return Comparator.comparingInt(p -> p.findKitStats(kitId).getWins());
            case LOSSES:
                return Comparator.comparingInt(p -> p.findKitStats(kitId).getLosses());
            case KILLS:
                return Comparator.comparingInt(p -> p.findKitStats(kitId).getKills());
            case DEATHS:
                return Comparator.comparingInt(p -> p.findKitStats(kitId).getDeaths());
            case WINSTREAK:
                return Comparator.comparingInt(p -> p.findKitStats(kitId).getWinstreak());
            case BEST_WINSTREAK:
                return Comparator.comparingInt(p -> p.findKitStats(kitId).getBestWinstreak());
            case MATCHES:
                return Comparator.comparingInt(p -> p.findKitStats(kitId).getMatches());
            case GLOBAL_ELO:
            case KIT_ELO:
            default:
                return Comparator.comparingInt(p -> p.findKitStats(kitId).getElo());
        }
    }
}
