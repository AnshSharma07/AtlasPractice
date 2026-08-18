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

package com.ansh.atlaspractice.util;

import org.bukkit.entity.Player;

public final class CombatActionBar {

    private CombatActionBar() {}
    public static void showHealth(Player attacker, Player victim, double remainingHealth) {
        if (attacker == null || victim == null) {
            return;
        }

        if (!attacker.isOnline() || !victim.isOnline()) {
            return;
        }
        int hearts = (int) Math.ceil(Math.max(0.0D, remainingHealth) / 2.0D);
        StringBuilder heartBar = new StringBuilder();

        for (int i = 0; i < hearts; i++) {
            heartBar.append("§c❤ ");
        }

        String message = "§e" + victim.getName() + " §f" + heartBar.toString().trim();

        ActionBarUtil.send(attacker, message);
    }
    public static void showKill(Player killer, Player victim) {
        if (killer == null || victim == null) {
            return;
        }
        if (!killer.isOnline()) {
            return;
        }
        String message = "§c§lKILL! §e" + victim.getName();
        ActionBarUtil.send(killer, message);
    }
}