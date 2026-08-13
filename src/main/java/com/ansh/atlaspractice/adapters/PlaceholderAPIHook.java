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

package com.ansh.atlaspractice.adapters;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.leaderboard.LeaderboardManager;
import com.ansh.atlaspractice.leaderboard.StatType;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


@RequiredArgsConstructor
public final class PlaceholderAPIHook extends PlaceholderExpansion {

    private final AtlasPracticePlugin plugin;
    private final ProfileManager profileManager;

    @Override
    public @NotNull String getIdentifier() {
        return "atlaspractice";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ansh";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.3";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {

        if (player == null) {
            return "";
        }

        Profile profile = profileManager.getProfile(player.getUniqueId());

        if (profile == null) {
            return "0";
        }

        params = params.toLowerCase();

        if (params.equals("level")) return String.valueOf(profile.getLevel());
        if (params.equals("xp")) return String.valueOf(profile.getExperience());
        if (params.equals("xp_current")) return String.valueOf(plugin.getLevelManager().getXPIntoCurrentLevel(profile.getExperience()));
        if (params.equals("xp_needed")) return String.valueOf(plugin.getLevelManager().getXPForNextLevel(profile.getLevel()));
        if (params.equals("coins")) return String.valueOf(profile.getCoins());

        // %atlaspractice_state%
        if (params.equals("state")) {
            return profile.getState().name();
        }

        // %atlaspractice_global_elo%
        if (params.equals("global_elo")) {
            return String.valueOf(profile.getEloForKit("global"));
        }

        // %atlaspractice_elo_nodebuff%
        // %atlaspractice_elo_builduhc%
        if (params.startsWith("elo_")) {
            String kitId = params.substring(4);
            return String.valueOf(profile.getEloForKit(kitId));
        }
        // %atlaspractice_kills%
        if (params.equals("kills")) {
            return String.valueOf(profile.getKills());
        }

        // %atlaspractice_deaths%
        if (params.equals("deaths")) {
            return String.valueOf(profile.getDeaths());
        }

        // %atlaspractice_wins%
        if (params.equals("wins")) {
            return String.valueOf(profile.getWins());
        }

        // %atlaspractice_kdr%
        if (params.equals("kdr")) {
            double kdr = profile.getDeaths() == 0 ? profile.getKills() : (double) profile.getKills() / profile.getDeaths();
            return String.format("%.2f", kdr);
        }

//        /*
//         * Uncomment after MatchManager is implemented in AtlasPracticePlugin
//         *
//         * if (params.equals("playing")) {
//         *     return String.valueOf(
//         *             plugin.getMatchManager().getLiveMatches().size() * 2
//         *     );
//         * }
//         */

        /*
         * Uncomment after PartyManager is aded in AtlasPracticePlugin
         *
         * if (params.equals("party_size")) {
         *
         *     if (profile.getActivePartyId() == null) {
         *         return "0";
         *     }
         *
         *     return String.valueOf(
         *             plugin.getPartyManager()
         *                     .getParty(profile.getActivePartyId())
         *                     .map(party -> party.getMembers().size())
         *                     .orElse(0)
         *     );
         * }
         */
        if (params.startsWith("top_")) {

            String[] split = params.split("_");

            // Format:
            // top_wins_1_name
            // top_wins_1_value
            // top_kills_3_name
            // top_deaths_10_value
            if (split.length != 4) {
                return "";
            }

            String category = split[1];

            int position;
            try {
                position = Integer.parseInt(split[2]) - 1;
            } catch (NumberFormatException e) {
                return "";
            }

            String field = split[3];

            List<Profile> entries;

            switch (category) {
                case "wins":
                    entries = plugin.getLeaderboardManager().getCache().getTopProfiles(StatType.WINS, "");
                    break;

                case "kills":
                    entries = plugin.getLeaderboardManager().getCache().getTopProfiles(StatType.KILLS, "");
                    break;

                case "deaths":
                    entries = plugin.getLeaderboardManager().getCache().getTopProfiles(StatType.DEATHS, "");
                    break;
                case "level":
                    entries = plugin.getLeaderboardManager().getCache().getTopProfiles(StatType.LEVEL, "");
                    break;
                case "xp":
                    entries = plugin.getLeaderboardManager().getCache().getTopProfiles(StatType.EXPERIENCE, "");
                    break;
                case "coins":
                    entries = plugin.getLeaderboardManager().getCache().getTopProfiles(StatType.COINS, "");
                    break;

                default:
                    return "";
            }

            if (position < 0 || position >= entries.size()) {
                return "";
            }

            Profile entry = entries.get(position);

            if (field.equalsIgnoreCase("name")) {
                return entry.getLastKnownName();
            }

            if (field.equalsIgnoreCase("value")) {
                switch (category) {
                    case "wins":
                        return String.valueOf(entry.getWins());

                    case "kills":
                        return String.valueOf(entry.getKills());

                    case "deaths":
                        return String.valueOf(entry.getDeaths());
                }
            }

            return "";

        }
        return null;
    }
}
