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

import lombok.RequiredArgsConstructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
@RequiredArgsConstructor
public final class ArenaManager {

    private final Map<String, Arena> arenaCache = new ConcurrentHashMap<>();
    public void registerArena(Arena arena) {
        if (arena.getWorldName() != null && isWorldAssigned(arena.getWorldName(), arena.getId())) {
            throw new IllegalArgumentException("World '" + arena.getWorldName() + "' is already assigned to another arena.");
        }
        this.arenaCache.put(arena.getId().toLowerCase(), arena);
    }
    public boolean isWorldAssigned(String worldName, String exceptArenaId) {
        if (worldName == null) return false;
        return this.arenaCache.values().stream()
                .filter(arena -> exceptArenaId == null || !arena.getId().equalsIgnoreCase(exceptArenaId))
                .anyMatch(arena -> arena.getWorldName() != null && arena.getWorldName().equalsIgnoreCase(worldName));
    }
    public Optional<Arena> getArenaByWorld(String worldName) {
        if (worldName == null) return Optional.empty();
        return this.arenaCache.values().stream()
                .filter(arena -> arena.getWorldName() != null && arena.getWorldName().equalsIgnoreCase(worldName))
                .findFirst();
    }
    public void unregisterArena(Arena arena) {
        this.arenaCache.remove(arena.getId().toLowerCase());
    }
    public Optional<Arena> getArena(String id) {
        return Optional.ofNullable(this.arenaCache.get(id.toLowerCase()));
    }
    public Optional<Arena> findAvailableArena(ArenaType type) {
        return this.arenaCache.values().stream()
                .filter(arena -> arena.getType() == type)
                .filter(Arena::isAvailable)
                .findFirst();
    }
    public void resetAllArenas() {
        this.arenaCache.values().forEach(Arena::cleanAndResetWorld);
    }
    public Collection<Arena> getArenas() {
        return Collections.unmodifiableCollection(this.arenaCache.values());
    }
    public void clear() {
        this.arenaCache.clear();
    }
}
