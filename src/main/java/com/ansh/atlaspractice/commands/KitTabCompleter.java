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

package com.ansh.atlaspractice.commands;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
public class KitTabCompleter implements TabCompleter {

    private final AtlasPracticePlugin plugin;

    public KitTabCompleter(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {

        if (args.length == 1) {
            return Arrays.asList(
                            "create",
                            "edit",
                            "save",
                            "delete",
                            "list",
                            "addarena",
                            "removearena"
                    ).stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 &&
                args[0].equalsIgnoreCase("addarena")) {

            List<String> arenas = new ArrayList<>();

            for (Arena arena : plugin.getArenaManager().getArenas()) {

                boolean used = false;

                for (Kit kit : plugin.getKitManager().getAllKits()) {

                    if (kit.getArenaIds().stream()
                            .anyMatch(id -> id.equalsIgnoreCase(arena.getId()))) {

                        used = true;
                        break;
                    }
                }

                if (!used) {
                    arenas.add(arena.getId());
                }
            }

            return arenas.stream()
                    .filter(a -> a.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 &&
                args[0].equalsIgnoreCase("addarena")) {

            List<String> kits = new ArrayList<>();

            for (Kit kit : plugin.getKitManager().getAllKits()) {
                kits.add(kit.getId());
            }

            return kits.stream()
                    .filter(k -> k.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 &&
                args[0].equalsIgnoreCase("removearena")) {

            List<String> arenas = new ArrayList<>();

            for (Kit kit : plugin.getKitManager().getAllKits()) {
                arenas.addAll(kit.getArenaIds());
            }

            return arenas.stream()
                    .filter(a -> a.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 &&
                args[0].equalsIgnoreCase("removearena")) {

            List<String> kits = new ArrayList<>();

            for (Kit kit : plugin.getKitManager().getAllKits()) {
                kits.add(kit.getId());
            }

            return kits.stream()
                    .filter(k -> k.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
