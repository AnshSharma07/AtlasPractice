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

package com.ansh.atlaspractice.config;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import org.bukkit.configuration.file.FileConfiguration;

public final class ExplosionConfig {

    private final AtlasPracticePlugin plugin;

    // --- TNT ---
    private boolean tntEnabled;
    private int     tntFuseTicks;
    private double  tntRadius;
    private double  tntKnockback;
    private double  tntIntensity;
    private boolean tntBreakPlacedBlocks;
    private boolean tntBreakBreakableBlocks;
    private boolean tntBreakEndstone;
    private boolean tntBreakBeds;
    private boolean tntDamagePlayers;

    // --- Fireball ---
    private boolean fireballEnabled;
    private double  fireballRadius;
    private double  fireballKnockback;
    private double  fireballSpeed;
    private int     fireballCooldownTicks;
    private boolean fireballBreakPlacedBlocks;
    private boolean fireballBreakBreakableBlocks;
    private boolean fireballBreakEndstone;
    private boolean fireballBreakBeds;
    private boolean fireballDamagePlayers;

    // --- Fireball Bounce-back ---
    /** Whether the victim can deflect an incoming fireball with a left-click. */
    private boolean fireballBounceEnabled;
    /** Maximum distance (blocks) at which a fireball can be detected for deflection. */
    private double  fireballBounceMaxRange;
    /**
     * Maximum angle (degrees) between the player's look direction and the
     * vector pointing toward the fireball for the deflect to register.
     */
    private double  fireballBounceMaxAngle;
    /** Speed multiplier applied to the deflected fireball. */
    private double  fireballBounceSpeedMultiplier;

    public ExplosionConfig(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        this.load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        // TNT
        tntEnabled                = config.getBoolean("special.tnt.enabled",               true);
        tntFuseTicks              = config.getInt    ("special.tnt.fuseTicks",              40);
        tntRadius                 = config.getDouble ("special.tnt.radius",                 4.5);
        tntKnockback              = config.getDouble ("special.tnt.knockback",              2.8);
        tntIntensity              = config.getDouble ("special.tnt.intensity",              1.0);
        tntBreakPlacedBlocks      = config.getBoolean("special.tnt.breakPlacedBlocks",      true);
        tntBreakBreakableBlocks   = config.getBoolean("special.tnt.breakBreakableBlocks",   true);
        tntBreakEndstone          = config.getBoolean("special.tnt.breakEndstone",          true);
        tntBreakBeds              = config.getBoolean("special.tnt.breakBeds",              false);
        tntDamagePlayers          = config.getBoolean("special.tnt.damagePlayers",          false);

        // Fireball
        fireballEnabled                = config.getBoolean("special.fireball.enabled",               true);
        fireballRadius                 = config.getDouble ("special.fireball.radius",                4.0);
        fireballKnockback              = config.getDouble ("special.fireball.knockback",             3.0);
        fireballSpeed                  = config.getDouble ("special.fireball.speed",                 2.8);
        fireballCooldownTicks          = config.getInt    ("special.fireball.cooldownTicks",         8);
        fireballBreakPlacedBlocks      = config.getBoolean("special.fireball.breakPlacedBlocks",     true);
        fireballBreakBreakableBlocks   = config.getBoolean("special.fireball.breakBreakableBlocks",  false);
        fireballBreakEndstone          = config.getBoolean("special.fireball.breakEndstone",         false);
        fireballBreakBeds              = config.getBoolean("special.fireball.breakBeds",             false);
        fireballDamagePlayers          = config.getBoolean("special.fireball.damagePlayers",         false);

        // Fireball bounce
        fireballBounceEnabled         = config.getBoolean("special.fireball.bounce.enabled",             true);
        fireballBounceMaxRange        = config.getDouble ("special.fireball.bounce.max-range",           4.0);
        fireballBounceMaxAngle        = config.getDouble ("special.fireball.bounce.max-angle",           35.0);
        fireballBounceSpeedMultiplier = config.getDouble ("special.fireball.bounce.speed-multiplier",    1.0);
    }

    // --- TNT getters ---
    public boolean isTntEnabled()               { return tntEnabled; }
    public int     getTntFuseTicks()            { return tntFuseTicks; }
    public double  getTntRadius()               { return tntRadius; }
    public double  getTntKnockback()            { return tntKnockback; }
    public double  getTntIntensity()            { return tntIntensity; }
    public boolean isTntBreakPlacedBlocks()     { return tntBreakPlacedBlocks; }
    public boolean isTntBreakBreakableBlocks()  { return tntBreakBreakableBlocks; }
    public boolean isTntBreakEndstone()         { return tntBreakEndstone; }
    public boolean isTntBreakBeds()             { return tntBreakBeds; }
    public boolean isTntDamagePlayers()         { return tntDamagePlayers; }

    // --- Fireball getters ---
    public boolean isFireballEnabled()               { return fireballEnabled; }
    public double  getFireballRadius()               { return fireballRadius; }
    public double  getFireballKnockback()            { return fireballKnockback; }
    public double  getFireballSpeed()                { return fireballSpeed; }
    public int     getFireballCooldownTicks()        { return fireballCooldownTicks; }
    public boolean isFireballBreakPlacedBlocks()     { return fireballBreakPlacedBlocks; }
    public boolean isFireballBreakBreakableBlocks()  { return fireballBreakBreakableBlocks; }
    public boolean isFireballBreakEndstone()         { return fireballBreakEndstone; }
    public boolean isFireballBreakBeds()             { return fireballBreakBeds; }
    public boolean isFireballDamagePlayers()         { return fireballDamagePlayers; }

    // --- Fireball bounce getters ---
    public boolean isFireballBounceEnabled()         { return fireballBounceEnabled; }
    public double  getFireballBounceMaxRange()       { return fireballBounceMaxRange; }
    public double  getFireballBounceMaxAngle()       { return fireballBounceMaxAngle; }
    public double  getFireballBounceSpeedMultiplier(){ return fireballBounceSpeedMultiplier; }
}
