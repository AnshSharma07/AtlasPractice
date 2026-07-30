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

package com.ansh.atlaspractice.leaderboard.hologram;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Hologram {
    private final Location location;
    private final List<EntityArmorStand> lines = new ArrayList<>();
    private final double LINE_SPACING = 0.25;

    public Hologram(Location location) {
        this.location = location;
    }

    public void setLines(List<String> textLines) {
        destroy();
        WorldServer nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        
        for (int i = 0; i < textLines.size(); i++) {
            EntityArmorStand stand = new EntityArmorStand(nmsWorld);
            stand.setLocation(location.getX(), location.getY() - (i * LINE_SPACING), location.getZ(), 0, 0);
            stand.setCustomName(textLines.get(i));
            stand.setCustomNameVisible(true);
            stand.setInvisible(true);
            stand.setGravity(false);
            stand.setSmall(true);
            lines.add(stand);
        }
    }

    public void spawn(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for (EntityArmorStand stand : lines) {
            connection.sendPacket(new PacketPlayOutSpawnEntityLiving(stand));
        }
    }

    public void destroy(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for (EntityArmorStand stand : lines) {
            connection.sendPacket(new PacketPlayOutEntityDestroy(stand.getId()));
        }
    }

    public void destroy() {
        for (Player p : location.getWorld().getPlayers()) {
            destroy(p);
        }
        lines.clear();
    }
    
    public List<EntityArmorStand> getEntityLines() {
        return lines;
    }
}