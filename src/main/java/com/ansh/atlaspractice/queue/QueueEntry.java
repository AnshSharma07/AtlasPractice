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

package com.ansh.atlaspractice.queue;

import com.ansh.atlaspractice.kit.Kit;
import java.util.UUID;


public final class QueueEntry {

    private final UUID entryId; // Maps back to Player UUID or Party UUID directly
    private final Kit kit;
    private final boolean party;
    private final boolean ranked;
    private final int baselineElo;
    private final long executionStartTimeMillis;
    private final int layoutIndex;

    public QueueEntry(UUID entryId,
                      Kit kit,
                      boolean party,
                      boolean ranked,
                      int baselineElo,
                      int layoutIndex) {
        this.entryId = entryId;
        this.kit = kit;
        this.party = party;
        this.ranked = ranked;
        this.layoutIndex = layoutIndex;
        this.baselineElo = baselineElo;
        this.executionStartTimeMillis = System.currentTimeMillis();
    }

    public UUID getEntryId(){return entryId;} public Kit getKit(){return kit;} public boolean isParty(){return party;} public boolean isRanked(){return ranked;} public int getBaselineElo(){return baselineElo;} public long getExecutionStartTimeMillis(){return executionStartTimeMillis;}


    public int getSecondsInQueue() {
        return (int) ((System.currentTimeMillis() - this.executionStartTimeMillis) / 1000L);
    }
    public int getLayoutIndex() {
        return layoutIndex;
    }
}