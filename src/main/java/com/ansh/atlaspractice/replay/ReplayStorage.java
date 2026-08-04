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

package com.ansh.atlaspractice.replay;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class ReplayStorage {
    private final AtlasPracticePlugin plugin;
    private final File file;
    private final YamlConfiguration config;

    public ReplayStorage(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "replays.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public synchronized void save(ReplayMetadata metadata) {
        String path = "replays." + metadata.getReplayId();
        config.set(path + ".replay-id", metadata.getReplayId());
        config.set(path + ".replay-uuid", metadata.getReplayUuid().toString());
        config.set(path + ".match-id", metadata.getMatchId().toString());
        config.set(path + ".arena", metadata.getArena());
        config.set(path + ".kit", metadata.getKit());
        config.set(path + ".players", metadata.getPlayers());
        config.set(path + ".winner", metadata.getWinner());
        config.set(path + ".loser", metadata.getLoser());
        config.set(path + ".duration-ms", metadata.getDurationMillis());
        config.set(path + ".date", metadata.getDate().toString());
        config.set(path + ".match-type", metadata.getMatchType());
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Unable to save replay metadata for " + metadata.getReplayId(), exception);
        }
    }
}
