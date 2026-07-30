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

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.Location;
import org.bukkit.World;

public final class RuntimeArena extends Arena {

    private final String sourceArenaId;

    public RuntimeArena(String id, Arena sourceArena, String runtimeWorldName) {
        super(id, sourceArena.getDisplayName(), sourceArena.getType());
        this.sourceArenaId = sourceArena.getId();
        this.setMode(sourceArena.getMode());
        this.setWorldName(runtimeWorldName);
        this.setTemplateWorld(sourceArena.getTemplateWorld());
        this.setSlimeWorldName(sourceArena.getSlimeWorldName());
        this.getBreakableBlocks().addAll(sourceArena.getBreakableBlocks());
        this.getRedGoalBlocks().addAll(sourceArena.getRedGoalBlocks());
        this.getBlueGoalBlocks().addAll(sourceArena.getBlueGoalBlocks());
        copyLocationsFrom(sourceArena, null);
        this.setState(ArenaState.ALLOCATED);
    }

    public String getSourceArenaId() {
        return sourceArenaId;
    }

    public void bindToWorld(World world) {
        copyLocationsFrom(this, world);
    }

    private void copyLocationsFrom(Arena source, World world) {
        this.setSpawnRed(copy(source.getSpawnRed(), world));
        this.setSpawnBlue(copy(source.getSpawnBlue(), world));
        this.setBedRed(copy(source.getBedRed(), world));
        this.setBedBlue(copy(source.getBedBlue(), world));
        this.setRedWool(copy(source.getRedWool(), world));
        this.setBlueWool(copy(source.getBlueWool(), world));
        this.setMinimumBoundary(copy(source.getMinimumBoundary(), world));
        this.setMaximumBoundary(copy(source.getMaximumBoundary(), world));
    }

    private Location copy(Location location, World world) {
        if (location == null) return null;
        return new Location(
                world == null ? location.getWorld() : world,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    @Override
    public void cleanAndResetWorld() {
        this.setState(ArenaState.RESETTING);
        AtlasPracticePlugin plugin = AtlasPracticePlugin.getInstance();
        plugin.getLogger().info("[AtlasPractice] Destroying overflow runtime " + getWorldName());
        plugin.getWorldService().unloadWorld(getWorldName(), false)
                .thenCompose(ignored -> plugin.getWorldService().deleteTemporaryWorld(getWorldName()))
                .whenComplete((ignored, throwable) -> {
                    plugin.getArenaManager().unregisterArena(this);
                    if (throwable == null) {
                        plugin.getLogger().info("[AtlasPractice] Runtime destroyed: " + getWorldName());
                    } else {
                        plugin.getLogger().warning("[AtlasPractice] Failed to destroy runtime " + getWorldName() + ": " + throwable.getMessage());
                    }
                });
    }
}
