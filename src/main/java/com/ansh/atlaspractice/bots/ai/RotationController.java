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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Rotation Controller for the Practice Bot.
 *
 * <p>Only change from the original: the {@code BotNPC} type is replaced by
 * {@link CitizensBotNPC}.  All smoothing and noise logic is identical.</p>
 */
public final class RotationController {

    private final CitizensBotNPC botNPC;
    private final BotDifficulty  difficulty;
    private float yawVelocity;
    private float pitchVelocity;
    private int   overcorrectCooldown;

    public RotationController(CitizensBotNPC botNPC, BotDifficulty difficulty) {
        this.botNPC    = botNPC;
        this.difficulty = difficulty;
    }

    public void facePlayer(Player target) {
        if (target == null || !target.isOnline() || target.isDead()) return;

        double predTicks = difficulty.getPredictionTicks();
        Vector vel = target.getVelocity().clone().multiply(predTicks * 0.05D);

        double verticalOffset = (difficulty == BotDifficulty.NOOB)
                ? ThreadLocalRandom.current().nextDouble(0.5, 2.5)
                : 1.45D;

        Location aim = target.getLocation().add(vel).add(0.0D, verticalOffset, 0.0D);
        faceLocation(aim, difficulty.getAimSpeed());
    }

    private void faceLocation(Location targetLocation, float speedFactor) {
        Location eyeLocation = botNPC.getLocation().clone().add(0.0D, 1.62D, 0.0D);
        Vector direction = targetLocation.toVector().subtract(eyeLocation.toVector());

        if (direction.lengthSquared() < 0.000001D) return;
        direction.normalize();

        float targetYaw = (float) Math.toDegrees(
                Math.atan2(-direction.getX(), direction.getZ()));
        float targetPitch = (float) -Math.toDegrees(
                Math.atan2(direction.getY(),
                        Math.sqrt(direction.getX() * direction.getX()
                                + direction.getZ() * direction.getZ())));

        float currentYaw   = botNPC.getLocation().getYaw();
        float currentPitch = botNPC.getLocation().getPitch();

        float yawDelta   = angleDifference(currentYaw,   targetYaw);
        float pitchDelta = angleDifference(currentPitch, targetPitch);

        yawVelocity   = yawVelocity   * 0.55F + yawDelta   * speedFactor * 0.45F;
        pitchVelocity = pitchVelocity * 0.60F + pitchDelta * speedFactor * 0.40F;

        if (overcorrectCooldown > 0) overcorrectCooldown--;
        if ((difficulty == BotDifficulty.EASY || difficulty == BotDifficulty.NOOB)
                && Math.abs(yawDelta) > 15.0F && overcorrectCooldown <= 0) {
            yawVelocity = -yawVelocity
                    * (difficulty == BotDifficulty.NOOB ? 1.5F : 0.6F);
            overcorrectCooldown = ThreadLocalRandom.current().nextInt(10, 30);
        }

        if (difficulty == BotDifficulty.EASY
                && ThreadLocalRandom.current().nextDouble() < 0.02) {
            yawVelocity += 180.0F;
        }

        float noiseScale = difficulty.getAimNoise();
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        double yawNoise = (noiseScale <= 0.0F) ? 0.0D
                : rng.nextDouble(-noiseScale, noiseScale);
        double pitchNoise = (noiseScale <= 0.0F) ? 0.0D
                : rng.nextDouble(-noiseScale * 0.6D, noiseScale * 0.6D);

        float finalYaw = currentYaw
                + clamp(yawVelocity, -25.0F, 25.0F)
                + (float) yawNoise;

        float finalPitch = clamp(
                currentPitch
                        + clamp(pitchVelocity, -15.0F, 15.0F)
                        + (float) pitchNoise,
                -89.0F, 89.0F
        );

        botNPC.rotate(finalYaw, finalPitch);
    }

    private float angleDifference(float current, float target) {
        float delta = (target - current) % 360.0F;
        if (delta >  180.0F) delta -= 360.0F;
        if (delta < -180.0F) delta += 360.0F;
        return delta;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
