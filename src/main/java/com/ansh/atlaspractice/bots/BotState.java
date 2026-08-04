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
// UNDER DEVELOPEMENT, disabled rn
package com.ansh.atlaspractice.bots;

public enum BotState {
    IDLE,
    SPAWNING,
    COUNTDOWN,
    SEARCHING,
    FOLLOWING,
    STRAFING,
    ATTACKING,
    COMBO,
    RECOVERING,
    HEALING,
    PEARLING,
    BOWING,
    STUCK,
    DYING,
    DEAD,
    DESPAWN;

    public boolean isAlive() {
        return this != DEAD
                && this != DYING
                && this != DESPAWN;
    }

    public boolean isCombatState() {
        switch (this) {
            case FOLLOWING:
            case STRAFING:
            case ATTACKING:
            case COMBO:
            case RECOVERING:
            case HEALING:
            case PEARLING:
            case BOWING:
                return true;

            default:
                return false;
        }
    }

    public boolean isMovementState() {
        switch (this) {
            case FOLLOWING:
            case STRAFING:
            case RECOVERING:
            case SEARCHING:
            case STUCK:
                return true;

            default:
                return false;
        }
    }

    public boolean isWaiting() {
        return this == IDLE
                || this == SPAWNING
                || this == COUNTDOWN;
    }
}