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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.match.DuelMatch;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchManager;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.party.PartyPvPMatch;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import com.ansh.atlaspractice.profile.ProfileState;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@RequiredArgsConstructor
public final class PlayerSumoListener implements Listener {

    private final ProfileManager profileManager;
    private final MatchManager matchManager;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();

        Profile profile = profileManager.getProfile(player.getUniqueId());

        if (profile == null || profile.getState() != ProfileState.MATCH) {
            return;
        }

        Match match = matchManager.getMatchByPlayer(player.getUniqueId()).orElse(null);

        if (match == null || match.getState() != Match.MatchState.FIGHTING) {
            return;
        }

        if (!match.getKit().getId().equalsIgnoreCase("SUMO")) {
            return;
        }

        Material type = player.getLocation().getBlock().getType();

        if (type != Material.WATER && type != Material.STATIONARY_WATER) {
            return;
        }

        if (match instanceof DuelMatch) {
            MatchTeam winningTeam = match.getTeams().stream()
                    .filter(team -> team.getPlayers().stream()
                            .noneMatch(mp -> mp.getUuid().equals(player.getUniqueId())))
                    .findFirst()
                    .orElse(null);

            ((DuelMatch) match).handleRoundWin(winningTeam);
        } else if (match instanceof PartyPvPMatch) {
            ((PartyPvPMatch) match).handleDeath(player);
        }
    }
}