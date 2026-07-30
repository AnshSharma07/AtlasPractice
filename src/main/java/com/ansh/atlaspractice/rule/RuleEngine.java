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

package com.ansh.atlaspractice.rule;

import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import com.ansh.atlaspractice.profile.ProfileState;
import java.util.Optional;


@RequiredArgsConstructor
public final class RuleEngine {

    private final ProfileManager profileManager;

    
    public boolean checkPlayerAction(Player player, MatchRule rule) {
        Profile profile = this.profileManager.getProfile(player.getUniqueId());
        if (profile == null) return false;

        // Fast-path evaluation: If the player isn't in a match, defer to their current ProfileState rules
        if (profile.getState() != ProfileState.MATCH || profile.getActiveMatchId() == null) {
            return !rule.isDefaultState(); // Deny environmental changes across standard lobby areas
        }

        // Performance Note: This hooks into the MatchManager to fetch the active RuleSet context.
        // If no custom rules are registered for the match, it falls back safely to baseline system defaults.
        return fetchActiveRuleSetForPlayer(profile).map(rules -> rules.allows(rule)).orElse(false);
    }

    
    private Optional<RuleSet> fetchActiveRuleSetForPlayer(Profile profile) {
        // This structural layout will tie directly into the MatchManager instance in upcoming blocks
        // to query: MatchManager.getMatch(profile.getActiveMatchId()).getKit().getRuleSet()
        return Optional.empty();
    }
}
