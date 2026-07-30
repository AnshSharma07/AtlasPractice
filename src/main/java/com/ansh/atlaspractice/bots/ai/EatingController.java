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

package com.ansh.atlaspractice.bots.ai;

import com.ansh.atlaspractice.bots.BotDifficulty;
import com.ansh.atlaspractice.bots.npc.CitizensBotNPC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Eating Controller for the Practice Bot.
 *
 * <p>Only change from the original: the {@code BotNPC} type is replaced by
 * {@link CitizensBotNPC}.  All logic is identical.</p>
 */
public final class EatingController {

    private final CitizensBotNPC botNPC;
    private final double         eatThreshold;
    private final BotDifficulty  difficulty;
    private boolean isEating;
    private int     eatTicks;

    public EatingController(
            CitizensBotNPC botNPC,
            double eatThreshold,
            BotDifficulty difficulty
    ) {
        this.botNPC       = botNPC;
        this.eatThreshold = eatThreshold;
        this.difficulty   = difficulty;
    }

    public void updateEating() {
        if (isEating) {
            if (--eatTicks <= 0) {
                isEating = false;
                botNPC.setHealth(Math.min(20.0f, botNPC.getHealth() + 4.0f));
            }
            return;
        }

        // NOOB panic eating: random chance even at high health
        if (difficulty == BotDifficulty.NOOB
                && ThreadLocalRandom.current().nextDouble() < 0.005) {
            startEating();
            return;
        }

        if (botNPC.getHealth() <= eatThreshold) {
            startEating();
        }
    }

    private void startEating() {
        this.isEating = true;
        this.eatTicks = 32; // standard consume duration
        botNPC.setHeldItem(new ItemStack(Material.GOLDEN_APPLE));
    }

    public boolean isEating() { return isEating; }

    public void reset() {
        isEating = false;
        eatTicks = 0;
    }
}
