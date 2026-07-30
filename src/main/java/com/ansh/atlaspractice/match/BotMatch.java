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

package com.ansh.atlaspractice.match;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.bots.BotDifficulty;
import com.ansh.atlaspractice.bots.PracticeBot;
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public final class BotMatch extends Match {

    private final UUID playerUuid;
    private final UUID botUuid;
    private final BotDifficulty difficulty;

    private PracticeBot bot;
    private void startCountdown(Player player) {
        bot.setFrozen(true);

        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);

        new BukkitRunnable() {

            int seconds = 3;

            @Override
            public void run() {

                if (!player.isOnline() || bot == null) {
                    cancel();
                    return;
                }

                if (seconds > 0) {

                    player.sendTitle(
                            "§e" + seconds,
                            ""
                    );

                    player.playSound(
                            player.getLocation(),
                            org.bukkit.Sound.NOTE_PLING,
                            1F,
                            1F
                    );

                    seconds--;
                    return;
                }

                player.sendTitle(
                        "§aFight!",
                        ""
                );

                player.playSound(
                        player.getLocation(),
                        org.bukkit.Sound.LEVEL_UP,
                        1F,
                        1F
                );

                player.setWalkSpeed(0.2F);
                player.setFlySpeed(0.1F);

                bot.setFrozen(false);
                bot.startFighting();

                cancel();
            }

        }.runTaskTimer(
                AtlasPracticePlugin.getInstance(),
                20L,
                20L
        );
    }
    public BotMatch(
            Kit kit,
            Arena arena,
            List<MatchTeam> teams,
            UUID playerUuid,
            UUID botUuid,
            BotDifficulty difficulty
    ) {
        super(kit, arena, teams);

        this.playerUuid = playerUuid;
        this.botUuid = botUuid;
        this.difficulty = difficulty;
    }

    @Override
    public void start() {

        super.start();

        Player player = Bukkit.getPlayer(playerUuid);

        if (player == null || !player.isOnline()) {
            end(null);
            return;
        }

        bot = AtlasPracticePlugin.getInstance()
                .getBotManager()
                .createBot(
                        player,
                        botUuid,
                        getArena().getSpawnBlue(),
                        difficulty
                );

        if (bot == null) {
            end(null);
            return;
        }

        bot.setMatch(this);

        AtlasPracticePlugin.getInstance()
                .getMatchManager()
                .registerExternalParticipant(botUuid, getId());

        getKit().applyToPlayer(
                bot.getBukkitEntity(),
                getTeams().get(1).getTeamColor()
        );

        bot.getBukkitEntity().teleport(getArena().getSpawnBlue());

        bot.updateEquipment();

        startCountdown(player);
    }

    @Override
    public void end(MatchTeam winnerTeam) {

        AtlasPracticePlugin plugin = AtlasPracticePlugin.getInstance();

        plugin.getMatchManager().unregisterExternalParticipant(botUuid);

        plugin.getBotManager().removeBot(playerUuid);

        bot = null;

        super.end(winnerTeam);
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public UUID getBotUuid() {
        return botUuid;
    }

    public PracticeBot getBot() {
        return bot;
    }

    public BotDifficulty getDifficulty() {
        return difficulty;
    }
}