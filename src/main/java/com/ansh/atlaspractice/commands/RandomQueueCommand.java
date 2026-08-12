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
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class RandomQueueCommand implements CommandExecutor {

    private final AtlasPracticePlugin plugin;
    private final Random random = new Random();

    /*
     * was mainly added to test the capabilities and performance with Mineflayer bots, currently this
     * command still exists to test the plugin.
     */
    private static final String[] RANDOM_KITS = {
            "boxing"

    };

    public RandomQueueCommand(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Player player = (Player) sender;

        List<Kit> available = new ArrayList<>();

        for (String id : RANDOM_KITS) {
            Optional<Kit> optional = plugin.getKitManager()
                    .getRegistry()
                    .getKit(id);

            optional.ifPresent(available::add);
        }

        if (available.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No random queue kits were found.");
            return true;
        }

        Kit selected = available.get(random.nextInt(available.size()));

        player.sendMessage(ChatColor.YELLOW + "Randomly selected "
                + ChatColor.GOLD
                + selected.getDisplayName());

        plugin.getQueueManager().joinUnrankedQueue(player, selected);

        return true;
    }
}