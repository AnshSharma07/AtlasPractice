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

package com.ansh.atlaspractice.config;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Manages ranked.yml â€” controls global ranked toggle, elo
 * win requirements and per-kit enable/disable.
 */
public final class RankedConfig {

    public enum WinsMode {
        GLOBAL,
        PER_KIT
    }

    private final AtlasPracticePlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public RankedConfig(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranked.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            writeDefaults();
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void writeDefaults() {
        YamlConfiguration defaults = new YamlConfiguration();
        defaults.set("Ranked.enabled", true);
        defaults.set("Ranked.win-elo", 10);
        defaults.set("Ranked.lose-elo", 10);
        defaults.set("Ranked.minimum-wins-needed", "GLOBAL");
        defaults.set("Ranked.global.wins", 0);
        // No per-kit defaults â€” the admin gui adds them from KitManager
        try {
            defaults.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create ranked.yml", e);
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save ranked.yml", e);
        }
    }


    public boolean isEnabled() {
        return config.getBoolean("Ranked.enabled", true);
    }

    public int getWinElo() {
        return config.getInt("Ranked.win-elo", 10);
    }

    public int getLoseElo() {
        return config.getInt("Ranked.lose-elo", 10);
    }

    public WinsMode getWinsMode() {
        String raw = config.getString("Ranked.minimum-wins-needed", "GLOBAL");
        try {
            return WinsMode.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return WinsMode.GLOBAL;
        }
    }

    public int getGlobalWins() {
        return config.getInt("Ranked.global.wins", 0);
    }

    /**
     * Returns whether this kit is enabled for ranked.
     * Defaults to true if the key is absent (so new kits are opt-in by default via gui).
     */
    public boolean isKitEnabled(String kitId) {
        String path = "Ranked.kits." + kitId + ".enabled";
        if (!config.contains(path)) {
            return false;
        }
        return config.getBoolean(path, false);
    }

    public int getKitWins(String kitId) {
        return config.getInt("Ranked.kits." + kitId + ".wins", 0);
    }

    public void setKitEnabled(String kitId, boolean enabled) {
        config.set("Ranked.kits." + kitId + ".enabled", enabled);
        if (!config.contains("Ranked.kits." + kitId + ".wins")) {
            config.set("Ranked.kits." + kitId + ".wins", 0);
        }
        save();
    }

    public void setEnabled(boolean enabled) {
        config.set("Ranked.enabled", enabled);
        save();
    }
}