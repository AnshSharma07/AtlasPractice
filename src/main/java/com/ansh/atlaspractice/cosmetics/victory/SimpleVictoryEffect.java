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

package com.ansh.atlaspractice.cosmetics.victory;

import com.ansh.atlaspractice.cosmetics.ParticleCosmeticUtil;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class SimpleVictoryEffect extends VictoryEffect {

    private final Effect effect;
    private final Sound sound;
    private final boolean firework;
    private final boolean lightning;
    private final boolean spiral;

    public SimpleVictoryEffect(String id, String displayName, Material icon, Effect effect, Sound sound, boolean firework, boolean lightning, boolean spiral) {
        super(id, displayName, icon);
        this.effect = effect;
        this.sound = sound;
        this.firework = firework;
        this.lightning = lightning;
        this.spiral = spiral;
    }

    @Override
    public void play(Player winner) {
        Location location = winner.getLocation();
        if (lightning) {
            winner.getWorld().strikeLightningEffect(location);
        }
        if (firework) {
            Firework fw = winner.getWorld().spawn(location, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().withColor(Color.AQUA).withColor(Color.WHITE).trail(true).flicker(true).build());
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        }
        if (spiral) {
            ParticleCosmeticUtil.spiral(location, effect, 1.0D, 16);
        } else {
            ParticleCosmeticUtil.circle(location.clone().add(0.0D, 0.3D, 0.0D), effect, 1.3D, 18);
        }
        ParticleCosmeticUtil.sound(location, sound, 1.0F, 1.0F);
    }
}
