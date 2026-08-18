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

package com.ansh.atlaspractice.explosion;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.config.ExplosionConfig;
import com.ansh.atlaspractice.match.Match;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ExplosionManager {

    private final AtlasPracticePlugin plugin;

    public ExplosionManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void createCustomExplosion(Location location, ExplosionType type, Entity source) {
        ExplosionConfig config = plugin.getExplosionConfig();

        boolean isTnt = type == ExplosionType.TNT;

        double radius = isTnt
                ? config.getTntRadius()
                : config.getFireballRadius();

        double knockback = isTnt
                ? config.getTntKnockback()
                : config.getFireballKnockback();

        boolean breakPlacedBlocks = isTnt
                ? config.isTntBreakPlacedBlocks()
                : config.isFireballBreakPlacedBlocks();

        boolean breakBreakableBlocks = isTnt
                ? config.isTntBreakBreakableBlocks()
                : config.isFireballBreakBreakableBlocks();

        boolean breakEndstone = isTnt
                ? config.isTntBreakEndstone()
                : config.isFireballBreakEndstone();

        boolean breakBeds = isTnt
                ? config.isTntBreakBeds()
                : config.isFireballBreakBeds();

        // Show the explosion effect.
        location.getWorld().playEffect(location, Effect.EXPLOSION_LARGE, 1);
        location.getWorld().playSound(location, Sound.EXPLODE, 1.0F, 1.0F);

        Optional<Match> matchOptional = findMatch(location, source);
        if (matchOptional.isEmpty()) {
            return;
        }

        Match match = matchOptional.get();
        Arena arena = match.getArena();

        destroyBlocks(
                location,
                radius,
                match,
                arena,
                breakPlacedBlocks,
                breakBreakableBlocks,
                breakEndstone,
                breakBeds
        );

        applyKnockback(
                location,
                radius,
                knockback,
                type,
                source,
                match
        );
    }

    private Optional<Match> findMatch(Location location, Entity source) {
        // If a player caused the explosion, use their current match.
        if (source instanceof Player) {
            return plugin.getMatchManager()
                    .getMatchByPlayer(source.getUniqueId());
        }

        // For TNT and other explosions without a player source,
        // find a nearby player who belongs to an arena match.
        for (Player player : location.getWorld().getPlayers()) {
            Optional<Match> matchOptional =
                    plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());

            if (matchOptional.isEmpty()) {
                continue;
            }

            Match match = matchOptional.get();

            if (match.getArena() == null) {
                continue;
            }

            if (isInsideArena(location, match.getArena())) {
                return matchOptional;
            }
        }

        return Optional.empty();
    }

    private void destroyBlocks(
            Location location,
            double radius,
            Match match,
            Arena arena,
            boolean breakPlacedBlocks,
            boolean breakBreakableBlocks,
            boolean breakEndstone,
            boolean breakBeds
    ) {
        int blockRadius = (int) Math.ceil(radius);
        List<Block> blocksToBreak = new ArrayList<>();

        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int y = -blockRadius; y <= blockRadius; y++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {

                    Location blockLocation = location.clone().add(x, y, z);

                    if (location.distance(blockLocation) > radius) {
                        continue;
                    }

                    Block block = blockLocation.getBlock();

                    if (block.getType() == Material.AIR) {
                        continue;
                    }

                    if (block.getType() == Material.BED_BLOCK && !breakBeds) {
                        continue;
                    }

                    String blockKey =
                            block.getLocation().getBlockX() + ":"
                                    + block.getLocation().getBlockY() + ":"
                                    + block.getLocation().getBlockZ();

                    String arenaBlockKey =
                            arena.getId().toLowerCase() + ":" + blockKey;

                    boolean playerPlaced =
                            match.getPlacedBlocks().contains(blockKey)
                                    || match.getPlacedBlocks().contains(arenaBlockKey);

                    boolean arenaBreakable =
                            arena.getBreakableBlocks().contains(
                                    block.getType().name() + ":" + block.getData()
                            )
                                    || arena.getBreakableBlocks().contains(
                                    block.getType().name()
                            );

                    if (block.getType() == Material.ENDER_STONE) {
                        if (breakEndstone) {
                            blocksToBreak.add(block);
                        }
                    } else if (arenaBreakable) {
                        if (breakBreakableBlocks) {
                            blocksToBreak.add(block);
                        }
                    } else if (playerPlaced) {
                        if (breakPlacedBlocks) {
                            blocksToBreak.add(block);
                        }
                    }
                }
            }
        }

        for (Block block : blocksToBreak) {
            block.setType(Material.AIR);
        }
    }

    private void applyKnockback(
            Location location,
            double radius,
            double knockback,
            ExplosionType type,
            Entity source,
            Match match
    ) {
        boolean isTnt = type == ExplosionType.TNT;
        double radiusSquared = radius * radius;

        for (Entity entity : location.getWorld()
                .getNearbyEntities(location, radius, radius, radius)) {

            if (!(entity instanceof Player)) {
                continue;
            }

            Player player = (Player) entity;

            Optional<Match> playerMatch =
                    plugin.getMatchManager()
                            .getMatchByPlayer(player.getUniqueId());

            if (playerMatch.isEmpty() || playerMatch.get() != match) {
                continue;
            }

            double distanceSquared =
                    player.getLocation().distanceSquared(location);

            if (distanceSquared > radiusSquared) {
                continue;
            }

            double distance = Math.sqrt(distanceSquared);

            // Players closer to the explosion get more kb.
            double force = Math.pow(1.0 - (distance / radius), 0.5);

            if (force <= 0.0) {
                continue;
            }

            Vector direction =
                    player.getLocation().toVector()
                            .subtract(location.toVector());

            if (direction.lengthSquared() < 0.0001) {
                direction = new Vector(0, 1, 0);
            }

            direction.normalize();

            double horizontal;
            double vertical;

            if (isTnt) {
                horizontal = knockback * force * 1.10;
                vertical = 0.50 + (force * 0.60);
            } else {
                horizontal = knockback * force * 1.20;
                vertical = 0.48 + (force * 0.65);

                // Fireball jumps are slightly stronger for the shooter.
                if (source instanceof Player
                        && source.getUniqueId().equals(player.getUniqueId())) {
                    horizontal *= 1.18;
                    vertical *= 1.10;
                }
            }

            Vector velocity = direction.clone().multiply(horizontal);
            velocity.setY(vertical);

            Vector currentVelocity = player.getVelocity();
            currentVelocity.add(velocity);

            // Stop explosions from launching players too high.
            if (currentVelocity.getY() > 3.0) {
                currentVelocity.setY(3.0);
            }

            player.setVelocity(currentVelocity);
        }
    }

    private boolean isInsideArena(Location location, Arena arena) {
        Location minimum = arena.getMinimumBoundary();
        Location maximum = arena.getMaximumBoundary();

        if (minimum == null || maximum == null) {
            return true;
        }

        int minX = Math.min(minimum.getBlockX(), maximum.getBlockX());
        int maxX = Math.max(minimum.getBlockX(), maximum.getBlockX());

        int minY = Math.min(minimum.getBlockY(), maximum.getBlockY());
        int maxY = Math.max(minimum.getBlockY(), maximum.getBlockY());

        int minZ = Math.min(minimum.getBlockZ(), maximum.getBlockZ());
        int maxZ = Math.max(minimum.getBlockZ(), maximum.getBlockZ());

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }
}