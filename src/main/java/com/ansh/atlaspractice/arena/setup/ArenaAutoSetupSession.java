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

package com.ansh.atlaspractice.arena.setup;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaManager;
import com.ansh.atlaspractice.arena.ArenaState;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.ansh.atlaspractice.arena.ArenaRepository;
public class ArenaAutoSetupSession {

    private final Player player;
    private final Arena arena;
    private final ArenaManager arenaManager;
    private final AtlasPracticePlugin plugin;
    private boolean active = true;
    private final int buildLimit;
    private ArenaTemplateResult result;
    private final ArenaRepository arenaRepository;
    public ArenaAutoSetupSession(Player player, Arena arena, AtlasPracticePlugin plugin, ArenaManager arenaManager) {
        this.player = player;
        this.arena = arena;
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.arenaRepository = plugin.getArenaRepository();
        int limit = plugin.getConfig().getInt("arena-auto-setup.build-limit", 15);

        if (limit < 10 || limit > 25) {
            plugin.getLogger().warning("Invalid arena-auto-setup.build-limit (" + limit + "). Using default 15.");
            limit = 15;
        }

        this.buildLimit = limit;
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        player.sendMessage(ChatColor.YELLOW + "Scanning... (This may take a moment)");

        // Starts scheduler
        new ArenaTemplateScanner(this, player.getLocation(), this::handleScanResult).runTaskTimer(plugin, 0L, 1L);
    }

    private void handleScanResult(ArenaTemplateResult scanResult) {
        if (!active) return;

        if (!scanResult.isSuccess()) {
            player.sendMessage(ChatColor.RED + "====================================");
            player.sendMessage(ChatColor.RED + "Scan Failed!");
            player.sendMessage(ChatColor.RED + "Reason: " + ChatColor.WHITE + scanResult.getError());
            player.sendMessage(ChatColor.RED + "====================================");
            active = false;
            return;
        }

        this.result = scanResult;
        player.sendMessage(ChatColor.GREEN + "Arena Found! Beginning auto configuration...");
        executeSequence();
    }
    private void scanBedDefense(Arena arena, Location center) {
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Material type = loc.getBlock().getType();

                    if (type != Material.ENDER_STONE && type != Material.WOOD) {
                        continue;
                    }

                    String key = loc.getWorld().getName()
                            + ":" + loc.getBlockX()
                            + ":" + loc.getBlockY()
                            + ":" + loc.getBlockZ();

                    arena.getBreakableBlocks().add(key);
                }
            }
        }
    }
    private void executeSequence() {
        Location redWoolLoc = result.getRedWool().getLocation().add(0.5, 1, 0.5);
        Location blueWoolLoc = result.getBlueWool().getLocation().add(0.5, 1, 0.5);

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (!active) {
                    this.cancel();
                    return;
                }

                try {
                    switch (step) {
                        case 0:
                            player.sendMessage(ChatColor.YELLOW + "Setting Pos1...");
                            arena.setMinimumBoundary(ArenaGeometryUtil.calculatePos1(redWoolLoc, blueWoolLoc, buildLimit));
                            break;

                        case 1:
                            player.sendMessage(ChatColor.YELLOW + "Setting Pos2...");
                            arena.setMaximumBoundary(ArenaGeometryUtil.calculatePos2(redWoolLoc, blueWoolLoc, buildLimit));
                            break;

                        case 2:
                            player.sendMessage(ChatColor.YELLOW + "Setting Red Spawn...");
                            Location redSpawn = ArenaGeometryUtil.faceLocation(redWoolLoc, blueWoolLoc);
                            arena.setSpawnRed(redSpawn);
                            break;

                        case 3:
                            player.sendMessage(ChatColor.YELLOW + "Setting Blue Spawn...");
                            Location blueSpawn = ArenaGeometryUtil.faceLocation(blueWoolLoc, redWoolLoc);
                            arena.setSpawnBlue(blueSpawn);
                            break;

                        case 4:
                            player.sendMessage(ChatColor.YELLOW + "Selecting Red Bed...");
                            arena.setBedRed(result.getRedBed().getLocation());
                            break;

                        case 5:
                            player.sendMessage(ChatColor.YELLOW + "Selecting Blue Bed...");
                            arena.setBedBlue(result.getBlueBed().getLocation());
                            arena.setRedWool(result.getRedWool().getLocation());
                            arena.setBlueWool(result.getBlueWool().getLocation());
                            break;

                        case 6:
                            player.sendMessage(ChatColor.YELLOW + "Selecting Breakable Blocks...");

                            arena.getBreakableBlocks().clear();

                            scanBedDefense(arena, arena.getBedRed());
                            scanBedDefense(arena, arena.getBedBlue());

                            break;

                        case 7:
                            player.sendMessage(ChatColor.YELLOW + "Saving...");
                            arenaRepository.saveArena(arena);
                            break;

                        case 8:
                            player.sendMessage(ChatColor.YELLOW + "Enabling...");
                            arena.setState(ArenaState.FREE);
                            arenaRepository.saveArena(arena);
                            break;

                        case 9:
                            player.sendMessage(ChatColor.GREEN + "====================================");
                            player.sendMessage(ChatColor.GREEN + "Arena Auto Setup Complete!");
                            player.sendMessage(ChatColor.GREEN + "Arena Name: " + ChatColor.WHITE + arena.getId());
                            player.sendMessage(ChatColor.GREEN + "Now assign this arena to a kit.");
                            player.sendMessage(ChatColor.GREEN + "====================================");
                            active = false;
                            this.cancel();
                            return;
                    }

                    step++;

                } catch (Exception ex) {
                    player.sendMessage(ChatColor.RED + "An error occurred during step " + step + ": " + ex.getMessage());
                    active = false;
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 10L); // 10 ticks wait in each step for smooth setting  up
    }
}