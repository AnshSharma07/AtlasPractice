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

package com.ansh.atlaspractice.cosmetics;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;

public final class ParticleCosmeticUtil {

    private ParticleCosmeticUtil() {
    }

    public static void play(Location location, Effect effect, int amount) {
        if (location == null || location.getWorld() == null || effect == null) {
            return;
        }
        World world = location.getWorld();
        for (int i = 0; i < amount; i++) {
            world.playEffect(location, effect, 0);
        }
    }

    public static void circle(Location center, Effect effect, double radius, int points) {
        if (center == null || center.getWorld() == null || effect == null) {
            return;
        }
        World world = center.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            Location location = center.clone().add(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
            world.playEffect(location, effect, 0);
        }
    }

    public static void spiral(Location base, Effect effect, double radius, int points) {
        if (base == null || base.getWorld() == null || effect == null) {
            return;
        }
        World world = base.getWorld();
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            Location location = base.clone().add(Math.cos(angle) * radius, 0.15D * i, Math.sin(angle) * radius);
            world.playEffect(location, effect, 0);
        }
    }

    public static void sound(Location location, Sound sound, float volume, float pitch) {
        if (location != null && location.getWorld() != null && sound != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }
}
