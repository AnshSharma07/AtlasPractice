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
import com.ansh.atlaspractice.arena.SharedArena;
import com.ansh.atlaspractice.commands.ArenaAdminCommand;
import com.ansh.atlaspractice.match.BattleRushMatch;
import com.ansh.atlaspractice.match.BridgeMatch;
import com.ansh.atlaspractice.match.Match;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Bed;
import com.ansh.atlaspractice.match.MatchTeam;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import java.util.Optional;

public final class MatchBlockListener implements Listener {

    private final AtlasPracticePlugin plugin;

    private String blockKey(Location loc) {
        return loc.getWorld().getName()
                + ":" + loc.getBlockX()
                + ":" + loc.getBlockY()
                + ":" + loc.getBlockZ();
    }
    private boolean isProtectedSpawnColumn(Location placed, Location wool) {

        if (wool == null)
            return false;

        return placed.getBlockX() == wool.getBlockX()
                && placed.getBlockZ() == wool.getBlockZ()
                && (
                placed.getBlockY() == wool.getBlockY() + 1
                        || placed.getBlockY() == wool.getBlockY() + 2
        );
    }
    private boolean isInside(Location loc, Location p1, Location p2) {

        if (loc == null || p1 == null || p2 == null) {
            return false;
        }

        if (!loc.getWorld().equals(p1.getWorld())) {
            return false;
        }

        int minX = Math.min(p1.getBlockX(), p2.getBlockX());
        int maxX = Math.max(p1.getBlockX(), p2.getBlockX());

        int minY = Math.min(p1.getBlockY(), p2.getBlockY());
        int maxY = Math.max(p1.getBlockY(), p2.getBlockY());

        int minZ = Math.min(p1.getBlockZ(), p2.getBlockZ());
        int maxZ = Math.max(p1.getBlockZ(), p2.getBlockZ());

        return loc.getBlockX() >= minX
                && loc.getBlockX() <= maxX
                && loc.getBlockY() >= minY
                && loc.getBlockY() <= maxY
                && loc.getBlockZ() >= minZ
                && loc.getBlockZ() <= maxZ;
    }
    private void trackEntireBed(
            SharedArena arena,
            Block bedBlock
    ) {

        arena.getBlockTracker().trackBreak(
                bedBlock.getLocation(),
                bedBlock.getType(),
                bedBlock.getData()
        );

        Bed bedData = (Bed) bedBlock.getState().getData();

        Block otherHalf;

        if (bedData.isHeadOfBed()) {
            otherHalf = bedBlock.getRelative(
                    bedData.getFacing().getOppositeFace()
            );
        } else {
            otherHalf = bedBlock.getRelative(
                    bedData.getFacing()
            );
        }

        if (otherHalf.getType() == Material.BED_BLOCK) {

            arena.getBlockTracker().trackBreak(
                    otherHalf.getLocation(),
                    otherHalf.getType(),
                    otherHalf.getData()
            );
        }
    }


    public MatchBlockListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        if (player.hasPermission("atlaspractice.admin")
                && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Optional<Match> matchOpt =
                plugin.getMatchManager()
                        .getMatchByPlayer(player.getUniqueId());

        if (matchOpt.isEmpty()) {
            event.setCancelled(true);
            return;
        }
        Match match = matchOpt.get();

        if (match.getState() != Match.MatchState.FIGHTING) {
            event.setCancelled(true);
            return;
        }
        // BattleRush manages its own wool placement â€” handled by BattleRushListener
        if (match instanceof BattleRushMatch) {
            return;
        }
        Location blockLoc = event.getBlockPlaced().getLocation();

// Protect the exact respawn blocks
        Location redSpawn = match.getArena().getSpawnRed();
        Location blueSpawn = match.getArena().getSpawnBlue();

        if (redSpawn != null) {
            Location red = redSpawn.getBlock().getLocation();

            if (blockLoc.equals(red) || blockLoc.equals(red.clone().add(0, 1, 0))) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot place blocks here.");
                return;
            }
        }

        if (blueSpawn != null) {
            Location blue = blueSpawn.getBlock().getLocation();

            if (blockLoc.equals(blue) || blockLoc.equals(blue.clone().add(0, 1, 0))) {
                event.setCancelled(true);
                player.sendMessage("§cYou cannot place blocks here.");
                return;
            }
        }

        if (match instanceof BridgeMatch bridgeMatch) {

            MatchTeam team = bridgeMatch.getTeam(player);

        }
        if (isProtectedSpawnColumn(blockLoc, match.getArena().getRedWool())
                || isProtectedSpawnColumn(blockLoc, match.getArena().getBlueWool())) {

            event.setCancelled(true);
            player.sendMessage("§cYou cannot place blocks above the spawn.");
            return;
        }
        Location min = match.getArena().getMinimumBoundary();
        Location max = match.getArena().getMaximumBoundary();

        if (min != null && max != null) {

            int minX = Math.min(min.getBlockX(), max.getBlockX());
            int maxX = Math.max(min.getBlockX(), max.getBlockX());

            int minY = Math.min(min.getBlockY(), max.getBlockY());
            int maxY = Math.max(min.getBlockY(), max.getBlockY());

            int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
            int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

            int x = blockLoc.getBlockX();
            int y = blockLoc.getBlockY();
            int z = blockLoc.getBlockZ();

            if (x < minX || x > maxX
                    || y < minY || y > maxY
                    || z < minZ || z > maxZ) {

                event.setCancelled(true);

                player.sendMessage("§cYou can't place blocks here.");

                return;
            }
        }
        match.getPlacedBlocks().add(
                blockKey(event.getBlockPlaced().getLocation())
        );
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        if (player.hasPermission("atlaspractice.admin")
                && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Optional<Match> matchOpt =
                plugin.getMatchManager()
                        .getMatchByPlayer(player.getUniqueId());

        if (matchOpt.isEmpty()) {
            event.setCancelled(true);
            return;
        }
        Match match = matchOpt.get();

        if (match.getState() != Match.MatchState.FIGHTING) {
            event.setCancelled(true);
            return;
        }
        // BattleRush manages its own wool-only breaking â€” handled by BattleRushListener
        if (match instanceof BattleRushMatch) {
            return;
        }

        Location blockLoc = event.getBlock().getLocation();

        if (event.getBlock().getType() == Material.BED_BLOCK
                && match.getTeams().size() >= 2) {

            boolean redPlayer =
                    match.getTeams().get(0)
                            .getPlayers()
                            .stream()
                            .anyMatch(mp ->
                                    mp.getUuid().equals(player.getUniqueId()));

            if (match.getArena().getBedRed() != null
                    && match.isRedBedAlive()
                    && match.getArena().getBedRed().distanceSquared(blockLoc) <= 4) {

                if (redPlayer) {
                    player.sendMessage("§cYou cannot break your own bed.");
                    event.setCancelled(true);
                    return;
                }

                match.setRedBedAlive(false);

                if (match.getArena() instanceof SharedArena) {

                    SharedArena sharedArena =
                            (SharedArena) match.getArena();

                    trackEntireBed(
                            sharedArena,
                            event.getBlock()
                    );
                }

                event.setCancelled(true);
                Block bed = event.getBlock();

                Bed data = (Bed) bed.getState().getData();

                Block otherHalf;

                if (data.isHeadOfBed()) {
                    otherHalf = bed.getRelative(
                            data.getFacing().getOppositeFace()
                    );
                } else {
                    otherHalf = bed.getRelative(
                            data.getFacing()
                    );
                }

                bed.setType(Material.AIR);

                if (otherHalf.getType() == Material.BED_BLOCK) {
                    otherHalf.setType(Material.AIR);
                }

                match.broadcastMessage("§c§lRED BED DESTROYED!");

                for (MatchTeam team : match.getTeams()) {
                    team.getPlayers().forEach(mp -> {
                        Player p = org.bukkit.Bukkit.getPlayer(mp.getUuid());

                        if (p != null) {
                            p.sendTitle(
                                    "§c§lBED DESTROYED!",
                                    "§fRed Team's bed has been destroyed!"
                            );
                        }
                    });
                }

                for (MatchTeam team : match.getTeams()) {
                    team.getPlayers().forEach(mp -> {
                        Player p = org.bukkit.Bukkit.getPlayer(mp.getUuid());

                        if (p != null) {
                            p.playSound(
                                    p.getLocation(),
                                    Sound.ENDERDRAGON_GROWL,
                                    1.0F,
                                    1.0F
                            );
                        }
                    });
                }

                return;
            }
            if (match.getArena().getBedBlue() != null
                    && match.isBlueBedAlive()
                    && match.getArena().getBedBlue().distanceSquared(blockLoc) <= 4) {

                if (!redPlayer) {
                    player.sendMessage("§cYou cannot break your own bed.");
                    event.setCancelled(true);
                    return;
                }

                match.setBlueBedAlive(false);

                if (match.getArena() instanceof SharedArena) {

                    SharedArena sharedArena =
                            (SharedArena) match.getArena();

                    trackEntireBed(
                            sharedArena,
                            event.getBlock()
                    );
                }

                event.setCancelled(true);
                Block bed = event.getBlock();

                Bed data = (Bed) bed.getState().getData();

                Block otherHalf;

                if (data.isHeadOfBed()) {
                    otherHalf = bed.getRelative(
                            data.getFacing().getOppositeFace()
                    );
                } else {
                    otherHalf = bed.getRelative(
                            data.getFacing()
                    );
                }

                bed.setType(Material.AIR);

                if (otherHalf.getType() == Material.BED_BLOCK) {
                    otherHalf.setType(Material.AIR);
                }

                match.broadcastMessage("§9§lBLUE BED DESTROYED!");

                for (MatchTeam team : match.getTeams()) {
                    team.getPlayers().forEach(mp -> {
                        Player p = org.bukkit.Bukkit.getPlayer(mp.getUuid());

                        if (p != null) {
                            p.sendTitle(
                                    "§9§lBED DESTROYED!",
                                    "§fBlue Team's bed has been destroyed!"
                            );
                        }
                    });
                }

                for (MatchTeam team : match.getTeams()) {
                    team.getPlayers().forEach(mp -> {
                        Player p = org.bukkit.Bukkit.getPlayer(mp.getUuid());

                        if (p != null) {
                            p.playSound(
                                    p.getLocation(),
                                    Sound.ENDERDRAGON_GROWL,
                                    1.0F,
                                    1.0F
                            );
                        }
                    });
                }

                return;
            }
        }
        String key = blockKey(event.getBlock().getLocation());
        if (match.getPlacedBlocks().contains(key)) {

            match.getPlacedBlocks().remove(key);
            event.setCancelled(false);
            return;
        }
        if (match.getArena().getBreakableBlocks().contains(key)) {

            if (match.getArena() instanceof SharedArena) {

                SharedArena sharedArena =
                        (SharedArena) match.getArena();

                sharedArena.getBlockTracker().trackBreak(
                        event.getBlock().getLocation(),
                        event.getBlock().getType(),
                        event.getBlock().getData()
                );
            }
            if (event.getBlock().getType() == Material.BED_BLOCK) {
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                return;
            }
            event.setCancelled(false);
            return;
        }
        event.setCancelled(true);
    }
}
