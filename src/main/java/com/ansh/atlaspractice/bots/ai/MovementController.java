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

package com.ansh.atlaspractice.bots.ai;

import com.ansh.atlaspractice.bots.BotDifficulty;
import com.ansh.atlaspractice.bots.npc.CitizensBotNPC;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public final class MovementController {

    private enum Intent {
        CHASE,          // sprint straight at target
        STRAFE_LEFT,    // circle left while closing
        STRAFE_RIGHT,   // circle right while closing
        BACKSTEP,       // back away
        RETREAT,        // fast back-away (escape combo)
        STOP,           // stand still briefly
        FORWARD_PUSH,   // aggressive close-in
        WANDER_NOOB,    // random wander (NOOB only)
        PANIC_FEAR      // flee (EASY only)
    }
    private static final double CHASE_RANGE    = 4.5D; // switch from strafe to chase
    private static final double BACKSTEP_RANGE = 1.6D; // too close â€” back up
    private static final double ATTACK_RANGE   = 3.3D; // within combat range

    private final CitizensBotNPC botNPC;
    private final BotDifficulty  difficulty;

    private Intent  currentIntent   = Intent.CHASE;
    private int     holdTicks;
    private boolean sprintReset;
    private int     sprintResetTicks;
    private boolean inCombo;
    private boolean losingCombo;
    private float   strafeSign      = 1.0F;
    private boolean isPanicking;

    public MovementController(CitizensBotNPC botNPC, BotDifficulty difficulty) {
        this.botNPC    = botNPC;
        this.difficulty = difficulty;
    }
    public void update(Player target) {
        if (target == null || !target.isOnline() || target.isDead()) {
            stop();
            return;
        }

        double distance = botNPC.getLocation().distance(target.getLocation());
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        if (difficulty == BotDifficulty.NOOB) {
            if (--holdTicks <= 0) {
                holdTicks   = rng.nextInt(10, 40);
                strafeSign  = rng.nextBoolean() ? 1.0F : -1.0F;
            }
            applyIntent(Intent.WANDER_NOOB, distance, target);
            return;
        }
        if (difficulty == BotDifficulty.EASY && isPanicking) {
            if (--holdTicks <= 0) {
                isPanicking = false;
            } else {
                applyIntent(Intent.PANIC_FEAR, distance, target);
                return;
            }
        }
        if (distance > CHASE_RANGE && currentIntent != Intent.CHASE
                && currentIntent != Intent.RETREAT) {
            enterIntent(Intent.CHASE, holdRange(3, 6));
        } else if (distance < BACKSTEP_RANGE && currentIntent != Intent.BACKSTEP) {
            enterIntent(Intent.BACKSTEP, holdRange(3, 7));
        }
        if (--holdTicks <= 0) {
            pickNextIntent(distance, rng);
        }

        applyIntent(currentIntent, distance, target);
    }

    private void pickNextIntent(double distance, ThreadLocalRandom rng) {
        if (difficulty == BotDifficulty.IMPOSSIBLE) {
            strafeSign = rng.nextBoolean() ? 1.0F : -1.0F;
            enterIntent(
                rng.nextBoolean() ? Intent.STRAFE_LEFT : Intent.STRAFE_RIGHT,
                rng.nextInt(2, 5)
            );
            return;
        }

        if (losingCombo && rng.nextDouble() < difficulty.getComboEscapeChance()) {
            enterIntent(Intent.RETREAT, holdRange(4, 9));
            return;
        }

        if (inCombo && rng.nextDouble() < difficulty.getAggressionBias()) {
            enterIntent(
                rng.nextBoolean() ? Intent.FORWARD_PUSH : Intent.STRAFE_LEFT,
                holdRange(3, 7)
            );
            return;
        }

        if (rng.nextDouble() < difficulty.getCircleChance()) {
            strafeSign = rng.nextBoolean() ? 1.0F : -1.0F;
            enterIntent(
                strafeSign > 0 ? Intent.STRAFE_LEFT : Intent.STRAFE_RIGHT,
                holdRange(5, 12)
            );
            return;
        }

        enterIntent(Intent.CHASE, holdRange(5, 15));
    }

    private void applyIntent(Intent intent, double distance, Player target) {
        float  speedMod = 1.0F;
        boolean sprint  = true;
        boolean navigate = true;

        if (sprintReset) {
            sprint = false;
            if (--sprintResetTicks <= 0) sprintReset = false;
        }

        switch (intent) {
            case CHASE:
                speedMod = 1.4F; // sprint speed
                break;

            case STRAFE_LEFT:
            case STRAFE_RIGHT:

                speedMod = (float) (0.65 * difficulty.getStrafeSpeed() / 0.80);
                sprint   = false;
                break;

            case BACKSTEP:
                navigate = false;
                botNPC.stopNavigation();
                botNPC.setSprinting(false);
                return;

            case RETREAT:
                navigate = false;
                botNPC.stopNavigation();
                botNPC.setSprinting(false);
                return;

            case STOP:
                navigate = false;
                botNPC.stopNavigation();
                botNPC.setSprinting(false);
                return;

            case FORWARD_PUSH:
                speedMod = 1.4F;
                break;

            case WANDER_NOOB:
                speedMod = 0.6F;
                sprint   = false;
                break;

            case PANIC_FEAR:
                navigate = false;
                botNPC.stopNavigation();
                botNPC.setSprinting(false);
                return;
        }

        if (difficulty == BotDifficulty.IMPOSSIBLE) sprint = true;

        if (navigate) {
            botNPC.navigateTo(target, speedMod);
            botNPC.setSprinting(sprint);
        }
    }

    public void triggerFearPanic() {
        if (difficulty != BotDifficulty.EASY) return;
        isPanicking = true;
        enterIntent(Intent.PANIC_FEAR,
                ThreadLocalRandom.current().nextInt(40, 80));
    }

    public void triggerSprintReset() {
        if (difficulty == BotDifficulty.NOOB || difficulty == BotDifficulty.EASY) return;
        sprintReset      = true;
        sprintResetTicks = (difficulty == BotDifficulty.IMPOSSIBLE) ? 1 : 2;
    }

    public void setComboState(boolean inCombo, boolean losingCombo) {
        this.inCombo     = inCombo;
        this.losingCombo = losingCombo;
    }

    private void enterIntent(Intent intent, int ticks) {
        this.currentIntent = intent;
        this.holdTicks     = ticks;
    }

    private int holdRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public boolean isPanicking() { return isPanicking; }

    public void stop() {
        botNPC.stopNavigation();
        botNPC.setSprinting(false);
    }

    public void reset() {
        stop();
        currentIntent = Intent.CHASE;
        holdTicks     = 0;
        isPanicking   = false;
    }
}
