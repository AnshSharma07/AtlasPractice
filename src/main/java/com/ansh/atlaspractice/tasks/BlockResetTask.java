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

package com.ansh.atlaspractice.tasks;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


@RequiredArgsConstructor
public final class BlockResetTask extends BukkitRunnable {

    private final Queue<Block> blockClearQueue = new ConcurrentLinkedQueue<>();

    public void queueWorldBlocks(Collection<Block> structures) {
        this.blockClearQueue.addAll(structures);
    }

    @Override
    public void run() {
        // Pop and clear up to 20 blocks per game tick to keep the main thread smooth
        int processedThisTick = 0;
        while (!this.blockClearQueue.isEmpty() && processedThisTick < 20) {
            Block segment = this.blockClearQueue.poll();
            if (segment != null) {
                segment.setType(Material.AIR);
            }
            processedThisTick++;
        }

        if (this.blockClearQueue.isEmpty()) {
            this.cancel();
        }
    }
}
