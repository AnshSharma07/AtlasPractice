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

package com.ansh.atlaspractice.party;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchTeam;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class PartyMatch extends Match {

    private final Party party;
    private final Map<MatchTeam, Integer> activeTeamAliveCounts = new ConcurrentHashMap<>();
    private final Set<UUID> eliminatedPlayers = ConcurrentHashMap.newKeySet();

    public PartyMatch(Party party, Kit kit, Arena arena, List<MatchTeam> teams) {
        super(kit, arena, teams);
        this.party = party;

        teams.forEach(team -> this.activeTeamAliveCounts.put(team, team.getPlayers().size()));
    }
    public boolean isEliminated(Player player) {
        return eliminatedPlayers.contains(player.getUniqueId());
    }
    public Party getParty() {
        return party;
    }

    public void handlePlayerQuit(Player quitter) {
        handleDeath(quitter);
    }

    public void handleDeath(Player victim) {
        if (!eliminatedPlayers.add(victim.getUniqueId())) {
            return;
        }

        MatchTeam victimTeam = getTeams().stream()
                .filter(team -> team.getPlayers().stream().anyMatch(mp -> mp.getUuid().equals(victim.getUniqueId())))
                .findFirst()
                .orElse(null);

        if (victimTeam == null) {
            return;
        }

        int aliveRemaining = activeTeamAliveCounts.getOrDefault(victimTeam, 0) - 1;
        activeTeamAliveCounts.put(victimTeam, aliveRemaining);

        broadcastMessage("§c" + victim.getName() + " died! §e" + victimTeam.getLeaderName() + "'s Team has " + aliveRemaining + " alive left.");

        int activeTeams = 0;
        MatchTeam winningTeam = null;

        for (Map.Entry<MatchTeam, Integer> entry : activeTeamAliveCounts.entrySet()) {
            if (entry.getValue() > 0) {
                activeTeams++;
                winningTeam = entry.getKey();
            }
        }

        if (activeTeams <= 1) {
            AtlasPracticePlugin.getInstance().getMatchManager().triggerMatchEndSequence(this, winningTeam);
        }
    }
}
