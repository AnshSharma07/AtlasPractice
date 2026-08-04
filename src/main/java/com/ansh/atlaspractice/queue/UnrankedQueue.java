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

package com.ansh.atlaspractice.queue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


public final class UnrankedQueue {

    private final List<QueueEntry> entries = new CopyOnWriteArrayList<>();

    public void addEntry(QueueEntry entry) {
        this.entries.add(entry);
    }

    public boolean removeEntry(UUID entryId) {
        return this.entries.removeIf(entry -> entry.getEntryId().equals(entryId));
    }

    public List<QueueEntry> getSnapshot() {
        return new java.util.ArrayList<>(this.entries);
    }
}
