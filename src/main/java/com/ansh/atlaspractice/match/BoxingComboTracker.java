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

package com.ansh.atlaspractice.match;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BoxingComboTracker {

    private final Map<UUID, Integer> combos = new HashMap<>();

    /**
     * Increases the combo of the attacker.
     */
    public int addCombo(UUID attacker) {
        int combo = combos.getOrDefault(attacker, 0) + 1;
        combos.put(attacker, combo);
        return combo;
    }

    /**
     * Called when someone gets hit.
     * Their combo immediately resets.
     */
    public void resetCombo(UUID player) {
        combos.remove(player);
    }

    /**
     * Returns current combo.
     */
    public int getCombo(UUID player) {
        return combos.getOrDefault(player, 0);
    }

    /**
     * Clears every combo when a round starts.
     */
    public void clear() {
        combos.clear();
    }

}