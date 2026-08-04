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

import org.bukkit.configuration.file.FileConfiguration;

public enum BotDifficulty {

    NOOB("Noob", "noob", 4, 3.00D, 280L, 0.40D, 0.01D, 0.1D, 0.0D, 0.0D, 0.0D, 5.0F, 0.05F, 0.0D, 0.50D, 0.0D),
    EASY("Easy", "easy", 6, 2.95D, 240L, 0.55D, 0.05D, 0.2D, 0.0D, 0.3D, 0.02D, 3.5F, 0.10F, 0.5D, 0.25D, 0.30D),
    MEDIUM("Medium", "medium", 9, 3.02D, 130L, 0.80D, 0.08D, 0.5D, 0.15D, 0.15D, 0.10D, 1.5F, 0.20F, 1.5D, 0.10D, 0.0D),
    HARD("Hard", "hard", 12, 3.08D, 72L, 0.95D, 0.12D, 0.7D, 0.50D, 0.30D, 0.25D, 0.5F, 0.35F, 2.5D, 0.05D, 0.0D),
    INSANE("Insane", "insane", 14, 3.12D, 38L, 1.06D, 0.15D, 0.9D, 0.85D, 0.60D, 0.45D, 0.1F, 0.50F, 3.5D, 0.01D, 0.0D),
    IMPOSSIBLE("Impossible", "impossible", 15, 3.10D, 28L, 1.10D, 0.18D, 1.0D, 1.00D, 1.00D, 0.95D, 0.0F, 1.00F, 4.0D, 0.00D, 0.0D);
    private final String displayName;
    private final String configKey;
    private int cps;
    private double reach;
    private long reactionMs;
    private double strafeSpeed;

    // Personality Fields
    private final double jumpChance;
    private final double aggressionBias;
    private final double sprintResetChance;
    private final double comboEscapeChance;
    private final double circleChance;
    private final float aimNoise;
    private final float aimSpeed;
    private final double predictionTicks;
    private final double forcedMissChance;
    private final double panicChance;

    BotDifficulty(String displayName, String configKey, int cps, double reach, long reactionMs, double strafeSpeed,
                  double jumpChance, double aggressionBias, double sprintResetChance, double comboEscapeChance,
                  double circleChance, float aimNoise, float aimSpeed, double predictionTicks, double forcedMissChance, double panicChance) {
        this.displayName = displayName;
        this.configKey = configKey;
        this.cps = cps;
        this.reach = reach;
        this.reactionMs = reactionMs;
        this.strafeSpeed = strafeSpeed;
        this.jumpChance = jumpChance;
        this.aggressionBias = aggressionBias;
        this.sprintResetChance = sprintResetChance;
        this.comboEscapeChance = comboEscapeChance;
        this.circleChance = circleChance;
        this.aimNoise = aimNoise;
        this.aimSpeed = aimSpeed;
        this.predictionTicks = predictionTicks;
        this.forcedMissChance = forcedMissChance;
        this.panicChance = panicChance;
    }

    public void loadFromConfig(FileConfiguration config) {
        String path = config.contains("difficulties." + configKey)
                ? "difficulties." + configKey + "."
                : "bot." + configKey + ".";
        int minCps = config.getInt(path + "min-cps", this.cps);
        int maxCps = config.getInt(path + "max-cps", config.getInt(path + "cps", this.cps));
        this.cps = Math.max(1, (minCps + maxCps) / 2);
        this.reach = config.getDouble(path + "reach", config.getDouble(path + "range", this.reach));
        this.reactionMs = config.getLong(path + "reaction-time", config.getLong(path + "reaction-ms", this.reactionMs));
        this.strafeSpeed = config.getDouble(path + "strafe-speed", this.strafeSpeed);
    }

    // Getters
    public String getDisplayName() { return displayName; }
    public int getCps() { return cps; }
    public double getReach() { return reach; }
    public long getReactionMs() { return reactionMs; }
    public double getStrafeSpeed() { return strafeSpeed; }
    public double getJumpChance() { return jumpChance; }
    public double getAggressionBias() { return aggressionBias; }
    public double getSprintResetChance() { return sprintResetChance; }
    public double getComboEscapeChance() { return comboEscapeChance; }
    public double getCircleChance() { return circleChance; }
    public float getAimNoise() { return aimNoise; }
    public float getAimSpeed() { return aimSpeed; }
    public double getPredictionTicks() { return predictionTicks; }
    public double getForcedMissChance() { return forcedMissChance; }
    public double getPanicChance() { return panicChance; }
}