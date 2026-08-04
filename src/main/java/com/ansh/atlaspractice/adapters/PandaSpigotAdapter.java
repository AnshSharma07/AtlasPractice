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

package com.ansh.atlaspractice.adapters;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.lang.reflect.Method;
import java.util.logging.Level;


public final class PandaSpigotAdapter {

    private static boolean pandaSpigotDetected = false;
    private static Method setKbProfileMethod = null;

    static {
        try {
            // Reflective lookups targeting modern PandaSpigot player interface additions
            Class<?> pandaPlayerClass = Class.forName("org.github.paperspigot.PaperSpigotConfig"); // Base structural check
            setKbProfileMethod = Player.class.getMethod("setKnockbackProfile", String.class);
            pandaSpigotDetected = true;
            Bukkit.getLogger().info("[AtlasPractice] Successfully hooked into PandaSpigot API for custom Knockback scaling.");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            Bukkit.getLogger().warning("[AtlasPractice] PandaSpigot extensions not found. Falling back to default Spigot physics mechanics.");
        }
    }

    
    public void applyKnockbackProfile(Player player, String profileName) {
        if (!pandaSpigotDetected || setKbProfileMethod == null) return;

        try {
            setKbProfileMethod.invoke(player, profileName);
        } catch (Exception exception) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to apply dynamic PandaSpigot knockback modification frame: " + profileName, exception);
        }
    }
}
