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
import com.ansh.atlaspractice.config.ExplosionConfig;
import com.ansh.atlaspractice.explosion.ExplosionType;
import com.ansh.atlaspractice.match.Match;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Optional;

/**
 * Handles fireball throwing and the fireball bounce-back mechanic.
 *
 * <h3>Throwing</h3>
 * Right-clicking a fireball item in a match fires a custom {@link Fireball}
 * tagged with {@code atlas_fireball_source}.
 *
 * <h3>Bounce-back</h3>
 * When a player LEFT-CLICKS while an opponent's fireball is within
 * {@link ExplosionConfig#getFireballBounceMaxRange()} blocks and the player is
 * looking within {@link ExplosionConfig#getFireballBounceMaxAngle()} degrees of
 * the fireball, the fireball is deflected back in the player's look direction.
 * The new shooter metadata is updated so the deflected fireball is attributed
 * to the deflecting player.
 */
public final class FireballListener implements Listener {

    private static final String META_SOURCE = "atlas_fireball_source";

    private final AtlasPracticePlugin plugin;

    public FireballListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onFireballThrow(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.FIREBALL) {
            return;
        }

        Player player = event.getPlayer();
        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
        if (matchOpt.isEmpty()) {
            return;
        }

        if (!plugin.getExplosionConfig().isFireballEnabled()) {
            return;
        }

        event.setCancelled(true);

        if (plugin.getCooldownManager().hasFireballCooldown(player.getUniqueId())) {
            long remaining = plugin.getCooldownManager().getRemainingCooldownMillis(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "You must wait "
                    + String.format("%.1f", remaining / 1000.0)
                    + " seconds before doing that again.");
            return;
        }

        // Consume one fireball from the stack.
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }

        launchFireball(player, player.getLocation().getDirection(),
                plugin.getExplosionConfig().getFireballSpeed());

        plugin.getCooldownManager().setFireballCooldown(
                player.getUniqueId(),
                plugin.getExplosionConfig().getFireballCooldownTicks());
    }


    /**
     * Left-click to deflect an incoming fireball that was shot by an opponent.
     *
     * <p>Conditions:
     * <ol>
     *   <li>Bounce-back is enabled in config.</li>
     *   <li>The player is in a match.</li>
     *   <li>There is an {@code atlas_fireball_source}-tagged fireball within
     *       {@code max-range} blocks that was NOT shot by this player.</li>
     *   <li>The angle between the player's look direction and the vector toward
     *       the fireball is within {@code max-angle} degrees.</li>
     * </ol>
     * When triggered, the fireball's velocity is set to the player's look
     * direction multiplied by the configured speed, and the shooter metadata is
     * updated to this player so damage/knockback is attributed correctly.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFireballBounce(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ExplosionConfig cfg = plugin.getExplosionConfig();

        if (!cfg.isFireballBounceEnabled()) {
            return;
        }

        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());
        if (matchOpt.isEmpty()) {
            return;
        }

        double maxRange = cfg.getFireballBounceMaxRange();
        double maxAngleDeg = cfg.getFireballBounceMaxAngle();

        Fireball target = findBounceable(player, maxRange, maxAngleDeg);
        if (target == null) {
            return;
        }

        // Redirect the fireball.
        double speed = cfg.getFireballSpeed() * cfg.getFireballBounceSpeedMultiplier();
        Vector newDirection = player.getLocation().getDirection().normalize();

        target.setVelocity(newDirection.clone().multiply(speed));
        target.setShooter(player);
        target.setMetadata(META_SOURCE,
                new FixedMetadataValue(plugin, player.getUniqueId().toString()));

        event.setCancelled(true);

        player.sendMessage(ChatColor.YELLOW + "Fireball deflected!");
    }

    /**
     * Finds the nearest fireball within {@code maxRange} blocks that:
     * <ul>
     *   <li>has the {@code atlas_fireball_source} metadata (is an atlas fireball),</li>
     *   <li>was NOT shot by {@code player},</li>
     *   <li>is within {@code maxAngleDeg} degrees of the player's look direction.</li>
     * </ul>
     * Returns {@code null} if no such fireball exists.
     */
    private Fireball findBounceable(Player player, double maxRange, double maxAngleDeg) {
        Collection<org.bukkit.entity.Entity> nearby =
                player.getWorld().getNearbyEntities(player.getLocation(), maxRange, maxRange, maxRange);

        Vector look = player.getLocation().getDirection().normalize();
        Fireball best = null;
        double bestAngle = maxAngleDeg;

        for (org.bukkit.entity.Entity entity : nearby) {
            if (!(entity instanceof Fireball)) continue;
            Fireball fb = (Fireball) entity;

            if (!fb.hasMetadata(META_SOURCE)) continue;

            // Skip fireballs shot by this player (can't bounce your own).
            String shooterUuid = fb.getMetadata(META_SOURCE).get(0).asString();
            if (shooterUuid.equals(player.getUniqueId().toString())) continue;

            // Angle between player look direction and vector to fireball.
            Vector toFb = fb.getLocation().toVector()
                    .subtract(player.getEyeLocation().toVector())
                    .normalize();
            double angle = Math.toDegrees(look.angle(toFb));

            if (angle < bestAngle) {
                bestAngle = angle;
                best = fb;
            }
        }

        return best;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntityType() == EntityType.FIREBALL
                && event.getEntity().hasMetadata(META_SOURCE)) {
            event.setRadius(0F);
            event.setFire(false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.getEntityType() == EntityType.FIREBALL
                && event.getEntity().hasMetadata(META_SOURCE)) {
            event.blockList().clear();
            event.setCancelled(true);
            plugin.getExplosionManager().createCustomExplosion(
                    event.getLocation(),
                    ExplosionType.FIREBALL,
                    event.getEntity()
            );
        }
    }


    private Fireball launchFireball(Player shooter, Vector direction, double speed) {
        Fireball fireball = (Fireball) shooter.getWorld().spawnEntity(
                shooter.getEyeLocation().add(direction.clone().multiply(1.0)),
                EntityType.FIREBALL
        );
        fireball.setShooter(shooter);
        fireball.setVelocity(direction.normalize().multiply(speed));
        fireball.setMetadata(META_SOURCE,
                new FixedMetadataValue(plugin, shooter.getUniqueId().toString()));
        return fireball;
    }
}
