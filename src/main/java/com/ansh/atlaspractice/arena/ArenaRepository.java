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

package com.ansh.atlaspractice.arena;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.util.LocationUtil;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.Bukkit;
@RequiredArgsConstructor
public final class ArenaRepository {

    private final AtlasPracticePlugin plugin;
    public void loadArenas(ArenaManager manager) {
        File file = new File(this.plugin.getDataFolder(), "arenas.yml");
        if (!file.exists()) {
            this.plugin.saveResource("arenas.yml", false);
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("arenas")) return;

        for (String key : config.getConfigurationSection("arenas").getKeys(false)) {
            String path = "arenas." + key + ".";
            String displayName = config.getString(path + "display-name", key);
            ArenaType type;
            try {
                type = ArenaType.valueOf(config.getString(path + "type", "SHARED").toUpperCase());
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning("Skipping arena '" + key + "' because it has an invalid type.");
                continue;
            }

            Arena arena = switch (type) {
                case NORMAL, BEDFIGHT, FIREBALLFIGHT, BRIDGE, SHARED -> new SharedArena(key, displayName);
                case DYNAMIC, EVENT -> new DynamicArena(key, displayName);
            };

            String worldName = config.getString(path + "world-name");
            if (worldName == null || worldName.isBlank()) {
                String legacySpawn = config.getString(path + "spawn-red", config.getString(path + "spawn-1"));
                if (legacySpawn != null && legacySpawn.contains(":")) {
                    worldName = legacySpawn.split(":")[0];
                    this.plugin.getLogger().info("Migrated legacy arena '" + key + "' to world-name '" + worldName + "'.");
                }
            }
            arena.setWorldName(worldName);
            arena.setTemplateWorld(config.getString(path + "template-world", worldName));
            arena.setSlimeWorldName(config.getString(path + "slime-world-name", worldName));

            if (arena.getWorldName() == null) {
                this.plugin.getLogger().warning("Skipping arena '" + key + "' because no world-name is configured.");
                continue;
            }
            if (manager.isWorldAssigned(arena.getWorldName(), arena.getId())) {
                this.plugin.getLogger().warning("Skipping arena '" + key + "' because world '" + arena.getWorldName() + "' is already assigned.");
                continue;
            }
            String spawnRed = config.getString(path + "spawn-red", config.getString(path + "spawn-1"));
            String spawnBlue = config.getString(path + "spawn-blue", config.getString(path + "spawn-2"));
            String bedRed = config.getString(path + "bed-red");
            String bedBlue = config.getString(path + "bed-blue");
            String minBoundary = config.getString(path + "min-boundary");
            String maxBoundary = config.getString(path + "max-boundary");
            String redProtectedMin = config.getString(path + "red-protected-min");
            String redProtectedMax = config.getString(path + "red-protected-max");

            String blueProtectedMin = config.getString(path + "blue-protected-min");
            String blueProtectedMax = config.getString(path + "blue-protected-max");
            int voidDepth = config.getInt(path + "void-depth", 10);
            java.util.List<String> breakableBlocks = config.getStringList(path + "breakable-blocks");
            java.util.List<String> redGoalBlocks = config.getStringList(path + "red-goal-blocks");
            java.util.List<String> blueGoalBlocks = config.getStringList(path + "blue-goal-blocks");
            String stateString = config.getString(path + "state", "DISABLED");

            this.plugin.getWorldService().loadArenaWorld(arena).whenComplete((world, throwable) -> {
                if (throwable != null) {
                    this.plugin.getLogger().log(Level.SEVERE, "Skipping arena '" + key + "' because SWM could not load its world.", throwable);
                    return;
                }

                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    arena.setSpawnRed(LocationUtil.deserialize(spawnRed));
                    arena.setSpawnBlue(LocationUtil.deserialize(spawnBlue));
                    arena.setBedRed(LocationUtil.deserialize(bedRed));
                    arena.setBedBlue(LocationUtil.deserialize(bedBlue));
                    arena.setMinimumBoundary(LocationUtil.deserialize(minBoundary));
                    arena.setMaximumBoundary(LocationUtil.deserialize(maxBoundary));
                    String mode = config.getString(path + "mode", "NORMAL");

                    try {
                        arena.setMode(ArenaMode.valueOf(mode.toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                        arena.setMode(ArenaMode.NORMAL);
                    }
                    arena.getBreakableBlocks().addAll(breakableBlocks);
                    arena.getRedGoalBlocks().addAll(redGoalBlocks);
                    arena.getBlueGoalBlocks().addAll(blueGoalBlocks);
                    try {
                        arena.setState(ArenaState.valueOf(stateString));
                    } catch (IllegalArgumentException e) {
                        arena.setState(ArenaState.DISABLED);
                    }
                    manager.registerArena(arena);
                    this.plugin.getLogger().info("Loaded arena '" + arena.getId() + "' from template '" + arena.getTemplateWorld() + "' as runtime world '" + arena.getWorldName() + "'.");
                });
            });
        }
        this.plugin.getLogger().info("Queued arena runtime world loading.......");
    }
    public void deleteArena(String arenaId) {

        File file = new File(this.plugin.getDataFolder(), "arenas.yml");

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        config.set("arenas." + arenaId, null);

        try {
            config.save(file);
        } catch (IOException exception) {
            this.plugin.getLogger().log(
                    Level.SEVERE,
                    "Failed to delete arena " + arenaId,
                    exception
            );
        }
    }
    public void saveArena(Arena arena) {
        File file = new File(this.plugin.getDataFolder(), "arenas.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String path = "arenas." + arena.getId() + ".";
        config.set(path + "display-name", arena.getDisplayName());
        config.set(path + "type", arena.getType().name());
        config.set(path + "mode", arena.getMode().name());
        config.set(path + "world-name", arena.getWorldName());
        config.set(path + "template-world", arena.getTemplateWorld());
        config.set(path + "slime-world-name", arena.getSlimeWorldName());
        config.set(path + "spawn-red",
                LocationUtil.serialize(arena.getSpawnRed()));

        config.set(path + "spawn-blue",
                LocationUtil.serialize(arena.getSpawnBlue()));

        config.set(path + "bed-red",
                LocationUtil.serialize(arena.getBedRed()));

        config.set(path + "bed-blue",
                LocationUtil.serialize(arena.getBedBlue()));

        config.set(path + "min-boundary",
                LocationUtil.serialize(arena.getMinimumBoundary()));

        config.set(path + "max-boundary",
                LocationUtil.serialize(arena.getMaximumBoundary()));
        config.set(path + "state", arena.getState().name());
        config.set(path + "breakable-blocks",
                new ArrayList<>(arena.getBreakableBlocks()));
        config.set(path + "red-goal-blocks",
                new ArrayList<>(arena.getRedGoalBlocks()));
        config.set(path + "blue-goal-blocks",
                new ArrayList<>(arena.getBlueGoalBlocks()));

        try {
            config.save(file);
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to write structural layout updates to disk file for: " + arena.getId(), exception);
        }
    }
}
