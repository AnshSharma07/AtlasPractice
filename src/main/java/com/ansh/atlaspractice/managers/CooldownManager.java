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

package com.ansh.atlaspractice.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CooldownManager {

    private final Map<UUID, Long> fireballCooldowns = new HashMap<>();

    public void setFireballCooldown(UUID uuid, int ticks) {
        long expireTime = System.currentTimeMillis() + (ticks * 50L);
        this.fireballCooldowns.put(uuid, expireTime);
    }

    public boolean hasFireballCooldown(UUID uuid) {
        if (!this.fireballCooldowns.containsKey(uuid)) {
            return false;
        }
        if (System.currentTimeMillis() > this.fireballCooldowns.get(uuid)) {
            this.fireballCooldowns.remove(uuid);
            return false;
        }
        return true;
    }

    public long getRemainingCooldownMillis(UUID uuid) {
        if (!this.fireballCooldowns.containsKey(uuid)) {
            return 0L;
        }
        long remaining = this.fireballCooldowns.get(uuid) - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }
}