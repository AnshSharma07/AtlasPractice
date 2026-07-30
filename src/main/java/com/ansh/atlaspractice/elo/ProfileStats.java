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

package com.ansh.atlaspractice.elo;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public final class ProfileStats {

    private int elo;
    private int wins;
    private int losses;
    private int currentStreak;
    private int maxStreak;

    
    public ProfileStats() {
        this.elo = 1000;
        this.wins = 0;
        this.losses = 0;
        this.currentStreak = 0;
        this.maxStreak = 0;
    }

    
    public void incWins() {
        this.wins++;
        this.currentStreak++;
        this.maxStreak = Math.max(this.maxStreak, this.currentStreak);
    }

    
    public void incLosses() {
        this.losses++;
        this.currentStreak = 0;
    }
}
