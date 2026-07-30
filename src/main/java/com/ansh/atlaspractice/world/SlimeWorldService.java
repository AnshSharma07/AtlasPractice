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

package com.ansh.atlaspractice.world;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class SlimeWorldService implements WorldService {

    private final AtlasPracticePlugin plugin;
    private final Object slimePlugin;
    private final Object loader;
    private final Method loadWorld;
    private final Method generateWorld;
    private final Method cloneWorld;
    private final Method deleteWorld;
    private final Constructor<?> propertyMapConstructor;
    private final Set<String> activeRuntimeLoads = ConcurrentHashMap.newKeySet();

    public SlimeWorldService(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        Plugin swm = Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        if (swm == null) {
            throw new IllegalStateException("SlimeWorldManager is required for AtlasPractice arena worlds.");
        }

        try {
            Class<?> slimePluginClass = Class.forName("com.grinderwolf.swm.api.SlimePlugin");
            Class<?> slimeLoaderClass = Class.forName("com.grinderwolf.swm.api.loaders.SlimeLoader");
            Class<?> slimeWorldClass = Class.forName("com.grinderwolf.swm.api.world.SlimeWorld");
            Class<?> propertyMapClass = Class.forName("com.grinderwolf.swm.api.world.properties.SlimePropertyMap");

            if (!slimePluginClass.isInstance(swm)) {
                throw new IllegalStateException("Installed SlimeWorldManager does not expose the SWM API.");
            }

            this.slimePlugin = swm;
            Method getLoader = slimePluginClass.getMethod("getLoader", String.class);
            this.loader = getLoader.invoke(swm, plugin.getConfig().getString("swm.loader", "file"));
            if (this.loader == null) {
                throw new IllegalStateException("SWM loader not found: " + plugin.getConfig().getString("swm.loader", "file"));
            }

            this.propertyMapConstructor = propertyMapClass.getConstructor();
            this.loadWorld = slimePluginClass.getMethod("loadWorld", slimeLoaderClass, String.class, boolean.class, propertyMapClass);
            this.generateWorld = slimePluginClass.getMethod("generateWorld", slimeWorldClass);
            this.cloneWorld = slimeWorldClass.getMethod("clone", String.class);
            this.deleteWorld = slimeLoaderClass.getMethod("deleteWorld", String.class);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to bind SlimeWorldManager API.", exception);
        }
    }

    @Override
    public CompletableFuture<World> loadArenaWorld(Arena arena) {
        String runtimeWorld = arena == null ? null : arena.getWorldName();
        String templateWorld = arena == null ? null : arena.getTemplateWorld();
        plugin.getLogger().info(
                "[ATLAS-SWM] Runtime="+runtimeWorld+
                        " Template="+templateWorld);
        if (runtimeWorld == null || runtimeWorld.isBlank() || templateWorld == null || templateWorld.isBlank()) {
            return failedFuture(new IllegalArgumentException("Arena runtime world and template world names are required."));
        }
        if (!activeRuntimeLoads.add(runtimeWorld.toLowerCase())) {
            return failedFuture(new IllegalStateException("Runtime world '" + runtimeWorld + "' is already loading or resetting."));
        }

        long start = System.currentTimeMillis();
        plugin.getLogger().info("[AtlasPractice] Loading template " + templateWorld);
        return supplyAsyncStage("load template " + templateWorld, () -> loadSlimeWorld(templateWorld, true))
                .thenApply(template -> {
                    plugin.getLogger().info("[AtlasPractice] Generating runtime world " + runtimeWorld);
                    return clone(template, runtimeWorld);
                })
                .thenCompose(slimeWorld -> generate(slimeWorld, runtimeWorld))
                .whenComplete((world, throwable) -> {
                    activeRuntimeLoads.remove(runtimeWorld.toLowerCase());
                    if (throwable == null) {
                        plugin.getLogger().info("[AtlasPractice] Arena world ready in " + (System.currentTimeMillis() - start) + " ms");
                    } else {
                        logFailure("load arena runtime world " + runtimeWorld + " from template " + templateWorld, throwable);
                    }
                });
    }

    @Override
    public CompletableFuture<World> resetArena(Arena arena) {
        if (arena == null || arena.getWorldName() == null || arena.getTemplateWorld() == null) {
            return failedFuture(new IllegalArgumentException("Arena world and template names are required."));
        }
        String runtimeWorld = arena.getWorldName();
        plugin.getLogger().info("[AtlasPractice] Resetting arena " + arena.getId() + "...");
        arena.setState(ArenaState.RESETTING);
        return unloadWorld(runtimeWorld, false)
                .thenCompose(ignored -> deleteTemporaryWorld(runtimeWorld))
                .thenCompose(ignored -> loadArenaWorld(arena))
                .thenApply(world -> {
                    rebindArenaLocations(arena, world);
                    return world;
                })
                .whenComplete((world, throwable) -> {
                    if (throwable == null) {
                        arena.setState(ArenaState.FREE);
                        plugin.getLogger().info("[AtlasPractice] Reset completed for arena " + arena.getId() + ".");
                    } else {
                        arena.setState(ArenaState.DISABLED);
                        logFailure("reset arena " + arena.getId(), throwable);
                    }
                });
    }

    @Override
    public CompletableFuture<Void> unloadWorld(String worldName, boolean save) {
        if (worldName == null || worldName.isBlank()) return CompletableFuture.completedFuture(null);
        plugin.getLogger().info("[AtlasPractice] Deleting runtime world... unloading " + worldName);
        return supplySync(() -> {
            World world = Bukkit.getWorld(worldName);
            if (world != null && !Bukkit.unloadWorld(world, save)) {
                throw new IllegalStateException("Bukkit refused to unload world '" + worldName + "'.");
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> deleteTemporaryWorld(String worldName) {
        if (worldName == null || worldName.isBlank()) return CompletableFuture.completedFuture(null);
        plugin.getLogger().info("[AtlasPractice] Deleting runtime world " + worldName + " from SWM loader if present.");
        return CompletableFuture.runAsync(() -> {
            try {
                deleteWorld.invoke(loader, worldName);
            } catch (InvocationTargetException exception) {
                plugin.getLogger().fine("Runtime world '" + worldName + "' was not stored in the SWM loader: " + exception.getTargetException().getMessage());
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to delete temporary SWM world '" + worldName + "'.", exception);
            }
        });
    }

    private Object loadSlimeWorld(String name, boolean readOnly) {
        try {
            return loadWorld.invoke(slimePlugin, loader, name, readOnly, propertyMapConstructor.newInstance());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to load SWM world '" + name + "'.", unwrap(exception));
        }
    }

    private Object clone(Object slimeWorld, String targetName) {
        try {
            return cloneWorld.invoke(slimeWorld, targetName);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to clone SWM world to '" + targetName + "'.", unwrap(exception));
        }
    }

    private CompletableFuture<World> generate(Object slimeWorld, String bukkitWorldName) {
        return supplySync(() -> {
            try {
                generateWorld.invoke(slimePlugin, slimeWorld);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to generate SWM world: " + bukkitWorldName, unwrap(exception));
            }
            World world = Bukkit.getWorld(bukkitWorldName);
            plugin.getLogger().info(
                    "[ATLAS-SWM] Generated="+
                            world.getName());
            if (world == null) {
                throw new IllegalStateException("SWM generated no Bukkit world named " + bukkitWorldName);
            }
            return world;
        });
    }

    private <T> CompletableFuture<T> supplySync(SyncSupplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future;
    }

    private <T> CompletableFuture<T> supplyAsyncStage(String stage, SyncSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Throwable throwable) {
                throw new IllegalStateException("SWM stage failed: " + stage, throwable);
            }
        });
    }
    private void rebindArenaLocations(Arena arena, World world) {
        arena.setSpawnRed(rebind(arena.getSpawnRed(), world));
        arena.setSpawnBlue(rebind(arena.getSpawnBlue(), world));

        arena.setBedRed(rebind(arena.getBedRed(), world));
        arena.setBedBlue(rebind(arena.getBedBlue(), world));

        arena.setRedWool(rebind(arena.getRedWool(), world));
        arena.setBlueWool(rebind(arena.getBlueWool(), world));

        arena.setMinimumBoundary(rebind(arena.getMinimumBoundary(), world));
        arena.setMaximumBoundary(rebind(arena.getMaximumBoundary(), world));
        // Restore Bridge/BattleRush goal portal blocks after SWM reset
        for (String key : arena.getRedGoalBlocks()) {

            String[] split = key.split(":");

            if (split.length != 4) {
                continue;
            }

            world.getBlockAt(
                    Integer.parseInt(split[1]),
                    Integer.parseInt(split[2]),
                    Integer.parseInt(split[3])
            ).setType(org.bukkit.Material.ENDER_PORTAL);
        }

        for (String key : arena.getBlueGoalBlocks()) {

            String[] split = key.split(":");

            if (split.length != 4) {
                continue;
            }

            world.getBlockAt(
                    Integer.parseInt(split[1]),
                    Integer.parseInt(split[2]),
                    Integer.parseInt(split[3])
            ).setType(org.bukkit.Material.ENDER_PORTAL);
        }
    }

    private Location rebind(Location loc, World world) {
        if (loc == null) return null;

        return new Location(
                world,
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );
    }
    private void logFailure(String stage, Throwable throwable) {
        plugin.getLogger().log(Level.SEVERE, "[AtlasPractice] SWM failure while attempting to " + stage + ".", unwrap(throwable));
    }

    private static Throwable unwrap(Throwable throwable) {
        if (throwable instanceof InvocationTargetException invocation && invocation.getTargetException() != null) {
            return invocation.getTargetException();
        }
        if (throwable.getCause() != null && (throwable instanceof java.util.concurrent.CompletionException || throwable instanceof RuntimeException)) {
            return unwrap(throwable.getCause());
        }
        return throwable;
    }

    private static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }

    private interface SyncSupplier<T> {
        T get() throws Exception;
    }
}
