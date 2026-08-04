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

package com.ansh.atlaspractice.leaderboard;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.leaderboard.hologram.Hologram;
import com.ansh.atlaspractice.leaderboard.hologram.LeaderboardHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardManager {

    private final AtlasPracticePlugin plugin;
    private final Map<String, LeaderboardHologram> activeHolograms = new ConcurrentHashMap<>();
    private final LeaderboardCache cache;

    private File configFile;
    private FileConfiguration config;

    public LeaderboardManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        this.cache = new LeaderboardCache(plugin);
        loadConfig();
    }

    public void start() {
        this.cache.startCachingTask();
        Bukkit.getScheduler().runTaskLater(plugin, this::loadHolograms, 40L); // Wait for worlds
    }

    public void shutdown() {
        for (LeaderboardHologram hologram : activeHolograms.values()) {
            hologram.destroy();
        }
        activeHolograms.clear();
        this.cache.shutdown();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "leaderboards.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create leaderboards.yml!");
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save leaderboards.yml!");
        }
    }

    public void loadHolograms() {
        activeHolograms.values().forEach(LeaderboardHologram::destroy);
        activeHolograms.clear();

        ConfigurationSection section = config.getConfigurationSection("holograms");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String world = section.getString(key + ".world");
            double x = section.getDouble(key + ".x");
            double y = section.getDouble(key + ".y");
            double z = section.getDouble(key + ".z");
            StatType type = StatType.valueOf(section.getString(key + ".type", "GLOBAL_ELO"));
            String kit = section.getString(key + ".kit", "NONE");

            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
            LeaderboardHologram hologram = new LeaderboardHologram(key, loc, type, kit, this.cache);
            hologram.spawn();
            activeHolograms.put(key, hologram);
        }
    }

    public boolean createLeaderboard(String name, Location location, StatType type, String kit) {
        if (activeHolograms.containsKey(name)) return false;

        ConfigurationSection section = config.createSection("holograms." + name);
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("type", type.name());
        section.set("kit", kit);
        saveConfig();

        LeaderboardHologram hologram = new LeaderboardHologram(name, location, type, kit, this.cache);
        hologram.spawn();
        activeHolograms.put(name, hologram);
        return true;
    }

    public boolean deleteLeaderboard(String name) {
        LeaderboardHologram hologram = activeHolograms.remove(name);
        if (hologram != null) {
            hologram.destroy();
            config.set("holograms." + name, null);
            saveConfig();
            return true;
        }
        return false;
    }

    public Collection<LeaderboardHologram> getHolograms() {
        return activeHolograms.values();
    }

    public void updateAllHolograms() {
        for (LeaderboardHologram hologram : activeHolograms.values()) {
            hologram.updateLines();
        }
    }
    public LeaderboardCache getCache() {
        return cache;
    }
}