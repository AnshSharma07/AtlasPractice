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
        // Run completely asynchronously every 5 minutes (6000 ticks)
        this.taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::refreshCache, 20L, 60).getTaskId();
    }

    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private void refreshCache() {
        List<Profile> allProfiles = new ArrayList<>(plugin.getProfileManager().getProfiles());
        List<Kit> allKits = new ArrayList<>(plugin.getKitManager().getAllKits());

        for (StatType type : StatType.values()) {
            if (type == StatType.KIT_ELO) continue; // Deprecated in favor of generic per-kit types

            //  Calculate Global Rankings
            LeaderboardKey globalKey = new LeaderboardKey(type, null);
            List<Profile> globalSorted = allProfiles.stream()
                    .sorted(getGlobalComparator(type).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            rankings.put(globalKey, globalSorted);

            // 2. Calculate Per-Kit Rankings for every kit
            for (Kit kit : allKits) {
                LeaderboardKey kitKey = new LeaderboardKey(type, kit.getId());
                List<Profile> kitSorted = allProfiles.stream()
                        .sorted(getKitComparator(type, kit.getId()).reversed())
                        .limit(10)
                        .collect(Collectors.toList());
                rankings.put(kitKey, kitSorted);
            }
        }

        Bukkit.getScheduler().runTask(plugin, () -> plugin.getLeaderboardManager().updateAllHolograms());
    }

    public List<Profile> getTopProfiles(StatType type, String kit) {
        LeaderboardKey key = new LeaderboardKey(type, kit);
        return rankings.getOrDefault(key, Collections.emptyList());
    }

    private Comparator<Profile> getGlobalComparator(StatType type) {
        switch (type) {
            case WINS: return Comparator.comparingInt(Profile::getWins);
            case LOSSES: return Comparator.comparingInt(Profile::getLosses);
            case KILLS: return Comparator.comparingInt(Profile::getKills);
            case DEATHS: return Comparator.comparingInt(Profile::getDeaths);
            case WINSTREAK: return Comparator.comparingInt(Profile::getWinStreak);
            case BEST_WINSTREAK: return Comparator.comparingInt(Profile::getBestWinStreak);
            case MATCHES: return Comparator.comparingInt(Profile::getMatchesPlayed);
            case LEVEL: return Comparator.comparingInt(Profile::getLevel);
            case EXPERIENCE: return Comparator.comparingLong(Profile::getExperience);
            case COINS: return Comparator.comparingLong(Profile::getCoins);
            case GLOBAL_ELO:
            default:
                return Comparator.comparingDouble(p -> p.getAllKitStats().values().stream()
                        .mapToInt(KitStats::getElo)
                        .average()
                        .orElse(1000.0));
        }
    }

    private Comparator<Profile> getKitComparator(StatType type, String kitId) {
        switch (type) {
            case WINS: return Comparator.comparingInt(p -> p.getKitStats(kitId).getWins());
            case LOSSES: return Comparator.comparingInt(p -> p.getKitStats(kitId).getLosses());
            case KILLS: return Comparator.comparingInt(p -> p.getKitStats(kitId).getKills());
            case DEATHS: return Comparator.comparingInt(p -> p.getKitStats(kitId).getDeaths());
            case WINSTREAK: return Comparator.comparingInt(p -> p.getKitStats(kitId).getWinstreak());
            case BEST_WINSTREAK: return Comparator.comparingInt(p -> p.getKitStats(kitId).getBestWinstreak());
            case MATCHES: return Comparator.comparingInt(p -> p.getKitStats(kitId).getMatches());
            case GLOBAL_ELO:
            default:
                return Comparator.comparingInt(p -> p.getKitStats(kitId).getElo());
        }
    }
}