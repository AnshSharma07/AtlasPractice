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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaState;
import com.ansh.atlaspractice.commands.ArenaAdminCommand;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Optional;

public final class ArenaBreakableListener implements Listener {

    private final AtlasPracticePlugin plugin;
    private static final Map<UUID, Location> BRIDGE_POS1 = new HashMap<>();
    private static final Map<UUID, Location> BRIDGE_POS2 = new HashMap<>();

    public static Location getBridgePos1(UUID uuid) { return BRIDGE_POS1.get(uuid); }
    public static Location getBridgePos2(UUID uuid) { return BRIDGE_POS2.get(uuid); }
    public static void clearBridgeSelection(UUID uuid) {
        BRIDGE_POS1.remove(uuid);
        BRIDGE_POS2.remove(uuid);
    }
    private boolean isInside(Location loc, Location min, Location max) {

        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());
        return loc.getBlockX() >= minX
                && loc.getBlockX() <= maxX
                && loc.getBlockY() >= minY
                && loc.getBlockY() <= maxY
                && loc.getBlockZ() >= minZ
                && loc.getBlockZ() <= maxZ;
    }
    public ArenaBreakableListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        if (item.getType() != Material.STICK) {
            return;
        }
        if (!item.hasItemMeta()) {
            return;
        }
        if (!item.getItemMeta().hasDisplayName()) {
            return;
        }
        String displayName = item.getItemMeta().getDisplayName();
        Arena arena = null;

        if (displayName.equals("§cRed Goal Wand")
                || displayName.equals("§9Blue Goal Wand")
                || displayName.equals("§6Breakable Selector")
                || displayName.equals("§bBridge Selector")) {

            String arenaId = ArenaAdminCommand.getEditingArena(player.getUniqueId());

            if (arenaId == null) {
                player.sendMessage("§cYou are not editing an arena.");
                return;
            }

            Optional<Arena> arenaOpt = plugin.getArenaManager().getArena(arenaId);

            if (!arenaOpt.isPresent()) {
                player.sendMessage("§cArena not found.");
                return;
            }

            arena = arenaOpt.get();
        }
        if (displayName.equals("§cRed Goal Wand")) {

            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            event.setCancelled(true);

            Block target = event.getClickedBlock();
            if (target == null) {
                return;
            }

            String key =
                    target.getWorld().getName()
                            + ":"
                            + target.getX()
                            + ":"
                            + target.getY()
                            + ":"
                            + target.getZ();

            if (arena.getRedGoalBlocks().contains(key)) {

                arena.getRedGoalBlocks().remove(key);
                target.setType(Material.AIR);

                plugin.getArenaRepository().saveArena(arena);

                player.sendMessage("§cRemoved Red Goal.");

            } else {

                target.setType(Material.ENDER_PORTAL);

                arena.getRedGoalBlocks().add(key);

                plugin.getArenaRepository().saveArena(arena);

                player.sendMessage("§aAdded Red Goal.");
            }

            return;
        }

        if (displayName.equals("§9Blue Goal Wand")) {

            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }

            event.setCancelled(true);

            Block target = event.getClickedBlock();
            if (target == null) {
                return;
            }

            String key =
                    target.getWorld().getName()
                            + ":"
                            + target.getX()
                            + ":"
                            + target.getY()
                            + ":"
                            + target.getZ();

            if (arena.getBlueGoalBlocks().contains(key)) {

                arena.getBlueGoalBlocks().remove(key);
                target.setType(Material.AIR);

                plugin.getArenaRepository().saveArena(arena);

                player.sendMessage("§9Removed Blue Goal.");

            } else {

                target.setType(Material.ENDER_PORTAL);

                arena.getBlueGoalBlocks().add(key);

                plugin.getArenaRepository().saveArena(arena);

                player.sendMessage("§aAdded Blue Goal.");
            }

            return;
        }
        if (item.getItemMeta().getDisplayName().equals("§bBridge Selector")) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                return;
            }
            event.setCancelled(true);
            Block target = event.getClickedBlock();
            if (target == null) {
                return;
            }
            UUID uuid = player.getUniqueId();
            if (!BRIDGE_POS1.containsKey(uuid)) {
                BRIDGE_POS1.put(uuid, target.getLocation());
                player.sendMessage("§bCorner 1 set at §f" + target.getX() + ", " + target.getY() + ", " + target.getZ());
                player.sendMessage("§7Right-click the second corner.");
            } else {
                BRIDGE_POS2.put(uuid, target.getLocation());
                player.sendMessage("§bCorner 2 set at §f" + target.getX() + ", " + target.getY() + ", " + target.getZ());
                player.sendMessage("§7Run §b/arena savebreakable <arena>§7 to save.");
            }
            return;
        }
        if (!item.getItemMeta().getDisplayName()
                .equals("§6Breakable Selector")) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR
                && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        event.setCancelled(true);


        if (arena.getState() != ArenaState.DISABLED) {
            player.sendMessage(
                    "§cDisable the arena before editing breakable blocks."
            );
            return;
        }

        Block target = player.getTargetBlock((HashSet<Byte>) null, 5);


        if (target == null
                || target.getType() == Material.AIR) {

            player.sendMessage("§cLook at a valid block.");
            return;
        }

        if (target.getType() == Material.BEDROCK) {
            player.sendMessage("§cYou cannot select bedrock.");
            return;
        }

        Location min = arena.getMinimumBoundary();
        Location max = arena.getMaximumBoundary();

        if (min == null || max == null) {
            player.sendMessage("§cSet pos1 and pos2 first.");
            return;
        }

        if (!isInside(target.getLocation(), min, max)) {
            player.sendMessage("§cBreakable blocks must be inside arena boundaries!");
            return;
        }

        if (!target.getWorld().equals(min.getWorld())) {
            player.sendMessage(
                    "§cThat block is outside the arena pos."
            );
            return;
        }

        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());

        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());

        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

        int x = target.getX();
        int y = target.getY();
        int z = target.getZ();

        if (x < minX || x > maxX
                || y < minY || y > maxY
                || z < minZ || z > maxZ) {

            player.sendMessage(
                    "§cThat block is outside the arena pos."
            );

            return;
        }

        String key =
                target.getWorld().getName()
                        + ":"
                        + target.getX()
                        + ":"
                        + target.getY()
                        + ":"
                        + target.getZ();

        if (arena.getBreakableBlocks().contains(key)) {

            arena.getBreakableBlocks().remove(key);

            plugin.getArenaRepository().saveArena(arena);

            player.sendMessage(
                    "§cRemoved "
                            + target.getType().name()
                            + " from breakable blocks ("
                            + arena.getBreakableBlocks().size()
                            + "/52)"
            );

            return;
        }

        if (arena.getBreakableBlocks().size() >= 52) {

            player.sendMessage(
                    "§cMaximum 52 breakable blocks."
            );

            return;
        }

        arena.getBreakableBlocks().add(key);

        plugin.getArenaRepository().saveArena(arena);

        player.sendMessage(
                "§aAdded "
                        + target.getType().name()
                        + " to breakable blocks ("
                        + arena.getBreakableBlocks().size()
                        + "/52)"
        );

        if (arena.getBreakableBlocks().size() == 1) {

            player.sendMessage("§eLooks like you're creating a BedFight or Fireball Fight arena.");
            player.sendMessage("§7Remember to configure beds if needed.");
        }
    }
}