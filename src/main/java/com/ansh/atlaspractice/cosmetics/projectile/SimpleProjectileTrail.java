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

package com.ansh.atlaspractice.cosmetics.projectile;

import com.ansh.atlaspractice.cosmetics.ParticleCosmeticUtil;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Projectile;

public class SimpleProjectileTrail extends ProjectileTrail {

    private final Effect effect;
    private final int amount;

    public SimpleProjectileTrail(String id, String displayName, Material icon, Effect effect, int amount) {
        super(id, displayName, icon);
        this.effect = effect;
        this.amount = amount;
    }

    @Override
    public void play(Projectile projectile) {
        ParticleCosmeticUtil.play(projectile.getLocation(), effect, amount);
    }
}
