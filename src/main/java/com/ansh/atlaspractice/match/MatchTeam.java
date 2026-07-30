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

package com.ansh.atlaspractice.match;

import com.ansh.atlaspractice.team.TeamColor;

import java.util.List;
import java.util.UUID;

public final class MatchTeam {

    private final UUID teamId = UUID.randomUUID();
    private final String leaderName;
    private final List<MatchPlayer> players;

    private TeamColor teamColor;

    public MatchTeam(List<MatchPlayer> players, TeamColor teamColor) {
        this.players = players;
        this.teamColor = teamColor;

        if (players != null && !players.isEmpty()) {
            this.leaderName = players.get(0).getUsername();
        } else {
            this.leaderName = "Unknown";
        }
    }

    public UUID getTeamId() {
        return teamId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public List<MatchPlayer> getPlayers() {
        return players;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(TeamColor teamColor) {
        this.teamColor = teamColor;
    }

    public static final class MatchPlayer {

        private final UUID uuid;
        private final String username;

        public MatchPlayer(UUID uuid, String username) {
            this.uuid = uuid;
            this.username = username;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getUsername() {
            return username;
        }
    }
}