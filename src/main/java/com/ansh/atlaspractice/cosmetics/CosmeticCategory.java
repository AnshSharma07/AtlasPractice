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

package com.ansh.atlaspractice.cosmetics;

import org.bukkit.Material;

public enum CosmeticCategory {

    KILL_EFFECTS("Kill Effects", Material.DIAMOND_SWORD),

    VICTORY_EFFECTS("Victory Effects", Material.FIREWORK),

    PROJECTILE_TRAILS("Projectile Trails", Material.BOW),

    WALKING_TRAILS("Walking Trails", Material.LEATHER_BOOTS),

    AURAS("Auras", Material.NETHER_STAR),

    CHAT_COLORS("Chat Colors", Material.NAME_TAG),

    KILL_MESSAGES("Kill Messages", Material.PAPER);

    private final String displayName;
    private final Material icon;

    CosmeticCategory(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }
}