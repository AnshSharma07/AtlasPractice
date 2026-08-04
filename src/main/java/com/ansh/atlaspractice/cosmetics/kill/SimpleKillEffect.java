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

package com.ansh.atlaspractice.cosmetics.kill;

import com.ansh.atlaspractice.cosmetics.ParticleCosmeticUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SimpleKillEffect extends KillEffect {

    private final Effect effect;
    private final Sound sound;
    private final int amount;
    private final boolean lightning;

    public SimpleKillEffect(String id, String displayName, Material icon, Effect effect, Sound sound, int amount, boolean lightning) {
        super(id, displayName, icon);
        this.effect = effect;
        this.sound = sound;
        this.amount = amount;
        this.lightning = lightning;
    }

    @Override
    public void play(Player killer, Location location) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        if (lightning) {
            location.getWorld().strikeLightningEffect(location);
        }
        ParticleCosmeticUtil.play(location.clone().add(0.0D, 1.0D, 0.0D), effect, amount);
        ParticleCosmeticUtil.sound(location, sound, 1.0F, 1.0F);
    }
}
