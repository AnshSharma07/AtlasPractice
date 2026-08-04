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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SharedArenaGroup {

    private final String id;
    private final String displayName;
    private final List<String> arenaIds = new ArrayList<>();

    public SharedArenaGroup(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getArenaIds() {
        return Collections.unmodifiableList(arenaIds);
    }

    public void setArenaIds(List<String> arenaIds) {
        this.arenaIds.clear();
        for (String arenaId : arenaIds) {
            addArena(arenaId);
        }
    }

    public boolean addArena(String arenaId) {
        String normalized = arenaId.toLowerCase();
        if (arenaIds.stream().anyMatch(id -> id.equalsIgnoreCase(normalized))) {
            return false;
        }
        arenaIds.add(normalized);
        return true;
    }

    public boolean removeArena(String arenaId) {
        return arenaIds.removeIf(id -> id.equalsIgnoreCase(arenaId));
    }
}
