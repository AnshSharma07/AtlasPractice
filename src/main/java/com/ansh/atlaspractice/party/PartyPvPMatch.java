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

package com.ansh.atlaspractice.party;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaState;
import com.ansh.atlaspractice.arena.SharedArena;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.profile.Profile;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@Getter
public class PartyPvPMatch extends Match implements PartyManagedMatch {

    private final List<Party> parties;
    private final int requiredWins;
    private int currentRound = 1;
    private int teamOneWins = 0;
    private int teamTwoWins = 0;
    private final Set<UUID> eliminatedThisRound = ConcurrentHashMap.newKeySet();
    private boolean processingRound = false;

    public PartyPvPMatch(List<Party> parties, Kit kit, Arena arena, List<MatchTeam> teams, int requiredWins) {
        super(kit, arena, teams);
        this.parties = parties;
        this.requiredWins = requiredWins;
    }
    public boolean isEliminated(Player player) {
        return eliminatedThisRound.contains(player.getUniqueId());
    }
    @Override
    public void start() {
        try {
            setStartTime(System.currentTimeMillis());
        } catch (Exception ignored) {}

        if (this.getArena() != null) {
            this.getArena().setState(ArenaState.ALLOCATED);
        }

        setupRoundLifecycle();
        new BukkitRunnable() {
            @Override
            public void run() {

                if (getState() == MatchState.ENDING) {
                    cancel();
                    return;
                }

                if (System.currentTimeMillis() - getStartTime() >= 900000L) {

                    broadcastMessage("§cParty PvP timed out.");

                    AtlasPracticePlugin.getInstance()
                            .getMatchManager()
                            .triggerMatchEndSequence(
                                    PartyPvPMatch.this,
                                    null
                            );

                    cancel();
                }
            }
        }.runTaskTimer(
                AtlasPracticePlugin.getInstance(),
                1200L,
                1200L
        );
    }

    private void setupRoundLifecycle() {
        this.processingRound = false;
        this.setState(MatchState.STARTING);
        this.eliminatedThisRound.clear();
        this.getSpectators().clear();

        for (int i = 0; i < this.getTeams().size(); i++) {
            MatchTeam team = this.getTeams().get(i);
            Location spawn = (i == 0) ? this.getArena().getSpawnRed() : this.getArena().getSpawnBlue();

            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                Player p = Bukkit.getPlayer(mp.getUuid());
                if (p == null || spawn == null) continue;

                p.teleport(spawn);
                p.setHealth(20.0D);
                p.setFoodLevel(20);
                p.setSaturation(2.0F);
                p.setExhaustion(0.0F);
                p.setFireTicks(0);
                p.setFallDistance(0.0F);
                p.setNoDamageTicks(60); // 3 seconds spawn protection
                p.setAllowFlight(false);
                p.setFlying(false);
                p.setWalkSpeed(0.2F);
                p.getInventory().clear();
                p.getInventory().setArmorContents(null);
                
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    p.removePotionEffect(effect.getType());
                }
                
                for (Player online : Bukkit.getOnlinePlayers()) {
                    p.showPlayer(online);
                }

                Profile profile = AtlasPracticePlugin.getInstance().getProfileManager().getProfile(p.getUniqueId());
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
        }

        new BukkitRunnable() {
            int countdown = 3;
            @Override
            public void run() {
                if (getState() == MatchState.ENDING) {
                    cancel();
                    return;
                }
                if (countdown <= 0) {
                    setState(MatchState.FIGHTING);
                    broadcastMessage("§aRound " + currentRound + " has officially begun!");
                    playSoundToAll(Sound.NOTE_PLING, 1.0F, 2.0F);
                    cancel();
                    return;
                }
                broadcastMessage("§7Round begins in §b" + countdown + "§7...");
                playSoundToAll(Sound.NOTE_PLING, 1.0F, 1.0F);
                countdown--;
            }
        }.runTaskTimer(AtlasPracticePlugin.getInstance(), 0L, 20L);
    }

    public void handleDeath(Player victim) {
        if (this.getState() != MatchState.FIGHTING || this.processingRound) return;
        if (!this.eliminatedThisRound.add(victim.getUniqueId())) return;

        AtlasPracticePlugin.getInstance().getMatchManager().makeInMatchSpectator(victim, this);
        broadcastMessage("§c" + victim.getName() + " was eliminated.");

        checkWinCondition();
    }

    private void checkWinCondition() {
        if (this.processingRound) {return;}
        MatchTeam teamOne = this.getTeams().get(0);
        MatchTeam teamTwo = this.getTeams().get(1);

        long t1Alive = teamOne.getPlayers().stream().filter(mp -> !this.eliminatedThisRound.contains(mp.getUuid())).count();
        long t2Alive = teamTwo.getPlayers().stream().filter(mp -> !this.eliminatedThisRound.contains(mp.getUuid())).count();

        if (t1Alive == 0 || t2Alive == 0) {

            this.processingRound = true;
            this.setState(MatchState.STARTING);

            if (t1Alive == 0 && t2Alive == 0) {
                broadcastMessage("§eBoth teams were eliminated!");
                broadcastMessage("§7Restarting the round...");
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (getState() != MatchState.ENDING) {

                            if (getArena() instanceof SharedArena) {

                                ((SharedArena) getArena()).resetForNextRound(PartyPvPMatch.this, () -> {

                                    setRedBedAlive(true);
                                    setBlueBedAlive(true);

                                    setupRoundLifecycle();

                                });

                            } else {

                                setRedBedAlive(true);
                                setBlueBedAlive(true);

                                setupRoundLifecycle();

                            }
                        }}}.runTaskLater(AtlasPracticePlugin.getInstance(), 60L);
                return;
            }
            MatchTeam roundWinner = (t1Alive == 0) ? teamTwo : teamOne;

            if (roundWinner == teamOne) {
                this.teamOneWins++;
            } else {
                this.teamTwoWins++;
            }

            broadcastMessage("");
            broadcastMessage("§b§lParty PvP");
            broadcastMessage("§7Round §f" + this.currentRound + "§7/§f" + ((this.requiredWins * 2) - 1));
            broadcastMessage("§b" + roundWinner.getLeaderName() + "'s Party §fwon this round!");
            broadcastMessage("§7Score: §b" + this.teamOneWins + " §7- §b" + this.teamTwoWins);
            broadcastMessage("");

            if (this.teamOneWins >= this.requiredWins || this.teamTwoWins >= this.requiredWins) {
                AtlasPracticePlugin.getInstance().getMatchManager().triggerMatchEndSequence(this, roundWinner);
                return;
            }

            this.currentRound++;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (getState() != MatchState.ENDING) {

                        if (getArena() instanceof SharedArena) {

                            ((SharedArena) getArena()).resetForNextRound(PartyPvPMatch.this, () -> {

                                setRedBedAlive(true);
                                setBlueBedAlive(true);

                                setupRoundLifecycle();

                            });

                        } else {

                            setRedBedAlive(true);
                            setBlueBedAlive(true);

                            setupRoundLifecycle();

                        }

                    }
                }
            }.runTaskLater(AtlasPracticePlugin.getInstance(), 60L);
        }
    }

    private void playSoundToAll(Sound sound, float volume, float pitch) {
        this.getTeams().forEach(team -> team.getPlayers().forEach(mp -> {
            Player p = Bukkit.getPlayer(mp.getUuid());
            if (p != null) p.playSound(p.getLocation(), sound, volume, pitch);
        }));
    }
    @Override
    public void end(MatchTeam winnerTeam) {
        broadcastMessage("");
        broadcastMessage("§b§lParty PvP Finished");
        if (winnerTeam != null) {
            broadcastMessage("§7Winner: §b" + winnerTeam.getLeaderName());
        } else {
            broadcastMessage("§7Result: §eDraw");
        }
        broadcastMessage("§7Final Score: §b" + this.teamOneWins + " §7- §b" + this.teamTwoWins);
        broadcastMessage("");
        super.end(winnerTeam);
    }
}