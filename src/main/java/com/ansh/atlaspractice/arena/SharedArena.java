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

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.List;
import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import com.ansh.atlaspractice.match.Match;
import org.bukkit.World;
@Getter
public final class SharedArena extends Arena {

    private final BlockTracker blockTracker;

    public SharedArena(String id, String displayName) {
        super(id, displayName, ArenaType.SHARED);

        this.blockTracker = new BlockTracker();

        this.setState(ArenaState.DISABLED);
    }
    @Override
    public void cleanAndResetWorld() {
        AtlasPracticePlugin.getInstance().getWorldService().resetArena(this);
    }
    public void resetForNextRound(Match match, Runnable callback) {

        // Remove player placed blocks
        for (String key : match.getPlacedBlocks()) {

            String[] parts = key.split(":");
            if (parts.length != 4)
                continue;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null)
                continue;

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            world.getBlockAt(x, y, z).setType(Material.AIR);
        }

        match.getPlacedBlocks().clear();

        // Restore broken blocks
        for (BlockTracker.BlockSnapshot snapshot :
                blockTracker.compileReversionSnapshots()) {

            Block block = snapshot.location().getBlock();

            block.setType(snapshot.material());
            block.setData(snapshot.data());
        }

        blockTracker.clear();

        // Remove temporary entities
        if (getWorld() != null) {

            for (Entity entity : getWorld().getEntities()) {

                if (entity instanceof Player)
                    continue;

                entity.remove();
            }
        }

        if (callback != null) {
            Bukkit.getScheduler().runTask(
                    AtlasPracticePlugin.getInstance(),
                    callback
            );
        }
    }
}
