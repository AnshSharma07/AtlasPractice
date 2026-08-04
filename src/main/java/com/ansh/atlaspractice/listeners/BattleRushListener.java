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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.match.BattleRushMatch;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

public final class BattleRushListener implements Listener {

    private final AtlasPracticePlugin plugin;

    public BattleRushListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    // goal detect

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        //  only process when the player actually moves to a new block
        if (event.getTo() == null
                || event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
        if (matchOpt.isEmpty() || !(matchOpt.get() instanceof BattleRushMatch)) return;

        BattleRushMatch match = (BattleRushMatch) matchOpt.get();
        if (match.getState() != Match.MatchState.FIGHTING) return;

        String key = blockKey(event.getTo());

        MatchTeam team = match.getTeam(player);
        if (team == null) return;

        boolean redTeam = match.isRedTeam(team);

        // Player walked into their ownn goal â€“ tp them back
        if ((redTeam  && match.getArena().getRedGoalBlocks().contains(key))
         || (!redTeam && match.getArena().getBlueGoalBlocks().contains(key))) {
            player.sendMessage("§cYou can't score in your own goal.");
            match.teleportToSpawn(player);
            return;
        }

        // real goal from opp team
        if ((redTeam  && match.getArena().getBlueGoalBlocks().contains(key))
         || (!redTeam && match.getArena().getRedGoalBlocks().contains(key))) {
            match.handleGoalScore(team);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
        if (matchOpt.isEmpty() || !(matchOpt.get() instanceof BattleRushMatch)) return;

        BattleRushMatch match = (BattleRushMatch) matchOpt.get();

        if (match.getState() != Match.MatchState.FIGHTING) {
            event.setCancelled(true);
            return;
        }

        // Only wool is allowed
        if (event.getBlockPlaced().getType() != Material.WOOL) {
            event.setCancelled(true);
            player.sendMessage("§cOnly §ewool §ccan be placed in BattleRush.");
            return;
        }

        // arena boundaries
        Location placed = event.getBlockPlaced().getLocation();
        Location min = match.getArena().getMinimumBoundary();
        Location max = match.getArena().getMaximumBoundary();

        if (min != null && max != null && !isInsideBounds(placed, min, max)) {
            event.setCancelled(true);
            player.sendMessage("§cYou can't place blocks outside the arena.");
            return;
        }
        String key = blockKey(placed);
        match.trackWoolBlock(key, player.getUniqueId());
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
        if (matchOpt.isEmpty() || !(matchOpt.get() instanceof BattleRushMatch)) return;

        BattleRushMatch match = (BattleRushMatch) matchOpt.get();

        if (match.getState() != Match.MatchState.FIGHTING) {
            event.setCancelled(true);
            return;
        }

        String key = blockKey(event.getBlock().getLocation());

        if (match.isTrackedWool(key)) { event.getBlock().setType(Material.AIR);

            match.onWoolBrokenByPlayer(key, player);
            return;
        }  event.setCancelled(true);
    }
    private String blockKey(Location loc) {
        return loc.getWorld().getName()
                + ":" + loc.getBlockX()
                + ":" + loc.getBlockY()
                + ":" + loc.getBlockZ();
    }

    private boolean isInsideBounds(Location loc, Location min, Location max) {
        if (!loc.getWorld().equals(min.getWorld())) return false;
        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());
        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX
            && loc.getBlockY() >= minY && loc.getBlockY() <= maxY
            && loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
    }
}
