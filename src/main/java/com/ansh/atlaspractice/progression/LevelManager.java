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

package com.ansh.atlaspractice.progression;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import com.ansh.atlaspractice.progression.event.PlayerCoinsChangeEvent;
import com.ansh.atlaspractice.progression.event.PlayerExperienceChangeEvent;
import com.ansh.atlaspractice.progression.event.PlayerLevelUpEvent;
import com.ansh.atlaspractice.util.ActionBarUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class LevelManager {

    private final AtlasPracticePlugin plugin;
    private final YamlConfiguration config;
    private final Map<String, Long> xp = new HashMap<>();
    private final Map<String, Long> coins = new HashMap<>();

    public LevelManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        File f = new File(plugin.getDataFolder(), "progression.yml");
        if (!f.exists()) {
            plugin.saveResource("progression.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(f);
        load("xp", xp);
        load("coins", coins);
    }

    private void sendRewardActionBar(Profile profile, long xp, long coins) {
        if (!config.getBoolean("actionbar.enabled", true)) {
            return;
        }

        Player player = Bukkit.getPlayer(profile.getUniqueId());
        if (player == null) {
            return;
        }

        String message;
        if (xp > 0 && coins > 0) {
            message = config.getString("actionbar.rewards")
                    .replace("%xp%", String.valueOf(xp))
                    .replace("%coins%", String.valueOf(coins));
        } else if (xp > 0) {
            message = config.getString("actionbar.xp-only")
                    .replace("%xp%", String.valueOf(xp));
        } else if (coins > 0) {
            message = config.getString("actionbar.coins-only")
                    .replace("%coins%", String.valueOf(coins));
        } else {
            return;
        }

        message = ChatColor.translateAlternateColorCodes('&', message);
        ActionBarUtil.send(player, message);

        if (config.getBoolean("actionbar.sound", true)) {
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.7F, 1.4F);
        }
    }

    private void load(String path, Map<String, Long> map) {
        if (config.getConfigurationSection(path) != null) {
            for (String k : config.getConfigurationSection(path).getKeys(false)) {
                map.put(k.toLowerCase(), config.getLong(path + "." + k));
            }
        }
    }

    public long getXpReward(String key) {
        return Math.max(0L, xp.getOrDefault(key.toLowerCase(), 0L));
    }

    public long getCoinReward(String key) {
        return Math.max(0L, coins.getOrDefault(key.toLowerCase(), 0L));
    }

    public int getLevel(long experience) {
        long safe = Math.max(0, experience);
        int level = 1;
        while (safe >= getTotalXPForLevel(level + 1) && level < 10000) {
            level++;
        }
        return level;
    }

    private long getTotalXPForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        long total = 0;
        for (int l = 1; l < level; l++) {
            total += getXPForNextLevel(l);
        }
        return total;
    }

    public long getCurrentLevelXP(long experience) {
        return getTotalXPForLevel(getLevel(experience));
    }

    public long getXPIntoCurrentLevel(long experience) {
        return Math.max(0, experience - getCurrentLevelXP(experience));
    }

    public long getXPForNextLevel(int level) {
        return Math.max(1, Math.round(140D + (level * 45D) + Math.pow(level, 2.15D) * 4.8D));
    }

    public double getProgress(long experience) {
        int level = getLevel(experience);
        return Math.max(0D, Math.min(1D, (double) getXPIntoCurrentLevel(experience) / (double) getXPForNextLevel(level)));
    }

    public void addExperience(Profile p, long amount) {
        if (amount > 0) {
            setExperience(p, p.getExperience() + amount);
        }
    }

    public void removeExperience(Profile p, long amount) {
        if (amount > 0) {
            setExperience(p, Math.max(0, p.getExperience() - amount));
        }
    }

    public void setExperience(Profile p, long amount) {
        long old = p.getExperience();
        int oldLevel = p.getLevel();
        long val = Math.max(0, amount);

        p.setExperienceRaw(val);
        p.setLevel(getLevel(val));

        Bukkit.getPluginManager().callEvent(new PlayerExperienceChangeEvent(p, old, val));

        if (p.getLevel() > oldLevel) {
            Bukkit.getPluginManager().callEvent(new PlayerLevelUpEvent(p, oldLevel, p.getLevel()));
            announceLevelUp(p, oldLevel, p.getLevel());
        }

        refresh(p);
    }

    public void addCoins(Profile p, long amount) {
        if (amount > 0) {
            setCoins(p, p.getCoins() + amount);
        }
    }

    public void removeCoins(Profile p, long amount) {
        if (amount > 0) {
            setCoins(p, Math.max(0, p.getCoins() - amount));
        }
    }

    public void setCoins(Profile p, long amount) {
        long old = p.getCoins();
        long val = Math.max(0, amount);

        p.setCoinsRaw(val);

        Bukkit.getPluginManager().callEvent(new PlayerCoinsChangeEvent(p, old, val));
        refresh(p);
    }

    public void reward(Profile p, String key) {
        long xp = getXpReward(key);
        long coins = getCoinReward(key);

        if (xp > 0) {
            addExperience(p, xp);
        }

        if (coins > 0) {
            addCoins(p, coins);
        }

        sendRewardActionBar(p, xp, coins);
    }

    public void sendPurchaseActionBar(Profile profile, long coins) {
        if (!config.getBoolean("actionbar.enabled", true)) {
            return;
        }

        Player player = Bukkit.getPlayer(profile.getUniqueId());
        if (player == null) {
            return;
        }

        String msg = config.getString("actionbar.purchase")
                .replace("%coins%", String.valueOf(coins));

        msg = ChatColor.translateAlternateColorCodes('&', msg);

        ActionBarUtil.send(player, msg);

        if (config.getBoolean("actionbar.sound", true)) {
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.7F, 0.7F);
        }
    }

    public void refresh(Profile p) {
        Player player = Bukkit.getPlayer(p.getUniqueId());
        if (player == null) {
            return;
        }

        updateXpBar(player, p);
        updateDisplayName(player, p);
    }

    public void updateXpBar(Player player, Profile p) {
        if (p.getState() == ProfileState.MATCH) {
            return;
        }

        player.setLevel(p.getLevel());
        player.setExp((float) getProgress(p.getExperience()));
    }

    public void updateDisplayName(Player player, Profile p) {
        String lvl = LevelColor.formatLevel(p.getLevel());
        String name = player.getName() + ChatColor.GRAY + " [Lvl " + lvl + ChatColor.GRAY + "]";

        player.setPlayerListName(name);
        player.setDisplayName(name);
    }

    private void announceLevelUp(Profile p, int oldLevel, int newLevel) {
        Player player = Bukkit.getPlayer(p.getUniqueId());
        if (player == null) {
            return;
        }

        if (config.getBoolean("level-up.title.enabled", true)) {
            player.sendTitle(ChatColor.GOLD + "Level Up!", ChatColor.YELLOW + String.valueOf(oldLevel) + " \u2192 " + newLevel);
        }

        player.sendMessage(ChatColor.GREEN + "You reached Practice Level " + LevelColor.formatLevel(newLevel) + ChatColor.GREEN + "!");
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F);

        if (config.getBoolean("level-up.firework", true)) {
            Firework fw = player.getWorld().spawn(player.getLocation(), Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().withColor(Color.LIME).with(FireworkEffect.Type.BALL).build());
            meta.setPower(0);
            fw.setFireworkMeta(meta);
        }
    }
}