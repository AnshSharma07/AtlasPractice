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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ArenaTabCompleter implements TabCompleter {

    private final AtlasPracticePlugin plugin;

    public ArenaTabCompleter(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {


         // /arena <tab>

        if (args.length == 1) {

            return Arrays.asList(
                            "create",
                            "list",
                            "delete",
                            "wizard",
                            "setspawnred",
                            "setspawnblue",
                            "setbedred",
                            "setbedblue",
                            "save",
                            "enable",
                            "autosetbreakableblocks",
                            "autobridgeblocks",
                            "autosetgoals",
                            "goalwand",
                            "useshared",
                            "disable",
                            "setpos1",
                            "setpos2"
                    ).stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if ((args.length == 2 || args.length == 3 || args.length == 4) &&
                args[0].equalsIgnoreCase("create")) {

            return Collections.emptyList();
        }
        if (args.length == 2 &&
                (
                        args[0].equalsIgnoreCase("setspawnred")
                                || args[0].equalsIgnoreCase("setspawnblue")
                                || args[0].equalsIgnoreCase("setbedred")
                                || args[0].equalsIgnoreCase("setbedblue")
                                || args[0].equalsIgnoreCase("autosetbreakableblocks")
                                || args[0].equalsIgnoreCase("autobridgeblocks")
                                || args[0].equalsIgnoreCase("autosetgoals")
                                || args[0].equalsIgnoreCase("wizard")
                                || args[0].equalsIgnoreCase("setpos1")
                                || args[0].equalsIgnoreCase("setpos2")
                                || args[0].equalsIgnoreCase("save")
                                || args[0].equalsIgnoreCase("enable")
                                || args[0].equalsIgnoreCase("disable")
                                || args[0].equalsIgnoreCase("delete")
                                || args[0].equalsIgnoreCase("useshared")
                )) {

            List<String> arenas = new ArrayList<>();

            for (Arena arena : plugin.getArenaManager().getArenas()) {
                arenas.add(arena.getId());
            }

            return arenas.stream()
                    .filter(a -> a.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
