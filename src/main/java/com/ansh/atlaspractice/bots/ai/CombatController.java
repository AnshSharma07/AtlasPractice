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
import com.ansh.atlaspractice.bots.PracticeBot;
import com.ansh.atlaspractice.bots.npc.CitizensBotNPC;
import com.ansh.atlaspractice.match.Match;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public final class CombatController {

    private static final long COMBO_WINDOW_MS  = 750L;
    private static final long LOSING_COMBO_MS  = 1_400L;

    private final PracticeBot        bot;
    private final CitizensBotNPC     botNPC;
    private final BotDifficulty      difficulty;
    private final MovementController movementController;

    private long nextAllowedAttackTime;
    private long lastHitTime;
    private int  consecutiveHits;

    public CombatController(
            PracticeBot bot,
            CitizensBotNPC botNPC,
            BotDifficulty difficulty,
            MovementController movementController
    ) {
        this.bot               = bot;
        this.botNPC            = botNPC;
        this.difficulty        = difficulty;
        this.movementController = movementController;
    }

    public void updateCombat(Player target) {
        if (target == null || !target.isOnline() || target.isDead()) return;

        Player botPlayer = botNPC.getBukkitEntity();
        if (botPlayer == null || botNPC.isDead() || botNPC.getHealth() <= 0.0F) return;

        if (difficulty != BotDifficulty.IMPOSSIBLE && !botPlayer.hasLineOfSight(target)) return;

        long    now         = System.currentTimeMillis();
        boolean inCombo     = isInCombo();
        boolean losingCombo = isLosingCombo();

        movementController.setComboState(inCombo, losingCombo);

        double distance     = botPlayer.getLocation().distance(target.getLocation());
        double currentReach = variedReach();

        // EASY: stop attacking while panicking
        if (difficulty == BotDifficulty.EASY && movementController.isPanicking()) return;

        // NOOB air swings (visual only, no damage)
        if (difficulty == BotDifficulty.NOOB
                && distance > currentReach && distance < 6.0D) {
            if (now >= nextAllowedAttackTime
                    && ThreadLocalRandom.current().nextDouble() < 0.35D) {
                botNPC.swingArm();
                nextAllowedAttackTime = now + calculateAttackInterval(false);
            }
            return;
        }

        // Out of reach â€” don't attack, don't swing
        if (distance > currentReach + 0.18D) return;

        // Attack cooldown
        if (now < nextAllowedAttackTime) return;
        nextAllowedAttackTime = now + calculateAttackInterval(inCombo);

        botNPC.swingArm();

        Match match    = bot.getMatch();
        boolean isBoxing = match != null
                && match.getKit() != null
                && match.getKit().isBoxingMode();

        boolean targetImmune = !isBoxing
                && target.getNoDamageTicks() > target.getMaximumNoDamageTicks() / 2;

        if (targetImmune) return; // swing already sent above; damage blocked by iframes

        // Forced miss
        if (ThreadLocalRandom.current().nextDouble() < missChance(distance, inCombo)) return;

        if (difficulty == BotDifficulty.NOOB && consecutiveHits >= 1) {
            consecutiveHits = 0;
            return;
        }
        if (difficulty == BotDifficulty.EASY && consecutiveHits >= 5) {
            consecutiveHits = 0;
            return;
        }

        // EASY panic trigger
        if (difficulty == BotDifficulty.EASY
                && ThreadLocalRandom.current().nextDouble() < difficulty.getPanicChance()) {
            movementController.triggerFearPanic();
            return;
        }

        registerHit(now);

        if (ThreadLocalRandom.current().nextDouble() < difficulty.getSprintResetChance()) {
            movementController.triggerSprintReset();
        }

        EntityPlayer attacker = botNPC.getEntityPlayer();
        if (attacker != null) {
            attacker.attack(((CraftPlayer) target).getHandle());
        }
    }

    private void registerHit(long now) {
        if (now - lastHitTime < COMBO_WINDOW_MS) {
            consecutiveHits++;
        } else {
            consecutiveHits = 1;
        }
        lastHitTime = now;
    }

    private long calculateAttackInterval(boolean inCombo) {
        int  baseCps      = dynamicCps(inCombo);
        long baseInterval = Math.max(40L, 1000L / baseCps);
        long reactionMs   = difficulty.getReactionMs();
        if (reactionMs <= 0) return baseInterval;

        ThreadLocalRandom rng = ThreadLocalRandom.current();
        long maxJitter;
        switch (difficulty) {
            case IMPOSSIBLE: maxJitter = Math.max(1L, reactionMs / 4L); break;
            case INSANE:     maxJitter = Math.max(1L, reactionMs / 3L); break;
            case HARD:       maxJitter = Math.max(1L, reactionMs / 2L); break;
            default:         maxJitter = Math.max(1L, reactionMs);      break;
        }
        long jitter = (long) (Math.pow(rng.nextDouble(), 1.5D) * maxJitter);
        return baseInterval + jitter;
    }

    private int dynamicCps(boolean inCombo) {
        int base = difficulty.getCps();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int variance;
        switch (difficulty) {
            case IMPOSSIBLE: variance = rng.nextInt(0, 2);  break;
            case INSANE:     variance = rng.nextInt(-1, 3); break;
            case HARD:       variance = rng.nextInt(-2, 3); break;
            default:         variance = rng.nextInt(-3, 3); break;
        }
        if (inCombo && (difficulty == BotDifficulty.INSANE
                || difficulty == BotDifficulty.IMPOSSIBLE)) {
            variance += 1;
        }
        return Math.max(1, base + variance);
    }

    private double variedReach() {
        return Math.max(
                2.50D,
                difficulty.getReach()
                        + ThreadLocalRandom.current().nextDouble(-0.08D, 0.08D)
        );
    }

    private double missChance(double distance, boolean inCombo) {
        double base = difficulty.getForcedMissChance();
        double distancePenalty = Math.max(0.0D, distance - 2.80D) * 0.15D;
        if (inCombo && consecutiveHits >= 2
                && (difficulty == BotDifficulty.HARD
                    || difficulty == BotDifficulty.INSANE)) {
            base = Math.max(0.01D, base - 0.05D);
        }
        return base + distancePenalty;
    }

    public boolean isInCombo() {
        return System.currentTimeMillis() - lastHitTime < COMBO_WINDOW_MS;
    }

    public boolean isLosingCombo() {
        if (lastHitTime == 0L) return false;
        long gap = System.currentTimeMillis() - lastHitTime;
        return gap > LOSING_COMBO_MS && gap < (long) (LOSING_COMBO_MS * 2.5);
    }

    public int getConsecutiveHits() { return consecutiveHits; }

    public void reset() {
        nextAllowedAttackTime = 0L;
        lastHitTime           = 0L;
        consecutiveHits       = 0;
    }
}
