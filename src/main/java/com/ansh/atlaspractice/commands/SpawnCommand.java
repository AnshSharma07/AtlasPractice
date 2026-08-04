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

package com.ansh.atlaspractice.commands;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;
import com.ansh.atlaspractice.profile.ProfileState;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class SpawnCommand implements CommandExecutor {

    private final AtlasPracticePlugin plugin;

    public SpawnCommand(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can execute spawn commands.");
            return true;}
        Player player = (Player) sender;
        if (label.equalsIgnoreCase("setlobbyspawn")) {
            if (!player.hasPermission("atlaspractice.admin")) {
                player.sendMessage("§cYou do not have permission to execute this command.");
                return true;
            }

            Location loc = player.getLocation();
            plugin.getConfig().set("lobby-spawn.world", loc.getWorld().getName());
            plugin.getConfig().set("lobby-spawn.x", loc.getX());
            plugin.getConfig().set("lobby-spawn.y", loc.getY());
            plugin.getConfig().set("lobby-spawn.z", loc.getZ());
            plugin.getConfig().set("lobby-spawn.yaw", (double) loc.getYaw());
            plugin.getConfig().set("lobby-spawn.pitch", (double) loc.getPitch());
            plugin.saveConfig();

            player.sendMessage("§aLobby spawn point successfully updated!");
            return true;
        }

        // handler /spawn
        if (label.equalsIgnoreCase("spawn")) {
            Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
            
            // Check if the player is inside an active match
            if (profile != null && profile.getState() == ProfileState.MATCH) {
                player.sendMessage("§cYou are not allowed to use the command during Match.");
                return true;
            }

            // retrievE custom location configuration from config.yml
            if (!plugin.getConfig().contains("lobby-spawn.world")) {
                // Fallback to world default spawn if custom location isn't configured yet
                player.teleport(player.getWorld().getSpawnLocation());
                player.sendMessage("§aTeleported to world spawn (Custom spawn not set).");
                return true;
            }

            String worldName = plugin.getConfig().getString("lobby-spawn.world");
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage("§cError: The configured lobby world '" + worldName + "' is not loaded.");
                return true;
            }

            double x = plugin.getConfig().getDouble("lobby-spawn.x");
            double y = plugin.getConfig().getDouble("lobby-spawn.y");
            double z = plugin.getConfig().getDouble("lobby-spawn.z");
            float yaw = (float) plugin.getConfig().getDouble("lobby-spawn.yaw");
            float pitch = (float) plugin.getConfig().getDouble("lobby-spawn.pitch");

            Location spawnLoc = new Location(world, x, y, z, yaw, pitch);
            player.teleport(spawnLoc);
            player.sendMessage("§aTeleported to the lobby spawn!");
            return true;
        }
        Profile profile = plugin.getProfileManager()
                .getProfile(player.getUniqueId());

        if (profile != null &&
                profile.getState() == ProfileState.SPECTATE) {

            player.sendMessage("§cYou cannot use /spawn while spectating.");
            player.sendMessage("§7Use the §cLeave Spectator §7bed to exit spectator mode.");

            return true;
        }

        return false;
    }
}
