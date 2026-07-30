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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.explosion.ExplosionType;
import com.ansh.atlaspractice.match.Match;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

public final class TNTListener implements Listener {

    private final AtlasPracticePlugin plugin;

    public TNTListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTNTPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.TNT) {
            return;
        }

        Player player = event.getPlayer();
        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
        if (matchOpt.isEmpty()) {
            return;
        }

        if (!plugin.getExplosionConfig().isTntEnabled()) {
            return;
        }

        event.setCancelled(true);
        ItemStack item = player.getItemInHand();

        if (item != null && item.getType() == Material.TNT) {

            if (item.getAmount() <= 1) {
                player.setItemInHand(null);
            } else {
                item.setAmount(item.getAmount() - 1);
                player.setItemInHand(item);
            }

            player.updateInventory();
        }
        Location spawnLoc = event.getBlockPlaced().getLocation().add(0.5, 0.0, 0.5);
        TNTPrimed tnt = (TNTPrimed) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.PRIMED_TNT);
        tnt.setFuseTicks(plugin.getExplosionConfig().getTntFuseTicks());
        tnt.setMetadata("atlas_tnt_source", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntityType() == EntityType.PRIMED_TNT) {
            if (event.getEntity().hasMetadata("atlas_tnt_source")) {
                event.setRadius(0F);
                event.setFire(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.PRIMED_TNT) {
            if (event.getEntity().hasMetadata("atlas_tnt_source")) {
                event.blockList().clear();
                event.setCancelled(true);
                plugin.getExplosionManager().createCustomExplosion(
                        event.getLocation(),
                        ExplosionType.TNT,
                        event.getEntity()
                );
            }
        }
    }
}