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

package com.ansh.atlaspractice.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class BlockTracker {

    private final Map<String, BlockSnapshot> originalStates
            = new ConcurrentHashMap<>();

    private String key(Location loc) {
        return loc.getWorld().getName()
                + ":"
                + loc.getBlockX()
                + ":"
                + loc.getBlockY()
                + ":"
                + loc.getBlockZ();
    }
    public record BlockSnapshot(Location location, Material material, byte data) {}

    
    public void trackPlacement(Location location, Material material, byte data) {
        Location clone = location.clone();

        this.originalStates.putIfAbsent(
                key(clone),
                new BlockSnapshot(clone, material, data)
        );
    }

    
    public void trackBreak(Location location, Material material, byte data) {
        Location clone = location.clone();

        this.originalStates.putIfAbsent(
                key(clone),
                new BlockSnapshot(clone, material, data)
        );
    }

    
    public Collection<BlockSnapshot> compileReversionSnapshots() {
        return List.copyOf(this.originalStates.values());
    }

    
    public void clear() {
        this.originalStates.clear();
    }
}
