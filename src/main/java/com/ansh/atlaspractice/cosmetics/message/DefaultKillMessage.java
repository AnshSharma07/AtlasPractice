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

package com.ansh.atlaspractice.cosmetics.message;

import com.ansh.atlaspractice.cosmetics.CosmeticType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class DefaultKillMessage implements KillMessage {

    public static final DefaultKillMessage INSTANCE = new DefaultKillMessage();

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public String getDisplayName() {
        return "Default";
    }

    public String getPermission() {
        return "";
    }

    @Override
    public CosmeticType getType() {
        return CosmeticType.KILL_MESSAGE;
    }

    @Override
    public Material getIcon() {
        return Material.BARRIER;
    }

    @Override
    public int getPrice() {
        return 0;
    }

    @Override
    public String format(Player killer, Player victim, KillMessageType type) {
        return ChatColor.translateAlternateColorCodes('&', "&a" + killer.getName() + " &ekilled &c" + victim.getName() + "&e.");
    }
}