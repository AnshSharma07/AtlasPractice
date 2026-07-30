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

/**
 * Handles custom explosions for TNT and Fireball with Minemen-style knockback.
 *
 * <h3>Knockback model (Minemen-style)</h3>
 * <ul>
 *   <li>Blast force is computed as {@code (1 - dist/radius) ^ 0.5}, giving a
 *       steeper fall-off than vanilla (which uses {@code ^ 0.55}).</li>
 *   <li>The blast vector is <em>added</em> to the player's current velocity
 *       rather than being mixed in at a dampened ratio.  This preserves
 *       momentum so players can stack velocity with consecutive hits.</li>
 *   <li>TNT: strong horizontal push + firm vertical lift.</li>
 *   <li>Fireball: slightly stronger outward force + sharper upward kick;
 *       self-boost retained for the shooter.</li>
 * </ul>
 */
public final class ExplosionManager {

    private final AtlasPracticePlugin plugin;

    public ExplosionManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public void createCustomExplosion(Location loc, ExplosionType type, Entity source) {
        ExplosionConfig cfg = plugin.getExplosionConfig();

        double radius         = type == ExplosionType.TNT ? cfg.getTntRadius()    : cfg.getFireballRadius();
        double knockback      = type == ExplosionType.TNT ? cfg.getTntKnockback() : cfg.getFireballKnockback();
        boolean breakPlaced   = type == ExplosionType.TNT ? cfg.isTntBreakPlacedBlocks()    : cfg.isFireballBreakPlacedBlocks();
        boolean breakBreakable= type == ExplosionType.TNT ? cfg.isTntBreakBreakableBlocks() : cfg.isFireballBreakBreakableBlocks();
        boolean breakEndstone = type == ExplosionType.TNT ? cfg.isTntBreakEndstone()        : cfg.isFireballBreakEndstone();
        boolean breakBeds     = type == ExplosionType.TNT ? cfg.isTntBreakBeds()            : cfg.isFireballBreakBeds();

        // Visual / audio
        loc.getWorld().playEffect(loc, Effect.EXPLOSION_LARGE, 1);
        loc.getWorld().playSound(loc, Sound.EXPLODE, 1.0F, 1.0F);

        // Resolve the match this explosion belongs to.
        Optional<Match> matchOpt = resolveMatch(loc, source);
        if (matchOpt.isEmpty()) {
            return;
        }

        Match match = matchOpt.get();
        Arena arena = match.getArena();

        // --- Block destruction ---
        applyBlockDamage(loc, radius, match, arena,
                breakPlaced, breakBreakable, breakEndstone, breakBeds);

        // --- Player knockback (Minemen-style) ---
        applyKnockback(loc, radius, knockback, type, source, match);
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /** Finds the {@link Match} associated with the explosion source. */
    private Optional<Match> resolveMatch(Location loc, Entity source) {
        if (source instanceof Player) {
            return plugin.getMatchManager().getMatchByPlayer(source.getUniqueId());
        }
        // For entity-based explosions (e.g., TNT placed by a player then
        // detonated without a direct player reference), scan nearby players.
        for (Player player : loc.getWorld().getPlayers()) {
            Optional<Match> possible = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
            if (possible.isPresent()) {
                Match m = possible.get();
                if (m.getArena() != null && isWithinArenaBoundaries(loc, m.getArena())) {
                    return possible;
                }
            }
        }
        return Optional.empty();
    }

    private void applyBlockDamage(
            Location loc, double radius, Match match, Arena arena,
            boolean breakPlaced, boolean breakBreakable,
            boolean breakEndstone, boolean breakBeds) {

        int blockRadius = (int) Math.ceil(radius);
        List<Block> toDestroy = new ArrayList<>();

        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int y = -blockRadius; y <= blockRadius; y++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    Location target = loc.clone().add(x, y, z);
                    if (loc.distance(target) > radius) continue;

                    Block block = target.getBlock();
                    if (block.getType() == Material.AIR) continue;
                    if (block.getType() == Material.BED_BLOCK && !breakBeds) continue;

                    String blockKey = block.getLocation().getBlockX() + ":"
                            + block.getLocation().getBlockY() + ":"
                            + block.getLocation().getBlockZ();

                    boolean isPlayerPlaced = match.getPlacedBlocks()
                            .contains(arena.getId().toLowerCase() + ":" + blockKey)
                            || match.getPlacedBlocks().contains(blockKey);

                    boolean isArenaBreakable = arena.getBreakableBlocks()
                            .contains(block.getType().name() + ":" + block.getData())
                            || arena.getBreakableBlocks().contains(block.getType().name());

                    if (block.getType() == Material.ENDER_STONE) {
                        if (breakEndstone) toDestroy.add(block);
                    } else if (isArenaBreakable) {
                        if (breakBreakable) toDestroy.add(block);
                    } else if (isPlayerPlaced) {
                        if (breakPlaced) toDestroy.add(block);
                    }
                }
            }
        }

        for (Block b : toDestroy) {
            b.setType(Material.AIR);
        }
    }

    /**
     * Applies Minemen-style knockback to all players in the match within range.
     *
     * <p>Key differences from vanilla:
     * <ul>
     *   <li>Force curve: {@code (1 - dist/radius)^0.5} — snappier drop-off.</li>
     *   <li>Blast vector is <em>added</em> to existing velocity, not mixed in
     *       at a dampened ratio, so momentum accumulates naturally.</li>
     *   <li>Vertical component is stronger and more consistent across distances.</li>
     * </ul>
     */
    private void applyKnockback(Location loc, double radius, double knockback,
                                ExplosionType type, Entity source, Match match) {

        double radiusSq = radius * radius;
        boolean isTnt = type == ExplosionType.TNT;

        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (!(entity instanceof Player)) continue;

            Player player = (Player) entity;
            Optional<Match> pMatch = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
            if (pMatch.isEmpty() || pMatch.get() != match) continue;

            double distSq = player.getLocation().distanceSquared(loc);
            if (distSq > radiusSq) continue;

            double dist  = Math.sqrt(distSq);

            // Minemen-style force curve: steeper near the centre.
            double force = Math.pow(1.0 - (dist / radius), 0.50);
            if (force <= 0.0) continue;

            // Direction from explosion to player.
            Vector delta = player.getLocation().toVector().subtract(loc.toVector());
            if (delta.lengthSquared() < 0.0001) {
                delta = new Vector(0.0, 1.0, 0.0);
            }
            delta.normalize();

            double horizontal;
            double vertical;

            if (isTnt) {
                // TNT: strong outward push, firm consistent lift.
                horizontal = knockback * force * 1.10;
                vertical   = 0.50 + (force * 0.60);
            } else {
                // Fireball: slightly more outward + snappier vertical.
                horizontal = knockback * force * 1.20;
                vertical   = 0.48 + (force * 0.65);

                // Self-boost for the shooter (reward good fireball aim).
                if (source instanceof Player
                        && ((Player) source).getUniqueId().equals(player.getUniqueId())) {
                    horizontal *= 1.18;
                    vertical   *= 1.10;
                }
            }

            // Build blast vector.
            Vector blast = delta.clone().multiply(horizontal);
            blast.setY(vertical);

            // Add to current velocity — Minemen style (no dampening).
            Vector current = player.getVelocity();
            current.add(blast);

            // Reasonable vertical cap to prevent absurd heights.
            if (current.getY() > 3.0) {
                current.setY(3.0);
            }

            player.setVelocity(current);
        }
    }

    private boolean isWithinArenaBoundaries(Location loc, Arena arena) {
        Location min = arena.getMinimumBoundary();
        Location max = arena.getMaximumBoundary();
        if (min == null || max == null) return true;

        int minX = Math.min(min.getBlockX(), max.getBlockX());
        int maxX = Math.max(min.getBlockX(), max.getBlockX());
        int minY = Math.min(min.getBlockY(), max.getBlockY());
        int maxY = Math.max(min.getBlockY(), max.getBlockY());
        int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
        int maxZ = Math.max(min.getBlockZ(), max.getBlockZ());

        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
