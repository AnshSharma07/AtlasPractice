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

import java.util.HashSet;
import java.util.Set;

public abstract class Arena {

    private final String id;
    private final String displayName;
    private final ArenaType type;
    private final Set<String> breakableBlocks = new HashSet<>();
    private String worldName;
    private String templateWorld;
    private String slimeWorldName;
    private ArenaMode mode = ArenaMode.NORMAL;
    private ArenaState state;
    private Location spawnRed;
    private Location spawnBlue;
    private Location bedRed;
    private Location bedBlue;

    // neW
    private Location redWool;
    private Location blueWool;

    private final Set<String> redGoalBlocks = new HashSet<>();
    private final Set<String> blueGoalBlocks = new HashSet<>();

    private Location minimumBoundary;
    private Location maximumBoundary;
    protected Arena(String id, String displayName, ArenaType type) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.state = ArenaState.DISABLED;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ArenaType getType() {
        return type;
    }
    public ArenaMode getMode() {
        return mode;
    }

    public void setMode(ArenaMode mode) {
        this.mode = mode;
    }
    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) {
        this.worldName = normalizeWorldName(worldName);
        if (this.slimeWorldName == null || this.slimeWorldName.isBlank()) this.slimeWorldName = this.worldName;
        if (this.templateWorld == null || this.templateWorld.isBlank()) this.templateWorld = this.worldName;
    }
    public String getTemplateWorld() { return templateWorld; }
    public void setTemplateWorld(String templateWorld) { this.templateWorld = normalizeWorldName(templateWorld); }
    public String getSlimeWorldName() { return slimeWorldName == null || slimeWorldName.isBlank() ? worldName : slimeWorldName; }
    public void setSlimeWorldName(String slimeWorldName) { this.slimeWorldName = normalizeWorldName(slimeWorldName); }
    public org.bukkit.World getWorld() { return worldName == null ? null : org.bukkit.Bukkit.getWorld(worldName); }
    private String normalizeWorldName(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public Set<String> getBreakableBlocks() {
        return breakableBlocks;
    }

    public Location getSpawnRed() {
        return spawnRed;
    }

    public void setSpawnRed(Location spawnRed) {
        this.spawnRed = spawnRed;
    }

    public Location getSpawnBlue() {
        return spawnBlue;
    }

    public void setSpawnBlue(Location spawnBlue) {
        this.spawnBlue = spawnBlue;
    }

    public Location getBedRed() {
        return bedRed;
    }

    public void setBedRed(Location bedRed) {
        this.bedRed = bedRed;
    }

    public Location getBedBlue() {
        return bedBlue;
    }

    public void setBedBlue(Location bedBlue) {
        this.bedBlue = bedBlue;
    }

    public Location getRedWool() {
        return redWool;
    }

    public void setRedWool(Location redWool) {
        this.redWool = redWool;
    }

    public Location getBlueWool() {
        return blueWool;
    }

    public void setBlueWool(Location blueWool) {
        this.blueWool = blueWool;
    }

    public Set<String> getRedGoalBlocks() {
        return redGoalBlocks;
    }

    public Set<String> getBlueGoalBlocks() {
        return blueGoalBlocks;
    }

    public Location getMinimumBoundary() {
        return minimumBoundary;
    }

    public void setMinimumBoundary(Location minimumBoundary) {
        this.minimumBoundary = minimumBoundary;
    }

    public Location getMaximumBoundary() {
        return maximumBoundary;
    }

    public void setMaximumBoundary(Location maximumBoundary) {
        this.maximumBoundary = maximumBoundary;
    }

    public boolean isAvailable() {
        return state == ArenaState.FREE;
    }

    public boolean isDisabled() {
        return state == ArenaState.DISABLED;
    }

    public abstract void cleanAndResetWorld();
}