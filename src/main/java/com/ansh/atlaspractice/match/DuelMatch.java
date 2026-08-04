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

package com.ansh.atlaspractice.match;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaState;
import com.ansh.atlaspractice.arena.SharedArena;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public final class DuelMatch extends Match {

    private final int maxRounds;
    private int currentRound = 1;
    private int teamOneWins = 0;
    private int teamTwoWins = 0;
    private BukkitRunnable countdownTask;
    private BukkitRunnable nextRoundTask;
    private boolean processingRound = false;

    public DuelMatch(Kit kit, Arena arena, List<MatchTeam> teams, int maxRounds) {
        super(kit, arena, teams);
        this.maxRounds = maxRounds;
        // ranked remains false (base default)
    }

    public DuelMatch(Kit kit, Arena arena, List<MatchTeam> teams, boolean ranked) {
        super(kit, arena, teams);
        this.maxRounds = 1;
        setRanked(ranked);
    }

    @Override
    public void start() {

        try {
            setStartTime(System.currentTimeMillis());
        } catch (Exception ignored) {
        }

        if (this.getArena() != null) {
            this.getArena().setState(ArenaState.ALLOCATED);
        }

        if (isRanked()) {
            broadcastMessage(
                    "§cThis is a ranked profile evaluation match. Rated skill allocations will modify at completion."
            );
        }

        setupRoundLifecycle();
    }

    private void resetPlayer(
            Player p,
            Location spawn,
            MatchTeam team
    ) {

        p.teleport(spawn);

        p.setHealth(20.0D);
        p.setFoodLevel(20);
        p.setSaturation(2.0F);
        p.setExhaustion(0.0F);

        p.setFireTicks(0);
        p.setFallDistance(0.0F);

        p.setNoDamageTicks(0);

        p.setAllowFlight(false);
        p.setFlying(false);

        p.setWalkSpeed(0.2F);

        p.getInventory().clear();
        p.getInventory().setArmorContents(null);

        for (PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }

        Profile profile =
                AtlasPracticePlugin.getInstance()
                        .getProfileManager()
                        .getProfile(p.getUniqueId());

        if (profile != null) {
            this.getKit().applyToPlayer(
                    p,
                    profile,
                    team.getTeamColor()
            );
        } else {
            this.getKit().applyToPlayer(
                    p,
                    team.getTeamColor()
            );
        }
    }

    public void setupRoundLifecycle() {

        processingRound = false;

        this.setState(MatchState.STARTING);

        for (int i = 0; i < this.getTeams().size(); i++) {

            MatchTeam team = this.getTeams().get(i);

            Location spawn = (i == 0)
                    ? this.getArena().getSpawnRed()
                    : this.getArena().getSpawnBlue();

            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {

                Player p = Bukkit.getPlayer(mp.getUuid());

                if (p == null || spawn == null) {
                    continue;
                }

                resetPlayer(
                        p,
                        spawn,
                        team
                );
            }
        }

        if (countdownTask != null) {
            countdownTask.cancel();
        }

        countdownTask = new BukkitRunnable() {

            int countdown = 3;

            @Override
            public void run() {

                if (getState() == MatchState.ENDING) {
                    countdownTask = null;
                    cancel();
                    return;
                }

                if (countdown <= 0) {

                    setState(MatchState.FIGHTING);

                    broadcastMessage(
                            "§aRound " + currentRound + " has officially begun!"
                    );

                    playSoundToAll(
                            Sound.NOTE_PLING,
                            1.0F,
                            2.0F
                    );

                    countdownTask = null;
                    cancel();
                    return;
                }

                broadcastMessage(
                        "§7Round begins in §b" + countdown + "§7..."
                );

                playSoundToAll(
                        Sound.NOTE_PLING,
                        1.0F,
                        1.0F
                );

                countdown--;
            }

        };

        countdownTask.runTaskTimer(
                AtlasPracticePlugin.getInstance(),
                0L,
                20L
        );
    }

    public void handleRoundWin(MatchTeam winningTeam) {

        if (this.getState() != MatchState.FIGHTING) {
            return;
        }

        if (processingRound) {
            return;
        }

        processingRound = true;

        this.setState(MatchState.STARTING);

        boolean teamOneWon =
                winningTeam == this.getTeams().get(0);

        int newScore;

        if (teamOneWon) {

            teamOneWins++;
            newScore = teamOneWins;

        } else {

            teamTwoWins++;
            newScore = teamTwoWins;
        }

        broadcastMessage("");
        broadcastMessage("§b§lDuel");

        broadcastMessage(
                "§7Round §f"
                        + currentRound
                        + "§7/§f"
                        + maxRounds
        );

        broadcastMessage(
                "§b"
                        + winningTeam.getLeaderName()
                        + " §fwon this round!"
        );

        broadcastMessage(
                "§7Score: §b"
                        + teamOneWins
                        + " §7- §b"
                        + teamTwoWins
        );

        broadcastMessage("");

        int roundsNeeded = (maxRounds / 2) + 1;

        if (newScore >= roundsNeeded) {

            if (countdownTask != null) {
                countdownTask.cancel();
                countdownTask = null;
            }

            if (nextRoundTask != null) {
                nextRoundTask.cancel();
                nextRoundTask = null;
            }

            AtlasPracticePlugin.getInstance()
                    .getMatchManager()
                    .triggerMatchEndSequence(
                            this,
                            winningTeam
                    );

            return;
        }

        currentRound++;

        if (nextRoundTask != null) {
            nextRoundTask.cancel();
        }

        nextRoundTask = new BukkitRunnable() {

            @Override
            public void run() {

                nextRoundTask = null;

                if (getState() == MatchState.ENDING) {
                    return;
                }

                if (getArena() instanceof SharedArena) {

                    ((SharedArena) getArena()).resetForNextRound(DuelMatch.this, () -> {
                        setRedBedAlive(true);
                        setBlueBedAlive(true);
                        setupRoundLifecycle();

                    });

                } else {

                    setupRoundLifecycle();

                }
            }

        };

        nextRoundTask.runTaskLater(
                AtlasPracticePlugin.getInstance(),
                60L
        );
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public int getPlayerScore(Player player) {

        UUID uuid = player.getUniqueId();

        boolean isTeamOne = this.getTeams()
                .get(0)
                .getPlayers()
                .stream()
                .anyMatch(mp ->
                        mp.getUuid().equals(uuid));

        return isTeamOne
                ? teamOneWins
                : teamTwoWins;
    }

    public int getOpponentScore(Player player) {

        UUID uuid = player.getUniqueId();

        boolean isTeamOne = this.getTeams()
                .get(0)
                .getPlayers()
                .stream()
                .anyMatch(mp ->
                        mp.getUuid().equals(uuid));

        return isTeamOne
                ? teamTwoWins
                : teamOneWins;
    }

    private void playSoundToAll(
            Sound sound,
            float volume,
            float pitch
    ) {

        this.getTeams().forEach(team ->
                team.getPlayers().forEach(mp -> {

                    Player p = Bukkit.getPlayer(mp.getUuid());

                    if (p != null) {

                        p.playSound(
                                p.getLocation(),
                                sound,
                                volume,
                                pitch
                        );
                    }
                }));
    }
}