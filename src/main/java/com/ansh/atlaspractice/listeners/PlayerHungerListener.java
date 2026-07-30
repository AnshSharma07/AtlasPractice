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

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.profile.Profile;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import com.ansh.atlaspractice.profile.ProfileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;


@RequiredArgsConstructor
public final class PlayerHungerListener implements Listener {

    private final AtlasPracticePlugin plugin;

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());

        if (profile == null) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            return;
        }

        if (profile.getState() != ProfileState.MATCH) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            return;
        }

        Match match = plugin.getMatchManager().getMatch(player);

        if (match == null) {
            return;
        }

        if (!match.getKit().isHungerLossEnabled()) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }
}
