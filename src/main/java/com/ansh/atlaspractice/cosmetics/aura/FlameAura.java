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

package com.ansh.atlaspractice.cosmetics.aura;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FlameAura extends AuraEffect {

    private static final double RADIUS = 0.75;
    private static final int POINTS = 5;
    private static final double ROTATION_SPEED = 0.25;

    public FlameAura() {
        super("flame", "Flame Ring", Material.BLAZE_POWDER);
    }

    @Override
    public void play(Player player) {

        Location center = player.getLocation().clone().add(0, 0.15, 0);

        long tick = System.currentTimeMillis() / 50L;
        double rotation = tick * ROTATION_SPEED;

        for (int i = 0; i < POINTS; i++) {

            double angle = rotation + ((Math.PI * 2D) / POINTS) * i;

            double x = Math.cos(angle) * RADIUS;
            double z = Math.sin(angle) * RADIUS;

            Location loc = center.clone().add(x, 0.05, z);

            player.getWorld().playEffect(
                    loc,
                    Effect.MOBSPAWNER_FLAMES,
                    0
            );
        }
    }
}