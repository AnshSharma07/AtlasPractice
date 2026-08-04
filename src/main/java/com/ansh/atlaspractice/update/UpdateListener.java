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

package com.ansh.atlaspractice.update;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

    private final AtlasPracticePlugin plugin;

    public UpdateListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        if (!e.getPlayer().isOp())
            return;

        VersionChecker checker = plugin.getVersionChecker();

        if (checker == null)
            return;

        if (!checker.isUpdateAvailable()) {

            e.getPlayer().sendMessage(ChatColor.GREEN +
                    "AtlasPractice is running the latest version.");

            return;

        }

        e.getPlayer().sendMessage("");

        e.getPlayer().sendMessage(ChatColor.RED + "AtlasPractice Update Available");

        e.getPlayer().sendMessage(ChatColor.GRAY +
                "Current Version: " +
                plugin.getDescription().getVersion());

        e.getPlayer().sendMessage(ChatColor.GRAY +
                "Latest Version: " +
                checker.getLatestVersion());

        e.getPlayer().sendMessage(ChatColor.YELLOW +
                "Download:");

        e.getPlayer().sendMessage(ChatColor.AQUA +
                "https://modularboyansh.xyz/updates");

        e.getPlayer().sendMessage("");

    }

}