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

package com.ansh.atlaspractice.tasks;

import com.ansh.atlaspractice.scoreboard.ScoreboardBoard;
import com.ansh.atlaspractice.scoreboard.ScoreboardAdapter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
public final class ScoreboardUpdateTask extends BukkitRunnable {

    private final ScoreboardAdapter adapter;
    private final Map<UUID, ScoreboardBoard> boardMap = new ConcurrentHashMap<>();

    public void registerPlayer(Player player) {
        this.boardMap.remove(player.getUniqueId());
        this.boardMap.put(player.getUniqueId(), new ScoreboardBoard(player));
    }

    public void unregisterPlayer(Player player) {
        this.boardMap.remove(player.getUniqueId());
    }

    @Override
    public void run() {
        for (UUID uuid : this.boardMap.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                this.boardMap.remove(uuid);
                continue;
            }

            // Repaint variables (pings, durations) via 1-second interval execution limits
            String title = this.adapter.getTitle(player);
            List<String> lines = this.adapter.getLines(player);

            ScoreboardBoard board = this.boardMap.get(uuid);
            if (board != null) {
                board.updateContents(title, lines);
            }
        }
    }
}
