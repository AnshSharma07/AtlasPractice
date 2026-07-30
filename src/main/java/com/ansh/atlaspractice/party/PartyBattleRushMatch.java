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

package com.ansh.atlaspractice.party;

import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.match.BattleRushMatch;
import com.ansh.atlaspractice.match.MatchTeam;

import java.util.List;

public final class PartyBattleRushMatch extends BattleRushMatch implements PartyManagedMatch {
    private final List<Party> parties;
    private final int requiredWins;

    public PartyBattleRushMatch(List<Party> parties, Kit kit, Arena arena, List<MatchTeam> teams, int requiredWins) {
        super(kit, arena, teams, false);
        this.parties = parties;
        this.requiredWins = Math.max(1, requiredWins);
    }

    public List<Party> getParties() { return parties; }
    public int getRequiredWins() { return requiredWins; }
    public int getCurrentRound() { return 1; }
    public int getTeamOneWins() { return getTeams().isEmpty() ? 0 : getScore(getTeams().get(0)); }
    public int getTeamTwoWins() { return getTeams().size() < 2 ? 0 : getScore(getTeams().get(1)); }
}
