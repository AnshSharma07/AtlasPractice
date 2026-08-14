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

package com.ansh.atlaspractice.replay;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class ReplayMetadata {

    private final String replayId;
    private final UUID replayUuid;
    private final UUID matchId;
    private final String arena;
    private final String kit;
    private final List<String> players;
    private final Instant date;
    private final String matchType;

    private String winner;
    private String loser;
    private long durationMillis;

    public ReplayMetadata(
            String replayId,
            UUID replayUuid,
            UUID matchId,
            String arena,
            String kit,
            List<String> players,
            Instant date,
            String matchType
    ) {
        this.replayId = replayId;
        this.replayUuid = replayUuid;
        this.matchId = matchId;
        this.arena = arena;
        this.kit = kit;
        this.players = Collections.unmodifiableList(new ArrayList<>(players));
        this.date = date;
        this.matchType = matchType;

        winner = "Unknown";
        loser = "Unknown";
    }

    public String getReplayId() {
        return replayId;
    }

    public UUID getReplayUuid() {
        return replayUuid;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public String getArena() {
        return arena;
    }

    public String getKit() {
        return kit;
    }

    public List<String> getPlayers() {
        return players;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner == null ? "Unknown" : winner;
    }

    public String getLoser() {
        return loser;
    }

    public void setLoser(String loser) {
        this.loser = loser == null ? "Unknown" : loser;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = Math.max(0L, durationMillis);
    }

    public Instant getDate() {
        return date;
    }

    public String getMatchType() {
        return matchType;
    }
}