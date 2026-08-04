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

package com.ansh.atlaspractice.arena.setup;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ArenaGeometryUtil {


    /**
     * makes player face straight at other player
     */
    public static Location faceLocation(Location origin, Location target) {
        Location loc = origin.clone();
        Vector direction = target.toVector().subtract(origin.toVector()).normalize();
        loc.setDirection(direction);
        return loc;
    }

    /**
     * Calculates boundaries relative to the team's facing direction.
     * Red Back is away from Blue. Blue Back is away from Red.
     * Left is relative to their forward facing.
     */
    public static Location calculatePos1(Location redWool, Location blueWool, int buildLimit) {
        Vector forward = blueWool.toVector().subtract(redWool.toVector()).normalize();
        Vector back = forward.clone().multiply(-1);
        Vector up = new Vector(0, 1, 0);
        Vector left = up.clone().crossProduct(forward).normalize(); // Left from Red's side

        return redWool.clone()
                .add(up.clone().multiply(buildLimit))
                .add(back.clone().multiply(20))
                .add(left.clone().multiply(20));
    }
    public static Location calculatePos2(Location redWool, Location blueWool, int buildLimit) {
        Vector forward = redWool.toVector().subtract(blueWool.toVector()).normalize();
        Vector back = forward.clone().multiply(-1);
        Vector up = new Vector(0, 1, 0);
        Vector left = up.clone().crossProduct(forward).normalize(); // Left from Blue's side

        return blueWool.clone()
                .add(up.clone().multiply(-buildLimit))
                .add(back.clone().multiply(20))
                .add(left.clone().multiply(20));
    }
}