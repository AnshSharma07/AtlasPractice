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
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.time.LocalDate;

@RequiredArgsConstructor
public final class PlayerConnectionListener implements Listener {

    private final AtlasPracticePlugin plugin;
    private final ProfileManager profileManager;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Profile joinedProfile = this.profileManager.createProfile(player.getUniqueId(), player.getName());
        this.plugin.getLevelManager().refresh(joinedProfile);
        String today = LocalDate.now().toString();
        if (!today.equals(joinedProfile.getLastDailyLogin())) {
            joinedProfile.setLastDailyLogin(today);
            this.plugin.getLevelManager().reward(joinedProfile, "daily-login");
            this.plugin.getDatabaseService().saveProfileDataAsync(joinedProfile);
        }

        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        if (this.plugin.getConfig().contains("lobby-spawn.world")) {
            String worldName = this.plugin.getConfig().getString("lobby-spawn.world");
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world != null) {
                double x = this.plugin.getConfig().getDouble("lobby-spawn.x");
                double y = this.plugin.getConfig().getDouble("lobby-spawn.y");
                double z = this.plugin.getConfig().getDouble("lobby-spawn.z");
                float yaw = (float) this.plugin.getConfig().getDouble("lobby-spawn.yaw");
                float pitch = (float) this.plugin.getConfig().getDouble("lobby-spawn.pitch");
                player.teleport(new org.bukkit.Location(world, x, y, z, yaw, pitch));
            } else {
                player.teleport(player.getWorld().getSpawnLocation());
            }
        } else {
            player.teleport(player.getWorld().getSpawnLocation());
        }

        this.plugin.getInventoryUtil().applyLobbyHotbarItems(player);

        event.setJoinMessage("§7[§a+§7] §b" + player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        AtlasPracticePlugin.getInstance().getBotManager().removeBot(event.getPlayer());
        Player player = event.getPlayer();
        Profile profile = this.profileManager.getProfile(player.getUniqueId());

        if (profile != null) {
            if (profile.getState() == com.ansh.atlaspractice.profile.ProfileState.MATCH) {
                this.plugin.getMatchManager().handleQuit(player);
            }

            if (profile.getPartyId() != null) {
                this.plugin.getPartyManager().forceLeaveParty(player);
            }

            this.profileManager.saveAndUnloadProfile(player.getUniqueId());
        }

        event.setQuitMessage("§7[§c-§7] §b" + player.getName());
    }
}
