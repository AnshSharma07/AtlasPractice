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

package com.ansh.atlaspractice.cosmetics.listener;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class ProjectileTrailListener implements Listener {

    private static final String SHOOTER_METADATA = "atlas_cosmetic_shooter";
    private final AtlasPracticePlugin plugin;

    public ProjectileTrailListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!isSupported(projectile)) {
            return;
        }
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }
        Player shooter = (Player) projectile.getShooter();
        projectile.setMetadata(SHOOTER_METADATA, new FixedMetadataValue(plugin, shooter.getUniqueId().toString()));
        new BukkitRunnable() {
            @Override
            public void run() {
                if (projectile.isDead() || !projectile.isValid()) {
                    cancel();
                    return;
                }
                Player player = Bukkit.getPlayer(shooter.getUniqueId());
                if (player == null || !player.isOnline()) {
                    cancel();
                    return;
                }
                plugin.getCosmeticManager().getProjectileTrail(player).play(projectile);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean isSupported(Entity entity) {
        return entity instanceof Arrow
                || entity instanceof Snowball
                || entity instanceof Egg
                || entity instanceof EnderPearl
                || entity instanceof Fireball
                || entity instanceof FishHook;
    }
}
