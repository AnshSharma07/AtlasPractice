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

package com.ansh.atlaspractice.util;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class TitleUtil {

    private TitleUtil() {}

    /**
     * Sends a title + subtitle to a player.
     *
     * @param player    target
     * @param title     large center text (null = blank)
     * @param subtitle  smaller text below (null = blank)
     * @param fadeIn    ticks to fade in
     * @param stay      ticks to stay
     * @param fadeOut   ticks to fade out
     */
    public static void send(Player player,
                            String title,
                            String subtitle,
                            int fadeIn,
                            int stay,
                            int fadeOut) {

        var connection = ((CraftPlayer) player).getHandle().playerConnection;

        // 1. Set timing
        connection.sendPacket(new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.TIMES,
                null,
                fadeIn, stay, fadeOut
        ));

        // 2. Subtitle first (must be sent before TITLE)
        if (subtitle != null) {
            connection.sendPacket(new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                    new ChatComponentText(subtitle)
            ));
        }

        // 3. Title â€” this triggers the display
        connection.sendPacket(new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.TITLE,
                new ChatComponentText(title != null ? title : "")
        ));
    }

    /** Clears any active title from the player's screen immediately. */
    public static void clear(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(
                new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR, null)
        );
    }
}