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

package com.ansh.atlaspractice.arena.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ArenaTemplateScanner extends BukkitRunnable {

    private final ArenaAutoSetupSession session;
    private final Location center;
    private final World world;
    private final Consumer<ArenaTemplateResult> callback;
    
    private final int radius = 50;
    private final int radiusSq = 2500;
    private final int blocksPerTick = 15000;
    
    private int x = -50;
    private int y = -50;
    private int z = -50;

    private final List<Block> beds = new ArrayList<>();
    private Block redWool = null;
    private Block blueWool = null;
    private int woodPlanks = 0;
    private int endStone = 0;

    public ArenaTemplateScanner(ArenaAutoSetupSession session, Location center, Consumer<ArenaTemplateResult> callback) {
        this.session = session;
        this.center = center;
        this.world = center.getWorld();
        this.callback = callback;
    }

    @Override
    public void run() {
        if (!session.isActive()) {
            this.cancel();
            return;
        }

        int processed = 0;
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        while (processed < blocksPerTick) {
            if (x * x + y * y + z * z <= radiusSq) {
                Block block = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                Material type = block.getType();
                
                if (type == Material.BED_BLOCK) {
                    // Only count the head of the bed to avoid double counting, uff 2 block bed
                    if ((block.getData() & 0x8) == 0x8) { 
                        beds.add(block);
                    }
                } else if (type == Material.WOOL) {
                    byte data = block.getData();
                    if (data == 14 && redWool == null) {
                        redWool = block;
                    } else if (data == 11 && blueWool == null) {
                        blueWool = block;
                    }
                } else if (type == Material.WOOD) {
                    woodPlanks++;
                } else if (type == Material.ENDER_STONE) {
                    endStone++;
                }
            }

            processed++;
            z++;
            if (z > radius) {
                z = -radius;
                y++;
                if (y > radius) {
                    y = -radius;
                    x++;
                    if (x > radius) {
                        this.cancel();
                        finalizeScan();
                        return;
                    }
                }
            }
        }
    }

    private void finalizeScan() {
        ArenaTemplateResult result = new ArenaTemplateResult();
        
        if (redWool == null) {
            result.setError("Red wool missing.");
            callback.accept(result);
            return;
        }
        if (blueWool == null) {
            result.setError("Blue wool missing.");
            callback.accept(result);
            return;
        }
        if (beds.size() < 2) {
            result.setError("Only " + beds.size() + " bed found.");
            callback.accept(result);
            return;
        }
        if (beds.size() > 2) {
            result.setError("Too many beds found (" + beds.size() + "). Please ensure exactly 2 exist.");
            callback.accept(result);
            return;
        }
        
        double woolDistance = redWool.getLocation().distance(blueWool.getLocation());
        if (woolDistance < 15 || woolDistance > 80) {
            result.setError("Red and Blue wool distance must be between 15 and 80 blocks (Found: " + (int)woolDistance + ").");
            callback.accept(result);
            return;
        }

        // Determine which bed is which
        Block bed1 = beds.get(0);
        Block bed2 = beds.get(1);

        double b1RedDist = bed1.getLocation().distanceSquared(redWool.getLocation());
        double b2RedDist = bed2.getLocation().distanceSquared(redWool.getLocation());

        Block redBed = b1RedDist < b2RedDist ? bed1 : bed2;
        Block blueBed = redBed == bed1 ? bed2 : bed1;

        // Verify beds clearly belong to a team
        if (redBed.getLocation().distanceSquared(redWool.getLocation()) > blueBed.getLocation().distanceSquared(redWool.getLocation())) {
            result.setError("Beds and spawns too inaccurate. Could not accurately assign beds to teams.");
            callback.accept(result);
            return;
        }

        result.setSuccess(true);
        result.setRedWool(redWool);
        result.setBlueWool(blueWool);
        result.setRedBed(redBed);
        result.setBlueBed(blueBed);
        result.setEndStone(endStone);
        result.setWoodPlanks(woodPlanks);
        
        callback.accept(result);
    }
}