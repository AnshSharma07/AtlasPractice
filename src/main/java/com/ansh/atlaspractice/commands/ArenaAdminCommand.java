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
import com.ansh.atlaspractice.arena.*;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.listeners.ArenaBreakableListener;
import com.ansh.atlaspractice.menus.SharedArenaModeMenu;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.command.Command;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.util.concurrent.CompletionException;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class ArenaAdminCommand implements CommandExecutor {

    private final ArenaManager arenaManager;
    private final ArenaRepository arenaRepository;
    private static final Map<UUID, String> EDITING_ARENAS = new HashMap<>();

    private void sendClickableCommand(Player player, String command) {
        TextComponent message = new TextComponent("§e" + command);
        message.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                command
        ));

        player.spigot().sendMessage(message);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly in-game active staff operators can execute this command .");
            return true;
        }

        if (!player.hasPermission("atlaspractice.admin.arena") && !player.hasPermission("atlas.admin")) {
            player.sendMessage("§cInsufficient permissions.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cUsage:");
            player.sendMessage(" §e/arena list");
            player.sendMessage(" §e/arena create <arena> <world> [Slime World Name]");
            player.sendMessage(" §e/arena delete <arena>");
            player.sendMessage(" §e/arena setpos1 <arena>");
            player.sendMessage(" §e/arena setpos2 <arena>");
            player.sendMessage(" §e/arena setspawnred <arena>");
            player.sendMessage(" §e/arena setspawnblue <arena>");
            player.sendMessage(" §e/arena setbedred <arena>");
            player.sendMessage(" §e/arena setbedblue <arena>");
            player.sendMessage(" §e/arena goalwand");
            player.sendMessage(" §e/arena autobridgeblocks <arena>");
            player.sendMessage(" §e/arena save <arena>");
            player.sendMessage(" §e/arena enable <arena>");
            player.sendMessage(" §e/arena useshared [arena]");
            player.sendMessage(" §e/arena disable <arena>");
            return true;
        }

        String action = args[0].toLowerCase();
        if (action.equals("goalwand")) {
            giveGoalWands(player);
            return true;
        }
        if (action.equals("useshared")) {
            if (!AtlasPracticePlugin.getInstance().getSharedArenaService().isEnabled()) {
                player.sendMessage("§cShared arenas are disabled in config.yml.");
                return true;
            }
            String selectedArenaId = args.length >= 2 ? args[1].toLowerCase() : EDITING_ARENAS.get(player.getUniqueId());
            if (selectedArenaId == null || selectedArenaId.isBlank()) {
                player.sendMessage("§cSelect or specify an arena first: /arena useshared <arena>");
                return true;
            }
            Optional<Arena> arenaOpt = arenaManager.getArena(selectedArenaId);
            if (arenaOpt.isEmpty()) {
                player.sendMessage("§cArena not found.");
                return true;
            }
            new SharedArenaModeMenu(AtlasPracticePlugin.getInstance(), arenaOpt.get()).openMenu(player);
            return true;
        }
        String arenaId = args.length >= 2
                ? args[1].toLowerCase()
                : "";
        if (!action.equals("list") && args.length < 2) {
            player.sendMessage("§cUsage: /arena " + action + " <arena>");
            return true;
        }
        switch (action) {
            case "list" -> {

                player.sendMessage("§8§m------------------");

                for (Arena arena : this.arenaManager.getArenas()) {

                    String kitName = "None";

                    for (Kit kit :
                            AtlasPracticePlugin.getInstance()
                                    .getKitManager()
                                    .getAllKits()) {

                        if (kit.getArenaIds().stream()
                                .anyMatch(id -> id.equalsIgnoreCase(arena.getId()))) {

                            kitName = kit.getDisplayName();
                            break;
                        }
                    }

                    String stateColor;

                    switch (arena.getState()) {
                        case FREE -> stateColor = "§a";
                        case ALLOCATED -> stateColor = "§c";
                        case RESETTING -> stateColor = "§6";
                        case DISABLED -> stateColor = "§7";

                        default -> stateColor = "§f";
                    }

                    player.sendMessage(
                            "§7- §e" + arena.getId()
                                    + " §8["
                                    + stateColor
                                    + arena.getState().name()
                                    + "§8]"
                                    + " §7World: §b" + arena.getWorldName()
                                    + " §7Kit: §b"
                                    + kitName
                    );
                }

                player.sendMessage("§8§m------------------");
            }
            case "delete" -> {

                Optional<Arena> arenaOpt =
                        arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena not found.");
                    return true;
                }

                Arena arena = arenaOpt.get();

                if (arena.getState() == ArenaState.ALLOCATED
                        || arena.getState() == ArenaState.RESETTING) {

                    player.sendMessage(
                            "§cCannot delete an arena that is currently in use."
                    );

                    return true;
                }

                int unlinked = 0;

                for (Kit kit :
                        AtlasPracticePlugin.getInstance()
                                .getKitManager()
                                .getAllKits()) {

                    boolean removed = kit.getArenaIds().removeIf(id ->
                            id.equalsIgnoreCase(arena.getId()));

                    if (removed) {

                        AtlasPracticePlugin.getInstance()
                                .getKitManager()
                                .saveKitToDisk(kit);

                        unlinked++;

                        // Optional warning
                        if (kit.getArenaIds().isEmpty()) {
                            player.sendMessage(
                                    "§eWarning: Kit §6"
                                            + kit.getDisplayName()
                                            + " §eno longer has any arenas."
                            );
                        }
                    }
                }

                AtlasPracticePlugin.getInstance().getSharedArenaService().removeArenaFromAllModes(arena.getId());

                arenaManager.unregisterArena(arena);

                arenaRepository.deleteArena(arena.getId());

                player.sendMessage(
                        "§aArena §e" + arena.getId() + " §ahas been deleted."
                );

                if (unlinked > 0) {

                    player.sendMessage(
                            "§7Automatically removed from §e" + unlinked + " §7kit(s)."
                    );
                }
                return true;
            }
            case "enable" -> {

                Optional<Arena> arenaOpt =
                        arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena not found.");
                    return true;
                }

                Arena arena = arenaOpt.get();

                if (arena.getState() == ArenaState.FREE) {
                    player.sendMessage("§cArena already enabled.");
                    return true;
                }

                if (arena.getSpawnRed() == null) {
                    player.sendMessage("§cSpawn Red not set.");
                    return true;
                }

                if (arena.getSpawnBlue() == null) {
                    player.sendMessage("§cSpawn Blue not set.");
                    return true;
                }
                if (arena.getMinimumBoundary() == null) {
                    player.sendMessage("§cPosition 1 not set.");
                    return true;
                }

                if (arena.getMaximumBoundary() == null) {
                    player.sendMessage("§cPosition 2 not set.");
                    return true;
                }

                if (arena.getMode() == ArenaMode.BRIDGE
                        && (arena.getBreakableBlocks().isEmpty()
                        || arena.getRedGoalBlocks().isEmpty()
                        || arena.getBlueGoalBlocks().isEmpty())) {
                    player.sendMessage("§cBridge arenas need bridge blocks, red goal, and blue goal configured.");
                    return true;
                }

                if (arena.getMode() == ArenaMode.BATTLERUSH
                        && (arena.getRedGoalBlocks().isEmpty()
                        || arena.getBlueGoalBlocks().isEmpty())) {
                    player.sendMessage("§cBattleRush arenas need red goal and blue goal configured.");
                    return true;
                }

                arena.setState(ArenaState.FREE);

                arenaRepository.saveArena(arena);

                player.sendMessage(
                        "§aArena enabled."
                );
            }
            case "disable" -> {

                Optional<Arena> arenaOpt =
                        arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena not found.");
                    return true;
                }

                Arena arena = arenaOpt.get();

                if (arena.getState() == ArenaState.ALLOCATED) {

                    player.sendMessage(
                            "§cCannot disable arena while match is active."
                    );

                    return true;
                }

                if (arena.getState() == ArenaState.RESETTING) {

                    player.sendMessage(
                            "§cArena is resetting."
                    );

                    return true;
                }

                if (arena.getState() == ArenaState.DISABLED) {

                    player.sendMessage(
                            "§cArena already disabled."
                    );

                    return true;
                }

                arena.setState(ArenaState.DISABLED);

                arenaRepository.saveArena(arena);

                player.sendMessage(
                        "§eArena disabled."
                );
            }
            case "create" -> {

                if (args.length < 3) {
                    player.sendMessage("§cUsage: /arena create <arena> <world> [templateWorld]");
                    return true;
                }

                String worldName = args[2];
                String templateWorld = args.length >= 4 ? args[3] : worldName;

                if (this.arenaManager.isWorldAssigned(worldName, null)) {
                    player.sendMessage("§cWorld '" + worldName + "' is already assigned to another arena.");
                    return true;
                }

                if (this.arenaManager.getArena(arenaId).isPresent()) {
                    player.sendMessage("§cArena identity match configuration already exists: " + arenaId);
                    return true;
                }
                SharedArena newArena = new SharedArena(arenaId, arenaId);
                newArena.setWorldName(worldName);
                newArena.setTemplateWorld(templateWorld);
                newArena.setSlimeWorldName(templateWorld);
                player.sendMessage("§eLoading SWM template §b" + templateWorld + " §eand generating runtime world §b" + worldName + "§e...");

                AtlasPracticePlugin plugin = AtlasPracticePlugin.getInstance();
                plugin.getWorldService().loadArenaWorld(newArena).whenComplete((world, throwable) -> {
                    if (throwable != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(
                                "§cSWM could not load world '" + worldName + "': " + fullMessage(throwable)));
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            this.arenaManager.registerArena(newArena);
                            arenaRepository.saveArena(newArena);
                            Location center = world.getSpawnLocation();

                            Location safe = null;

                            for (int radius = 0; radius <= 10 && safe == null; radius++) {

                                for (int x = -radius; x <= radius && safe == null; x++) {
                                    for (int z = -radius; z <= radius && safe == null; z++) {

                                        int y = world.getHighestBlockYAt(
                                                center.getBlockX() + x,
                                                center.getBlockZ() + z
                                        );

                                        Location test = new Location(
                                                world,
                                                center.getBlockX() + x + 0.5,
                                                y + 1,
                                                center.getBlockZ() + z + 0.5
                                        );

                                        if (test.getBlock().getType() == Material.AIR &&
                                                test.clone().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                                                test.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {

                                            safe = test;
                                        }
                                    }
                                }
                            }

                            if (safe == null) {
                                safe = world.getSpawnLocation().clone().add(0.5, 1, 0.5);
                            }

                            player.teleport(safe);
                            player.sendMessage("§aCreated arena: §e" + arenaId + " §7(world: §b" + worldName + "§7, template: §b" + templateWorld + "§7)");

                            player.sendMessage("§eSelect Arena Type:");

                            TextComponent bedfight = new TextComponent("§a§l[BF/FBF]");
                            TextComponent normal = new TextComponent(" §e§l[NORMAL]");
                            TextComponent shared = new TextComponent(" §6§l[SHARED ARENA]");
                            TextComponent bridge = new TextComponent(" §b§l[BRIDGE]");
                            TextComponent battleRush = new TextComponent(" §d§l[BATTLERUSH]");

                            bedfight.setClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/arena wizard " + arenaId + " yes"));

                            normal.setClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/arena wizard " + arenaId + " no"));

                            shared.setClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/arena useshared " + arenaId));

                            bridge.setClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/arena wizard " + arenaId + " bridge"));

                            battleRush.setClickEvent(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/arena wizard " + arenaId + " battlerush"));

                            if (AtlasPracticePlugin.getInstance().getSharedArenaService().isEnabled()) {
                                player.spigot().sendMessage(bedfight, normal, shared, bridge, battleRush);
                            } else {
                                player.spigot().sendMessage(bedfight, normal, bridge, battleRush);
                            }

                            EDITING_ARENAS.put(
                                    player.getUniqueId(),
                                    arenaId.toLowerCase()
                            );
                        } catch (RuntimeException exception) {
                            player.sendMessage("§cArena loaded but could not be registered: " + exception.getMessage());
                        }
                    });
                });
            }
            case "setpos1" -> {

                Optional<Arena> arenaOpt =
                        this.arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {

                    player.sendMessage(
                            "§cArena not found."
                    );

                    return true;
                }

                Arena arena = arenaOpt.get();

                arena.setMinimumBoundary(
                        player.getLocation()
                );

                arenaRepository.saveArena(arena);

                player.sendMessage(
                        "§aPosition 1 set."
                );
            }
            case "setpos2" -> {

                Optional<Arena> arenaOpt =
                        this.arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {

                    player.sendMessage(
                            "§cArena not found."
                    );

                    return true;
                }

                Arena arena = arenaOpt.get();

                arena.setMaximumBoundary(
                        player.getLocation()
                );

                arenaRepository.saveArena(arena);

                player.sendMessage(
                        "§aPosition 2 set."
                );
            }
            case "setspawnred" -> {
                Optional<Arena> arenaOpt = this.arenaManager.getArena(arenaId);
                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena target profile entry lookup found no results for: " + arenaId);
                    return true;
                }
                arenaOpt.get().setSpawnRed(player.getLocation());
                arenaRepository.saveArena(arenaOpt.get());
                player.sendMessage("§aSpawn point set for  §cRed successfully.");
            }
            case "setspawnblue" -> {
                Optional<Arena> arenaOpt = this.arenaManager.getArena(arenaId);
                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena target profile entry lookup found no results for: " + arenaId);
                    return true;
                }
                arenaOpt.get().setSpawnBlue(player.getLocation());
                arenaRepository.saveArena(arenaOpt.get());
                player.sendMessage("§aSpawn point set for  §9Blue successfully");
            }
            case "setbedred" -> {

                Optional<Arena> arenaOpt =
                        this.arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena not found.");
                    return true;
                }

                Block target = player.getTargetBlock((HashSet<Byte>) null, 5);

                if (target == null
                        || target.getType() != Material.BED_BLOCK) {

                    player.sendMessage("§cYou must look at a bed.");
                    return true;
                }

                Arena arena = arenaOpt.get();

                if (arena.getBedBlue() != null
                        && arena.getBedBlue().distanceSquared(target.getLocation()) <= 4) {

                    arena.setBedBlue(null);
                }

                arena.setBedRed(target.getLocation());

                arenaRepository.saveArena(arena);

                player.sendMessage("§cRed bed set.");
            }
            case "setbedblue" -> {

                Optional<Arena> arenaOpt =
                        this.arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena not found.");
                    return true;
                }

                Block target = player.getTargetBlock((HashSet<Byte>) null, 5);

                if (target == null
                        || target.getType() != Material.BED_BLOCK) {

                    player.sendMessage("§cYou must look at a bed.");
                    return true;
                }

                Arena arena = arenaOpt.get();

                if (arena.getBedRed() != null
                        && arena.getBedRed().distanceSquared(target.getLocation()) <= 4) {

                    arena.setBedRed(null);
                }

                arena.setBedBlue(target.getLocation());

                arenaRepository.saveArena(arena);

                player.sendMessage("§9Blue bed set.");
            }
            case "autobridgeblocks" -> {
                giveBridgeWand(player);
            }
            case "savebreakable" -> {

                Optional<Arena> arenaOpt = arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena not found.");
                    return true;
                }

                Arena arena = arenaOpt.get();

                Location pos1 = ArenaBreakableListener.getBridgePos1(player.getUniqueId());
                Location pos2 = ArenaBreakableListener.getBridgePos2(player.getUniqueId());

                if (pos1 == null || pos2 == null) {
                    player.sendMessage("§cYou must right-click both corners with the Bridge Selector first.");
                    return true;
                }

                Location min = arena.getMinimumBoundary();
                Location max = arena.getMaximumBoundary();

                if (min == null || max == null) {
                    player.sendMessage("§cSet pos1 and pos2 for the arena first.");
                    return true;
                }

                int selMinX = Math.min(pos1.getBlockX(), pos2.getBlockX());
                int selMaxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
                int selMinY = Math.min(pos1.getBlockY(), pos2.getBlockY());
                int selMaxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
                int selMinZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
                int selMaxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

                arena.getBreakableBlocks().clear();

                for (int x = selMinX; x <= selMaxX; x++) {
                    for (int y = selMinY; y <= selMaxY; y++) {
                        for (int z = selMinZ; z <= selMaxZ; z++) {
                            Block b = pos1.getWorld().getBlockAt(x, y, z);
                            if (b.getType() != Material.AIR) {
                                arena.getBreakableBlocks().add(
                                        b.getWorld().getName() + ":" + x + ":" + y + ":" + z
                                );
                            }
                        }
                    }
                }

                arenaRepository.saveArena(arena);
                ArenaBreakableListener.clearBridgeSelection(player.getUniqueId());

                player.sendMessage("§aSaved §e" + arena.getBreakableBlocks().size() + "§a breakable blocks for §b" + arena.getId() + "§a.");
            }

            case "autosetbreakableblocks" -> {

                Optional<Arena> arenaOpt =
                        arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {
                    player.sendMessage("§cArena not found.");
                    return true;
                }

                Arena arena = arenaOpt.get();

                if (arena.getBedRed() == null
                        || arena.getBedBlue() == null) {

                    player.sendMessage(
                            "§cBoth beds must be set first."
                    );
                    return true;
                }

                arena.getBreakableBlocks().clear();

                scanBedDefense(arena, arena.getBedRed());
                scanBedDefense(arena, arena.getBedBlue());

                arenaRepository.saveArena(arena);

                player.sendMessage(
                        "§aAuto-selected §e"
                                + arena.getBreakableBlocks().size()
                                + "§a breakable blocks."
                );
            }
            case "save" -> {

                Optional<Arena> arenaOpt =
                        this.arenaManager.getArena(arenaId);

                if (arenaOpt.isEmpty()) {

                    player.sendMessage(
                            "§cArena target profile entry lookup found no results for: "
                                    + arenaId
                    );

                    return true;
                }

                Arena arena = arenaOpt.get();

                if (arena.getSpawnRed() == null
                        || arena.getSpawnBlue() == null) {

                    player.sendMessage(
                            "§cArena needs both spawns."
                    );

                    return true;
                }

                this.arenaRepository.saveArena(arena);

                player.sendMessage(
                        "§aSuccessfully saved arena: §e"
                                + arenaId
                );
            }
            case "wizard" -> {

                if (args.length < 3) {
                    return true;
                }

                String choice = args[2];

                if (choice.equalsIgnoreCase("yes")) {
                    Optional<Arena> arenaOpt = arenaManager.getArena(arenaId);

                    if (arenaOpt.isPresent()) {
                        Arena arena = arenaOpt.get();
                        arena.setMode(ArenaMode.BEDFIGHT);
                        arenaRepository.saveArena(arena);
                    }

                    // ====== AUTO SETUP SNIPPET (Only triggers for BedFight/Fireball!) ======
                    player.sendMessage("§7====================================");
                    player.sendMessage("§bBedFight / Fireball Arena Detected");
                    player.sendMessage("§bWould you like to auto setup this arena?");
                    player.sendMessage("§cWARNING: §7Automatic setup is experimental.");
                    player.sendMessage("§7Always verify the arena afterwards.\n");

                    TextComponent yesBtn = new TextComponent("§a[ YES ] ");
                    yesBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arenaautosetup " + arenaId + " yes"));

                    TextComponent noBtn = new TextComponent("§c[ NO ]");
                    noBtn.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/arena wizard " + arenaId + " manual"));

                    player.spigot().sendMessage(yesBtn, noBtn);
                    player.sendMessage("§7====================================");

                } else if (choice.equalsIgnoreCase("manual")) {
                    // This executes if admin clicks no.
                    player.sendMessage("§6Special Arena Setup (Manual):");
                    sendClickableCommand(player, "/arena setpos1 " + arenaId);
                    sendClickableCommand(player, "/arena setpos2 " + arenaId);
                    sendClickableCommand(player, "/arena setspawnred " + arenaId);
                    sendClickableCommand(player, "/arena setspawnblue " + arenaId);
                    sendClickableCommand(player, "/arena setbedred " + arenaId);
                    sendClickableCommand(player, "/arena setbedblue " + arenaId);
                    player.sendMessage("§e/arena autosetbreakableblocks " + arenaId);
                    sendClickableCommand(player, "/arena save " + arenaId);
                    sendClickableCommand(player, "/arena enable " + arenaId);
                    sendClickableCommand(player, "/arena disable " + arenaId);
                    sendClickableCommand(player, "/arena delete " + arenaId);

                    player.sendMessage("§e/kit addarena <arena> <kit>");

                    player.sendMessage("§7Use Breakable Selector to select breakable blocks.");

                    giveBreakableSelectorStick(player);

                }
                else if (choice.equalsIgnoreCase("bridge")) {

                    Optional<Arena> arenaOpt = arenaManager.getArena(arenaId);

                    if (arenaOpt.isPresent()) {
                        Arena arena = arenaOpt.get();

                        arena.setMode(ArenaMode.BRIDGE);
                        arenaRepository.saveArena(arena);
                    }

                    player.sendMessage("§6Bridge Arena Setup:");

                    sendClickableCommand(player, "/arena setpos1 " + arenaId);
                    sendClickableCommand(player, "/arena setpos2 " + arenaId);
                    sendClickableCommand(player, "/arena setspawnred " + arenaId);
                    sendClickableCommand(player, "/arena setspawnblue " + arenaId);
                    sendClickableCommand(player, "/arena autobridgeblocks " + arenaId);
                    sendClickableCommand(player, "/arena goalwand");
                    sendClickableCommand(player, "/arena save " + arenaId);
                    sendClickableCommand(player, "/arena enable " + arenaId);
                }
                else if (choice.equalsIgnoreCase("battlerush")) {

                    Optional<Arena> arenaOpt = arenaManager.getArena(arenaId);

                    if (arenaOpt.isPresent()) {
                        Arena arena = arenaOpt.get();
                        arena.setMode(ArenaMode.BATTLERUSH);
                        arenaRepository.saveArena(arena);
                    }

                    player.sendMessage("§6BattleRush Arena Setup:");
                    sendClickableCommand(player, "/arena setpos1 " + arenaId);
                    sendClickableCommand(player, "/arena setpos2 " + arenaId);
                    sendClickableCommand(player, "/arena setspawnred " + arenaId);
                    sendClickableCommand(player, "/arena setspawnblue " + arenaId);
                    sendClickableCommand(player, "/arena goalwand");
                    
                    sendClickableCommand(player, "/arena save " + arenaId);
                    sendClickableCommand(player, "/arena enable " + arenaId);
                }
                else {
                    Optional<Arena> arenaOpt = arenaManager.getArena(arenaId);

                    arenaOpt.ifPresent(arena -> {
                        arena.setMode(ArenaMode.NORMAL);
                        arenaRepository.saveArena(arena);
                    });

                    player.sendMessage("§6Standard Arena Setup:");
                    sendClickableCommand(player, "/arena setpos1 " + arenaId);
                    sendClickableCommand(player, "/arena setpos2 " + arenaId);
                    sendClickableCommand(player, "/arena setspawnred " + arenaId);
                    sendClickableCommand(player, "/arena setspawnblue " + arenaId);
                    sendClickableCommand(player, "/arena save " + arenaId);
                    sendClickableCommand(player, "/arena enable " + arenaId);
                    sendClickableCommand(player, "/arena disable " + arenaId);
                    sendClickableCommand(player, "/arena delete " + arenaId);

                    player.sendMessage("§e/kit addarena <arena> <kit>");
                }
            }
            default -> player.sendMessage("§cUnknown action argument frame.");
        }

        return true;
    }

    private static String fullMessage(Throwable throwable) {
        Throwable current = throwable instanceof CompletionException && throwable.getCause() != null
                ? throwable.getCause()
                : throwable;
        StringBuilder message = new StringBuilder(current.getClass().getSimpleName());
        if (current.getMessage() != null && !current.getMessage().isBlank()) {
            message.append(": ").append(current.getMessage());
        }
        while (current.getCause() != null) {
            current = current.getCause();
            message.append(" | caused by ").append(current.getClass().getSimpleName());
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                message.append(": ").append(current.getMessage());
            }
        }
        return message.toString();
    }

    private void giveBridgeWand(Player player) {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName("§bBridge Selector");
        stick.setItemMeta(meta);
        player.getInventory().addItem(stick);
        player.sendMessage("§eYou received the §bBridge Selector§e.");
        player.sendMessage("§7Right-click corner §b1§7, then right-click corner §b2§7.");
        player.sendMessage("§7Then run: §b/arena savebreakable <arena>");
    }


    private void scanGoals(Set<String> goals, Location center) {
        if (center == null || center.getWorld() == null) {
            return;
        }
        for (int x = -15; x <= 15; x++) {
            for (int y = -15; y <= 15; y++) {
                for (int z = -15; z <= 15; z++) {
                    Location loc = center.clone().add(x, y, z);
                    if (loc.getBlock().getType() == Material.ENDER_PORTAL) {
                        goals.add(blockKey(loc));
                    }
                }
            }
        }
    }

    private String blockKey(Location loc) {
        return loc.getWorld().getName()
                + ":" + loc.getBlockX()
                + ":" + loc.getBlockY()
                + ":" + loc.getBlockZ();
    }

    private void scanBedDefense(Arena arena, Location center) {
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Material type = loc.getBlock().getType();

                    if (type != Material.ENDER_STONE && type != Material.WOOD) {
                        continue;
                    }

                    String key = loc.getWorld().getName()
                            + ":" + loc.getBlockX()
                            + ":" + loc.getBlockY()
                            + ":" + loc.getBlockZ();

                    arena.getBreakableBlocks().add(key);
                }
            }
        }
    }

    private void giveGoalWands(Player player) {
        ItemStack red = new ItemStack(Material.STICK);
        ItemMeta redMeta = red.getItemMeta();
        redMeta.setDisplayName("§cRed Goal Wand");
        red.setItemMeta(redMeta);

        ItemStack blue = new ItemStack(Material.STICK);
        ItemMeta blueMeta = blue.getItemMeta();
        blueMeta.setDisplayName("§9Blue Goal Wand");
        blue.setItemMeta(blueMeta);

        player.getInventory().addItem(red);
        player.getInventory().addItem(blue);

        player.sendMessage("§eYou received:");
        player.sendMessage(" §cRed Goal Wand");
        player.sendMessage(" §9Blue Goal Wand");
    }

    private void giveBreakableSelectorStick(Player player) {
        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        meta.setDisplayName("§6Breakable Selector");
        stick.setItemMeta(meta);
        player.getInventory().addItem(stick);
        player.sendMessage("§eYou received the Breakable Selector.");
    }

    public static String getEditingArena(UUID uuid) {
        return EDITING_ARENAS.get(uuid);
    }
}