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
import com.ansh.atlaspractice.match.BridgeMatch;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchTeam;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BridgeListener implements Listener {

    private final AtlasPracticePlugin plugin;
    private final Map<UUID, Long> arrowCooldowns = new HashMap<>();

    public BridgeListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
        if (matchOpt.isEmpty() || !(matchOpt.get() instanceof BridgeMatch)) {
            return;
        }

        BridgeMatch match = (BridgeMatch) matchOpt.get();
        if (match.getState() != Match.MatchState.FIGHTING) {
            return;
        }

        String key = event.getTo().getWorld().getName()
                + ":" + event.getTo().getBlockX()
                + ":" + event.getTo().getBlockY()
                + ":" + event.getTo().getBlockZ();

        MatchTeam team = match.getTeam(player);
        if (team == null) {
            return;
        }

        boolean redTeam = match.isRedTeam(team);
        if ((redTeam && match.getArena().getRedGoalBlocks().contains(key))
                || (!redTeam && match.getArena().getBlueGoalBlocks().contains(key))) {
            player.sendMessage("§cYou can't score in your own goal.");
            match.teleportToSpawn(player);
            return;
        }

        if ((redTeam && match.getArena().getBlueGoalBlocks().contains(key))
                || (!redTeam && match.getArena().getRedGoalBlocks().contains(key))) {
            match.handleGoalScore(team);
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
        if (matchOpt.isEmpty() || !(matchOpt.get() instanceof BridgeMatch)) {
            return;
        }

        long now = System.currentTimeMillis();
        long expires = arrowCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (expires > now) {
            event.setCancelled(true);
            player.sendMessage("§cYou can shoot another arrow in " + ((expires - now + 999L) / 1000L) + "s.");
            return;
        }

        arrowCooldowns.put(player.getUniqueId(), now + 5000L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.ARROW, 1));
            }
        }, 100L);
    }
}
