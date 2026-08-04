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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaMode;
import com.ansh.atlaspractice.cosmetics.message.KillMessage;
import com.ansh.atlaspractice.cosmetics.message.KillMessageType;
import com.ansh.atlaspractice.match.BattleRushMatch;
import com.ansh.atlaspractice.match.BridgeMatch;
import com.ansh.atlaspractice.match.DuelMatch;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.party.PartyFFAMatch;
import com.ansh.atlaspractice.party.PartyMatch;
import com.ansh.atlaspractice.party.PartyPvPMatch;
import com.ansh.atlaspractice.profile.KitStats;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public final class MatchDeathListener implements Listener {

    private final AtlasPracticePlugin plugin;

    public MatchDeathListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        if (victim.getGameMode() == GameMode.SPECTATOR) {
            event.setCancelled(true);
            return;
        }

        UUID matchId = plugin.getMatchManager().getPlayerMatchId(victim.getUniqueId());
        if (matchId == null) return;

        Match match = plugin.getMatchManager().getLiveMatch(matchId);
        if (match == null) return;

        if (match.getState() != Match.MatchState.FIGHTING) {
            event.setCancelled(true);
            return;
        }

        if ((match.getArena().getMode() == ArenaMode.BEDFIGHT
                || match.getArena().getMode() == ArenaMode.BRIDGE
                || match.getArena().getMode() == ArenaMode.BATTLERUSH)
                && event.getCause() == EntityDamageEvent.DamageCause.FALL) {

            event.setCancelled(true);
            return;
        }

        if (victim.getHealth() - event.getFinalDamage() <= 0.0D) {
            event.setCancelled(true);
            handleDeath(victim, match, false);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getFrom().getY() == event.getTo().getY()) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        UUID matchId = plugin.getMatchManager().getPlayerMatchId(player.getUniqueId());
        if (matchId == null) return;

        Match match = plugin.getMatchManager().getLiveMatch(matchId);
        if (match == null || match.getState() != Match.MatchState.FIGHTING) return;
        if (match.getKit().getId().equalsIgnoreCase("SUMO")) { return; }

        Arena arena = match.getArena();
        Location min = arena.getMinimumBoundary();
        Location max = arena.getMaximumBoundary();

        double deathY = Math.min(min.getY(), max.getY());
        if (event.getTo().getY() <= deathY) {
            event.setTo(event.getFrom());
            if (match instanceof PartyPvPMatch &&
                    ((PartyPvPMatch) match).isEliminated(player)) {
                return;
            }

            if (match instanceof PartyMatch &&
                    ((PartyMatch) match).isEliminated(player)) {
                return;
            }
            handleDeath(player, match, true);
        }
    }

    private void handleDeath(Player victim, Match match, boolean fellInVoid) {
        if (match.getKit().getId().equalsIgnoreCase("BOXING")) {
            return;
        }
        resetPlayerEntity(victim);

        UUID killerUuid = plugin.getMatchManager().getLastAttacker(victim.getUniqueId());
        Player killer = killerUuid != null ? Bukkit.getPlayer(killerUuid) : null;

        String deathMessage;

        if (killer != null) {
            plugin.getCosmeticManager().playKillEffect(killer, victim);

            KillMessageType type = KillMessageType.PVP;
            EntityDamageEvent damageEvent = victim.getLastDamageCause();
            if (damageEvent != null) {
                EntityDamageEvent.DamageCause cause = damageEvent.getCause();
                switch (cause) {
                    case VOID:
                        type = KillMessageType.VOID;
                        break;
                    case PROJECTILE:
                        type = KillMessageType.SHOOT;
                        break;
                    case BLOCK_EXPLOSION:
                    case ENTITY_EXPLOSION:
                        type = KillMessageType.EXPLOSION;
                        break;
                    default:
                        type = KillMessageType.PVP;
                        break;
                }
            }

            // Let CosmeticManager fetch the killer's active KillMessage automatically
            KillMessage killMessage = plugin.getCosmeticManager().getKillMessage(killer);
            deathMessage = killMessage.format(killer, victim, type);

        } else if (fellInVoid) {
            deathMessage = "§c" + victim.getName() + " §7fell into the void";
        } else {
            deathMessage = "§c" + victim.getName() + " §7died";
        }

        for (MatchTeam team : match.getTeams()) {
            team.getPlayers().forEach(mp -> {
                Player p = Bukkit.getPlayer(mp.getUuid());
                if (p != null && p.isOnline()) {
                    p.sendMessage(deathMessage);
                }
            });
        }

        plugin.getMatchManager().clearCombatTracking(victim.getUniqueId());

        boolean isBedfight = match.getArena().getBedRed() != null && match.getArena().getBedBlue() != null;
        String kitId = match.getKit().getId();

        Profile victimProfile = plugin.getProfileManager().getProfile(victim.getUniqueId());
        if (victimProfile != null) {
            victimProfile.addDeath();
            KitStats victimKitStats = victimProfile.getKitStats(kitId);
            victimKitStats.addDeath();
            plugin.getLevelManager().reward(victimProfile, "death");

            if (match.isRanked()) {
                int loseElo = plugin.getRankedConfig().getLoseElo();
                victimKitStats.setElo(Math.max(0, victimKitStats.getElo() - loseElo));
            }
            plugin.getDatabaseService().saveProfileDataAsync(victimProfile);
        }

        if (killer != null) {
            Profile killerProfile = plugin.getProfileManager().getProfile(killer.getUniqueId());
            if (killerProfile != null) {
                killerProfile.addKill();
                KitStats killerKitStats = killerProfile.getKitStats(kitId);
                killerKitStats.addKill();
                plugin.getLevelManager().reward(killerProfile, "kill");

                if (match.isRanked()) {
                    int winElo = plugin.getRankedConfig().getWinElo();
                    killerKitStats.setElo(killerKitStats.getElo() + winElo);
                }
                plugin.getDatabaseService().saveProfileDataAsync(killerProfile);
            }
        }

        if (match instanceof PartyFFAMatch) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> restorePlayerEntity(victim), 60L);
            ((PartyFFAMatch) match).handleDeath(victim, killer);
            return;
        }

        if (match instanceof BridgeMatch) {
            MatchTeam team = ((BridgeMatch) match).getTeam(victim);
            int teamIndex = match.getTeams().indexOf(team);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (match.getState() != Match.MatchState.FIGHTING) {
                    restorePlayerEntity(victim);
                    return;
                }
                victim.teleport(teamIndex == 0 ? match.getArena().getSpawnRed() : match.getArena().getSpawnBlue());
                restorePlayerEntity(victim);
                Profile profile = plugin.getProfileManager().getProfile(victim.getUniqueId());
                if (profile != null) {
                    match.getKit().applyToPlayer(victim, profile, match.getTeamColor(victim));
                } else {
                    match.getKit().applyToPlayer(victim, match.getTeamColor(victim));
                }
                victim.sendMessage("§aRespawned!");
            }, 1L);
            return;
        }

        if (match instanceof BattleRushMatch) {
            BattleRushMatch brMatch = (BattleRushMatch) match;
            MatchTeam team = brMatch.getTeam(victim);
            int teamIndex = match.getTeams().indexOf(team);

            new BukkitRunnable() {
                int seconds = 3;

                @Override
                public void run() {
                    if (match.getState() != Match.MatchState.FIGHTING) {
                        restorePlayerEntity(victim);
                        cancel();
                        return;
                    }

                    if (seconds > 0) {
                        victim.sendMessage("§eRespawning in §c" + seconds + "§e second" + (seconds == 1 ? "" : "s") + "...");
                        victim.playSound(victim.getLocation(), Sound.NOTE_STICKS, 1.0F, 1.0F);
                        seconds--;
                        return;
                    }

                    victim.teleport(teamIndex == 0 ? match.getArena().getSpawnRed() : match.getArena().getSpawnBlue());
                    restorePlayerEntity(victim);

                    Profile profile = plugin.getProfileManager().getProfile(victim.getUniqueId());
                    if (profile != null) {
                        match.getKit().applyToPlayer(victim, profile, match.getTeamColor(victim));
                    } else {
                        match.getKit().applyToPlayer(victim, match.getTeamColor(victim));
                    }

                    victim.sendMessage("§aRespawned!");
                    victim.playSound(victim.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                    cancel();
                }
            }.runTaskTimer(plugin, 0L, 20L);

            return;
        }

        if (isBedfight && match.getTeams().size() >= 2) {
            int teamIndex = match.getTeams().get(0).getPlayers().stream()
                    .anyMatch(mp -> mp.getUuid().equals(victim.getUniqueId())) ? 0 : 1;

            boolean canRespawn = (teamIndex == 0) ? match.isRedBedAlive() : match.isBlueBedAlive();

            if (canRespawn) {
                new BukkitRunnable() {
                    int seconds = 3;

                    @Override
                    public void run() {
                        if (match.getState() != Match.MatchState.FIGHTING) {
                            restorePlayerEntity(victim);
                            cancel();
                            return;
                        }

                        if (seconds > 0) {
                            victim.sendMessage("§eRespawning in §c" + seconds + "§e second" + (seconds == 1 ? "" : "s") + "...");
                            victim.playSound(victim.getLocation(), Sound.NOTE_STICKS, 1.0F, 1.0F);
                            seconds--;
                            return;
                        }

                        if (match.getState() != Match.MatchState.FIGHTING) {
                            restorePlayerEntity(victim);
                            cancel();
                            return;
                        }

                        if (teamIndex == 0) {
                            victim.teleport(match.getArena().getSpawnRed());
                        } else {
                            victim.teleport(match.getArena().getSpawnBlue());
                        }

                        restorePlayerEntity(victim);

                        Profile profile = plugin.getProfileManager().getProfile(victim.getUniqueId());
                        if (profile != null) {
                            match.getKit().applyToPlayer(victim, profile, match.getTeamColor(victim));
                        } else {
                            match.getKit().applyToPlayer(victim, match.getTeamColor(victim));
                        }
                        victim.sendMessage("§aRespawned!");
                        victim.playSound(victim.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                        cancel();
                    }
                }.runTaskTimer(plugin, 0L, 20L);

                return;
            }
        }

        if (match instanceof PartyMatch) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> restorePlayerEntity(victim), 60L);
            ((PartyMatch) match).handleDeath(victim);
            return;
        }

        if (match instanceof PartyPvPMatch) {
            ((PartyPvPMatch) match).handleDeath(victim);
            return;
        }

        MatchTeam winningTeam = match.getTeams().stream()
                .filter(team -> team.getPlayers().stream().noneMatch(mp -> mp.getUuid().equals(victim.getUniqueId())))
                .findFirst().orElse(match.getTeams().get(0));

        if (match instanceof DuelMatch) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> restorePlayerEntity(victim), 1L);
            ((DuelMatch) match).handleRoundWin(winningTeam);
            return;
        }

        plugin.getMatchManager().triggerMatchEndSequence(match, winningTeam);
    }

    private void resetPlayerEntity(Player victim) {
        victim.setHealth(20.0D);
        victim.setFoodLevel(20);
        victim.setSaturation(2.0F);
        victim.setExhaustion(0.0F);
        victim.setFireTicks(0);
        victim.setFallDistance(0.0F);
        victim.setNoDamageTicks(0);
        victim.setGameMode(GameMode.SPECTATOR);
        victim.setAllowFlight(true);
        victim.setFlying(true);
        victim.setWalkSpeed(0.2F);
    }

    private void restorePlayerEntity(Player player) {
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(2.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        player.setNoDamageTicks(0);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setWalkSpeed(0.2F);
    }
}