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

package com.ansh.atlaspractice.profile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KitStats {

    private final String kitName;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private int matches;
    private int winstreak;
    private int bestWinstreak;
    private int elo;

    public KitStats(String kitName) {
        this.kitName = kitName;
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
        this.losses = 0;
        this.matches = 0;
        this.winstreak = 0;
        this.bestWinstreak = 0;
        this.elo = 1000; // Default ELO
    }
    
    public void addKill() { this.kills++; }
    public void addDeath() { this.deaths++; }
    public void addWin() { this.wins++; }
    public void addLoss() { this.losses++; }
    public void addMatch() { this.matches++; }
    
    public void incWinstreak() {
        this.winstreak++;
        if (this.winstreak > this.bestWinstreak) {
            this.bestWinstreak = this.winstreak;
        }
    }
    
    public void resetWinstreak() {
        this.winstreak = 0;
    }
}