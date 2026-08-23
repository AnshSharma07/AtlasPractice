/*
 * AtlasPractice - Open-source Minecraft Practice plugin.
 * Copyright (C) 2026 Ansh Sharma (Modular Boy Ansh)
 *
 * This file is part of AtlasPractice.
 *
 * AtlasPractice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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
package com.ansh.atlaspractice.replay;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class ReplayCommand implements CommandExecutor, Listener {

    private final AtlasPracticePlugin plugin;
    private final ReplayManager replayManager;

    public ReplayCommand(AtlasPracticePlugin plugin, ReplayManager replayManager) {
        this.plugin = plugin;
        this.replayManager = replayManager;
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();

        if (!message.startsWith("/")) {
            return;
        }

        String[] split = message.substring(1).split(" ");

        if (split.length < 2 || !split[0].equalsIgnoreCase("replay")
                || !split[1].equalsIgnoreCase("search")) {
            return;
        }

        String[] args = new String[split.length - 1];
        System.arraycopy(split, 1, args, 0, args.length);

        event.setCancelled(true);
        onCommand(event.getPlayer(), null, "replay", args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            new ReplayMenu(plugin, replayManager, false, 1, null).openMenu(player);
            return true;
        }

        if (!args[0].equalsIgnoreCase("search") || args.length == 1) {
            player.sendMessage("§cUsage: /replay search <player> [player] [player] ...");
            return true;
        }

        List<String> players = new ArrayList<>();

        for (int i = 1; i < args.length; i++) {
            String name = args[i].toLowerCase();

            if (!players.contains(name)) {
                players.add(name);
            }
        }

        List<ReplayMetadata> replays = new ArrayList<>();

        for (ReplayMetadata replay : replayManager.getAvailableReplays()) {
            boolean matches = true;

            for (String name : players) {
                boolean hasPlayer = false;

                for (String replayPlayer : replay.getPlayers()) {
                    if (replayPlayer.equalsIgnoreCase(name)) {
                        hasPlayer = true;
                        break;
                    }
                }

                if (!hasPlayer) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                replays.add(replay);
            }
        }

        new ReplayMenu(plugin, replayManager, false, 1, replays).openMenu(player);
        return true;
    }
}