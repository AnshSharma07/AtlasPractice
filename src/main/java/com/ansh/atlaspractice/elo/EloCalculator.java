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


public final class EloCalculator {

    private static final int DEFAULT_K_FACTOR = 32;

    
    public static int[] calculateNewRatings(int ratingA, int ratingB, boolean aWon) {
        double expectedScoreA = 1.0 / (1.0 + Math.pow(10.0, (double) (ratingB - ratingA) / 400.0));
        double expectedScoreB = 1.0 / (1.0 + Math.pow(10.0, (double) (ratingA - ratingB) / 400.0));

        double actualScoreA = aWon ? 1.0 : 0.0;
        double actualScoreB = aWon ? 0.0 : 1.0;

        int newRatingA = (int) Math.round(ratingA + DEFAULT_K_FACTOR * (actualScoreA - expectedScoreA));
        int newRatingB = (int) Math.round(ratingB + DEFAULT_K_FACTOR * (actualScoreB - expectedScoreB));

        return new int[]{Math.max(100, newRatingA), Math.max(100, newRatingB)};
    }
}
