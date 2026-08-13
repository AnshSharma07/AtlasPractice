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
import com.ansh.atlaspractice.leaderboard.hologram.LeaderboardDisplaySettings;
import com.ansh.atlaspractice.leaderboard.hologram.LeaderboardHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
        Bukkit.getScheduler().runTaskLater(plugin, this::loadHolograms, 40L);
    }

    public void shutdown() {
        for (LeaderboardHologram hologram : activeHolograms.values()) {
            hologram.destroy();
        }
        activeHolograms.clear();
        cache.shutdown();
    }
    public void spawnForPlayer(Player player) {
        if (player == null || player.getWorld() == null) {
            return;
        }

        for (LeaderboardHologram hologram : activeHolograms.values()) {
            hologram.spawnForPlayer(player);
        }
    }
    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "lb.yml");

        if (!configFile.exists()) {
            File oldFile = new File(plugin.getDataFolder(), "leaderboards.yml");
            if (oldFile.exists()) {
                if (!oldFile.renameTo(configFile)) {
                    try {
                        java.nio.file.Files.copy(oldFile.toPath(), configFile.toPath());
                    } catch (IOException e) {
                        plugin.getLogger().severe("Could not move leaderboards.yml to lb.yml!");
                    }
                }
            } else {
                plugin.saveResource("lb.yml", false);
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save lb.yml!");
        }
    }

    public void loadHolograms() {
        activeHolograms.values().forEach(LeaderboardHologram::destroy);
        activeHolograms.clear();

        ConfigurationSection section = config.getConfigurationSection("holograms");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            String worldName = section.getString(key + ".world");
            if (worldName == null) {
                continue;
            }

            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().warning("Could not load leaderboard " + key + ": world " + worldName + " is not loaded.");
                continue;
            }

            double x = section.getDouble(key + ".x");
            double y = section.getDouble(key + ".y");
            double z = section.getDouble(key + ".z");
            String typeName = section.getString(key + ".type", StatType.WINS.name());
            String kit = section.getString(key + ".kit", "NONE");

            StatType type;
            try {
                type = StatType.valueOf(typeName.toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Skipping leaderboard " + key + ": invalid type " + typeName + ".");
                continue;
            }

            if (!kit.equalsIgnoreCase("NONE") && plugin.getKitManager().getKit(kit).isEmpty()) {
                plugin.getLogger().warning("Skipping leaderboard " + key + ": kit " + kit + " does not exist.");
                continue;
            }

            Location location = new Location(world, x, y, z);
            LeaderboardHologram hologram = new LeaderboardHologram(key, location, type, kit, cache, getDisplaySettings(key));
            hologram.spawn();
            activeHolograms.put(key, hologram);
        }
    }

    public LeaderboardDisplaySettings getDisplaySettings(String name) {
        String style = config.getString("holograms." + name + ".style", "default");
        String base = "leaderboards.styles." + style;
        if (!plugin.getConfig().isConfigurationSection(base)) {
            base = "leaderboards.styles.default";
        }

        int maxEntries = plugin.getConfig().getInt(base + ".max-entries", 10);
        String title = plugin.getConfig().getString(base + ".title", "&b&lTop {stat}");
        String kitLine = plugin.getConfig().getString(base + ".kit-line", "&7{kit}");
        String entry = plugin.getConfig().getString(base + ".entry", "&e#{position} &f{player} &7- &b{value}");
        String empty = plugin.getConfig().getString(base + ".empty", "&e#{position} &8N/A");
        String footer = plugin.getConfig().getString(base + ".footer", "");

        return new LeaderboardDisplaySettings(maxEntries, title, kitLine, entry, empty, footer);
    }

    public boolean createLeaderboard(String name, Location location, StatType type, String kit) {
        if (name == null || name.trim().isEmpty() || location == null || location.getWorld() == null) {
            return false;
        }

        if (activeHolograms.containsKey(name)) {
            return false;
        }

        String normalizedKit = normalizeKit(kit);
        ConfigurationSection section = config.createSection("holograms." + name);
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("type", type.name());
        section.set("kit", normalizedKit == null ? "NONE" : normalizedKit);
        section.set("style", "default");
        saveConfig();

        LeaderboardHologram hologram = new LeaderboardHologram(name, location, type, normalizedKit, cache, getDisplaySettings(name));
        hologram.spawn();
        activeHolograms.put(name, hologram);
        return true;
    }

    public boolean createGeneratedLeaderboard(Location location, StatType type, String kit) {
        if (hasLeaderboardNear(location, type, kit, 0.25)) {
            return false;
        }

        String base = "lb_" + type.name().toLowerCase();
        String normalizedKit = normalizeKit(kit);
        if (normalizedKit != null) {
            base += "_" + normalizedKit.toLowerCase();
        } else {
            base += "_global";
        }

        String name = base;
        int number = 2;
        while (activeHolograms.containsKey(name)) {
            name = base + "_" + number++;
        }

        return createLeaderboard(name, location, type, normalizedKit);
    }

    public boolean deleteLeaderboard(String name) {
        LeaderboardHologram hologram = activeHolograms.remove(name);
        if (hologram == null) {
            return false;
        }

        hologram.destroy();
        config.set("holograms." + name, null);
        saveConfig();
        return true;
    }

    public boolean removeNearest(Location location, StatType type, String kit) {
        LeaderboardHologram nearest = findNearest(location, type, kit);
        return nearest != null && deleteLeaderboard(nearest.getName());
    }

    public LeaderboardHologram findNearest(Location location, StatType type, String kit) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        String normalizedKit = normalizeKit(kit);
        LeaderboardHologram nearest = null;
        double bestDistance = Double.MAX_VALUE;

        for (LeaderboardHologram hologram : activeHolograms.values()) {
            if (hologram.getType() != type) {
                continue;
            }

            String hologramKit = normalizeKit(hologram.getKit());
            if (normalizedKit == null ? hologramKit != null : !normalizedKit.equalsIgnoreCase(hologramKit)) {
                continue;
            }

            Location target = hologram.getLocation();
            if (target.getWorld() == null || !target.getWorld().getUID().equals(location.getWorld().getUID())) {
                continue;
            }

            double distance = target.distanceSquared(location);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = hologram;
            }
        }

        return nearest;
    }

    public int count(StatType type, String kit) {
        String normalizedKit = normalizeKit(kit);
        int count = 0;

        for (LeaderboardHologram hologram : activeHolograms.values()) {
            if (hologram.getType() != type) {
                continue;
            }

            String hologramKit = normalizeKit(hologram.getKit());
            if (normalizedKit == null ? hologramKit == null : normalizedKit.equalsIgnoreCase(hologramKit)) {
                count++;
            }
        }

        return count;
    }

    public boolean hasLeaderboardNear(Location location, StatType type, String kit, double radius) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        String normalizedKit = normalizeKit(kit);
        double maxDistance = radius * radius;

        for (LeaderboardHologram hologram : activeHolograms.values()) {
            if (hologram.getType() != type) {
                continue;
            }

            String hologramKit = normalizeKit(hologram.getKit());
            if (normalizedKit == null ? hologramKit != null : !normalizedKit.equalsIgnoreCase(hologramKit)) {
                continue;
            }

            Location target = hologram.getLocation();
            if (target.getWorld() != null
                    && target.getWorld().getUID().equals(location.getWorld().getUID())
                    && target.distanceSquared(location) <= maxDistance) {
                return true;
            }
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

    private String normalizeKit(String kit) {
        if (kit == null || kit.trim().isEmpty() || kit.equalsIgnoreCase("NONE") || kit.equalsIgnoreCase("OVERALL")) {
            return null;
        }
        return kit;
    }
}
