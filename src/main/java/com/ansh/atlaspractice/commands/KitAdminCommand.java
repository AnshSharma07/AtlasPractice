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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class KitAdminCommand implements CommandExecutor {

    private final AtlasPracticePlugin plugin;

    public KitAdminCommand(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("atlaspractice.admin")) {
            player.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§6Kit Commands:");
            player.sendMessage("§e/kit create <name>");
            player.sendMessage("§e/kit edit <name>");
            player.sendMessage("§e/kit save <name>");
            player.sendMessage("§e/kit delete <name>");
            player.sendMessage("§e/kit list");
            player.sendMessage("§e/kit addarena <arena> <kit>");
            player.sendMessage("§e/kit removearena <arena> <kit>");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {

            player.sendMessage("§6Registered Kits:");

            for (Kit kit : plugin.getKitManager().getAllKits()) {
                player.sendMessage(
                        " §7- §e"
                                + kit.getDisplayName()
                                + " §8("
                                + kit.getId()
                                + ") §7Arenas: §b"
                                + (plugin.getSharedArenaService().usesSharedArenas(kit)
                                ? "Shared: " + String.join(", ", plugin.getSharedArenaService().getArenaIds(kit))
                                : (kit.getArenaIds().isEmpty()
                                ? "None"
                                : String.join(", ", kit.getArenaIds())))
                );
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {

            if (args.length < 2) {
                player.sendMessage("§cUsage: /kit create <name>");
                return true;
            }
            String id = args[1].toLowerCase();
            if (plugin.getKitManager().exists(id)) {
                player.sendMessage("§cThat kit already exists.");
                return true;
            }
            Kit kit = new Kit(id, args[1]);
            plugin.getKitManager().registerKit(kit);
            plugin.getKitManager().saveKitToDisk(kit);
            kit.setMainContents(
                    player.getInventory()
                            .getContents()
                            .clone()
            );

            kit.setArmorContents(
                    player.getInventory()
                            .getArmorContents()
                            .clone()
            );

            plugin.getKitManager().saveKitToDisk(kit);

            player.sendMessage(
                    "§aKit §e" + kit.getDisplayName()
                            + " §asaved from your inventory."
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("edit")) {

            if (args.length < 2) {
                player.sendMessage("§cUsage: /kit edit <name>");
                return true;
            }

            Kit kit = plugin.getKitManager()
                    .getKit(args[1])
                    .orElse(null);

            if (kit == null) {
                player.sendMessage("§cThat kit does not exist.");
                return true;
            }

            plugin.getKitEditorListener();
            plugin.getKitEditorListener().openEditor(
                    player,
                    kit.getId(),
                    kit.getDisplayName(),
                    kit.getMainContents().clone(),
                    kit.getArmorContents().clone()
            );

            return true;
        }
        if (args[0].equalsIgnoreCase("save")) {

            if (args.length < 2) {
                player.sendMessage("§cUsage: /kit save <kit>");
                return true;
            }

            Kit kit = plugin.getKitManager()
                    .getKit(args[1])
                    .orElse(null);

            if (kit == null) {
                player.sendMessage("§cKit not found.");
                return true;
            }

            kit.setMainContents(
                    player.getInventory()
                            .getContents()
                            .clone()
            );

            kit.setArmorContents(
                    player.getInventory()
                            .getArmorContents()
                            .clone()
            );

            plugin.getKitManager().saveKitToDisk(kit);

            player.sendMessage(
                    "§aSaved kit §e" + kit.getDisplayName()
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("addarena")) {

            if (args.length < 3) {
                player.sendMessage("§cUsage: /kit addarena <arena> <kit>");
                return true;
            }

            String arenaName = args[1];
            String kitName = args[2];

            Kit kit = plugin.getKitManager()
                    .getKit(kitName)
                    .orElse(null);

            if (kit == null) {
                player.sendMessage("§cThat kit does not exist.");
                return true;
            }

            if (plugin.getSharedArenaService().usesSharedArenas(kit)) {
                player.sendMessage("§cThis kit uses Shared Arenas. Use /arena useshared instead.");
                return true;
            }

            if (plugin.getArenaManager()
                    .getArena(arenaName)
                    .isEmpty()) {

                player.sendMessage("§cArena '" + arenaName + "' does not exist.");
                return true;
            }

            // Prevent assigning an arena that is already used by any Shared Arena mode
            if (plugin.getSharedArenaService().isEnabled()) {
                for (String mode : plugin.getSharedArenaService().getEnabledModes()) {
                    if (plugin.getSharedArenaService().isAssigned(mode, arenaName)) {
                        player.sendMessage("§cArena '" + arenaName + "' is already assigned to the shared arena pool.");
                        return true;
                    }
                }
            }

            for (Kit other : plugin.getKitManager().getAllKits()) {

                if (other == kit) {
                    continue;
                }

                if (other.getArenaIds().stream()
                        .anyMatch(id -> id.equalsIgnoreCase(arenaName))) {

                    player.sendMessage(
                            "§cArena '" + arenaName
                                    + "' is already assigned to kit "
                                    + other.getDisplayName() + "."
                    );

                    return true;
                }
            }

            kit.addArena(arenaName);

            plugin.getKitManager().saveKitToDisk(kit);

            player.sendMessage(
                    "§aAssigned arena §e"
                            + arenaName
                            + " §ato kit §e"
                            + kit.getDisplayName()
            );

            return true;
        }
        if (args[0].equalsIgnoreCase("removearena")) {

            if (args.length < 3) {
                player.sendMessage("§cUsage: /kit removearena <kit>");
                return true;
            }

            String arenaName = args[1];
            String kitName = args[2];

            Kit kit = plugin.getKitManager()
                    .getKit(kitName)
                    .orElse(null);

            if (kit == null) {
                player.sendMessage("§cThat kit does not exist.");
                return true;
            }

            if (!kit.getArenaIds().stream()
                    .anyMatch(id -> id.equalsIgnoreCase(arenaName))) {

                player.sendMessage("§cThat arena is not assigned to this kit.");
                return true;
            }
            kit.removeArena(arenaName);

            plugin.getKitManager().saveKitToDisk(kit);

            player.sendMessage(
                    "§aRemoved arena §e"
                            + arenaName
                            + " §afrom kit §e"
                            + kit.getDisplayName()
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {

            if (args.length < 2) {
                player.sendMessage("§cUsage: /kit delete <name>");
                return true;
            }

            String id = args[1].toLowerCase();

            if (!plugin.getKitManager().exists(id)) {
                player.sendMessage("§cThat kit does not exist.");
                return true;
            }

            plugin.getKitManager().deleteKit(id);

            player.sendMessage("§aDeleted kit §e" + id);

            return true;
        }

        player.sendMessage("§cUnknown subcommand.");
        return true;
    }

}

