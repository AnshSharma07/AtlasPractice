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

import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.SharedArena;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.util.ActionBarUtil;
import com.ansh.atlaspractice.util.TitleUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class BridgeMatch extends Match {

    protected final Map<MatchTeam, Integer> teamScores = new HashMap<>();
    protected static final int SCORE_LIMIT = 5;

    public BridgeMatch(Kit kit, Arena arena, List<MatchTeam> teams, boolean ranked) {
        super(kit, arena, teams);
        setRanked(ranked);
        teams.forEach(team -> this.teamScores.put(team, 0));
    }
    @Override
    public void start() {
        // Apply scoreboard teams and arena state from the base class,
        // but DO NOT set state to FIGHTING yet. Run the countdown instead.
        this.setStartTime(System.currentTimeMillis());

        if (getArena() != null) {
            getArena().setState(com.ansh.atlaspractice.arena.ArenaState.ALLOCATED);
        }

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team red = board.getTeam("ap_red");
        if (red == null) {
            red = board.registerNewTeam("ap_red");
            red.setPrefix("§c");
        }
        Team blue = board.getTeam("ap_blue");
        if (blue == null) {
            blue = board.registerNewTeam("ap_blue");
            blue.setPrefix("§9");
        }
        for (int i = 0; i < getTeams().size(); i++) {
            MatchTeam team = getTeams().get(i);
            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                if (i == 0) red.addEntry(mp.getUsername());
                else blue.addEntry(mp.getUsername());
            }
        }

        // Delegate to the countdown-based reset, which handles teleport + kit + STARTING state.
        resetPlayersForNextRound();
    }

    public int getScore(MatchTeam team) {
        return this.teamScores.getOrDefault(team, 0);
    }

    public void handleGoalScore(MatchTeam team) {
        int freshScore = this.teamScores.getOrDefault(team, 0) + 1;
        this.teamScores.put(team, freshScore);

        broadcastMessage("§e§lGoal! §a" + team.getLeaderName() + " §7scored! (§6" + freshScore + "/" + SCORE_LIMIT + "§7)");

        if (freshScore >= SCORE_LIMIT) {
            AtlasPracticePlugin.getInstance()
                    .getMatchManager()
                    .triggerMatchEndSequence(this, team);
        }  else {
            restorePlacedBlocks();
            resetPlayersForNextRound();
        }
    }

    public MatchTeam getTeam(Player player) {
        for (MatchTeam team : getTeams()) {
            for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {
                if (matchPlayer.getUuid().equals(player.getUniqueId())) {
                    return team;
                }
            }
        }
        return null;
    }

    public boolean isRedTeam(MatchTeam team) {
        return !getTeams().isEmpty() && getTeams().get(0) == team;
    }

    public void teleportToSpawn(Player player) {
        MatchTeam team = getTeam(player);
        Location spawn = isRedTeam(team) ? getArena().getSpawnRed() : getArena().getSpawnBlue();
        if (spawn != null) {
            player.teleport(spawn);
        }
    }
    private void restorePlacedBlocks() {
        // 1. Clear player-placed blocks (set back to AIR)
        for (String key : getPlacedBlocks()) {
            String[] parts = key.split(":");
            if (parts.length != 4) continue;
            org.bukkit.World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            world.getBlockAt(x, y, z).setType(org.bukkit.Material.AIR);
        }
        getPlacedBlocks().clear();

        // 2. Restore broken arena blocks from the BlockTracker snapshot
        if (getArena() instanceof com.ansh.atlaspractice.arena.SharedArena sharedArena) {
            for (com.ansh.atlaspractice.arena.BlockTracker.BlockSnapshot snapshot
                    : sharedArena.getBlockTracker().compileReversionSnapshots()) {
                org.bukkit.block.Block block = snapshot.location().getBlock();
                block.setType(snapshot.material());
                block.setData(snapshot.data());
            }
            sharedArena.getBlockTracker().clear();
        }
    }
    private void resetPlayersForNextRound() {
        this.setState(MatchState.STARTING);
        this.setStartTime(System.currentTimeMillis());

        for (int i = 0; i < this.getTeams().size(); i++) {
            MatchTeam team = this.getTeams().get(i);
            Location spawn = i == 0 ? this.getArena().getSpawnRed() : this.getArena().getSpawnBlue();
            for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {
                Player player = Bukkit.getPlayer(matchPlayer.getUuid());
                if (player == null || spawn == null) {
                    continue;
                }
                resetPlayer(player, spawn, team);
            }
        }

        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (getState() == MatchState.ENDING) {
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    setState(MatchState.FIGHTING);

                    forEachOnlinePlayer(player -> {
                        // Big green FIGHT! title
                        TitleUtil.send(player,
                                "§a§lFIGHT!",
                                "§7Good luck!",
                                3, 14, 6   // 3 ticks fade-in, 14 stay (~0.7 s), 6 fade-out
                        );
                        // Clear action bar
                        ActionBarUtil.send(player, "");
                        // Satisfying high ding
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 2.0F);
                    });

                    cancel();
                    return;
                }

                // Choose colour: red on 1, yellow on 2-3, green on 4-5
                String colour = countdown == 1 ? "§c§l"
                        : countdown <= 3 ? "§e§l"
                          : "§a§l";

                // Sound pitch rises with each tick so it feels like build-up
                float pitch = 0.6F + (0.2F * (5 - countdown));   // 0.6 â†’ 1.4

                forEachOnlinePlayer(player -> {
                    // Large number in the centre of the screen
                    TitleUtil.send(player,
                            colour + countdown,
                            "§7Match starting...",
                            2, 22, 2   // almost no fade, stay just over 1 second
                    );
                    // Redundant but useful: action bar shows the same thing
                    ActionBarUtil.send(player,
                            "§7Starting in " + colour + countdown + " §7second" + (countdown == 1 ? "" : "s")
                    );
                    // Ticking note-block click
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.8F, pitch);
                });

                countdown--;
            }
        }.runTaskTimer(AtlasPracticePlugin.getInstance(), 0L, 20L);
    }

    private void resetPlayer(Player player, Location spawn, MatchTeam team) {
        player.teleport(spawn);
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(2.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        player.setNoDamageTicks(0);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        Profile profile = AtlasPracticePlugin.getInstance().getProfileManager().getProfile(player.getUniqueId());
        if (profile != null) {
            this.getKit().applyToPlayer(player, profile, team.getTeamColor());
        } else {
            this.getKit().applyToPlayer(player, team.getTeamColor());
        }
    }
    private void forEachOnlinePlayer(java.util.function.Consumer<Player> action) {
        for (MatchTeam team : getTeams()) {
            for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {
                Player player = Bukkit.getPlayer(matchPlayer.getUuid());
                if (player != null) {
                    action.accept(player);
                }
            }
        }
    }
    private void playSoundToAll(Sound sound, float volume, float pitch) {
        for (MatchTeam team : getTeams()) {
            for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {
                Player player = Bukkit.getPlayer(matchPlayer.getUuid());
                if (player != null) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            }
        }
    }
}