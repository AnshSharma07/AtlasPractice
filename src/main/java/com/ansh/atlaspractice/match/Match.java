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
import com.ansh.atlaspractice.bots.PracticeBot;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.profile.KitStats;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.team.TeamColor;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public abstract class Match {

    private final UUID id;
    private final Kit kit;
    private final Arena arena;
    private final List<MatchTeam> teams;
    private boolean ranked = false;

    private final List<UUID> spectators;
    private final Set<String> placedBlocks;
    private MatchState state;
    private long startTime;
    private boolean redBedAlive = true;
    private boolean blueBedAlive = true;
    private final Map<UUID, Integer> boxingHits = new HashMap<>();
    private final BoxingComboTracker comboTracker = new BoxingComboTracker();
    public Match(Kit kit, Arena arena, List<MatchTeam> teams) {

        this.id = UUID.randomUUID();

        this.kit = kit;
        this.arena = arena;
        this.teams = teams;

        this.spectators = new ArrayList<>();
        this.placedBlocks = new HashSet<>();

        this.state = MatchState.STARTING;
    }
    private void enableHealthBelowName() {
        for (MatchTeam team : teams) {
            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {

                Player player = Bukkit.getPlayer(mp.getUuid());

                if (player == null) continue;

                Objective hp = player.getScoreboard().getObjective("atlas_hp");

                if (hp != null) {
                    hp.setDisplaySlot(DisplaySlot.BELOW_NAME);
                }
            }
        }
    }
    private void disableHealthBelowName() {
        for (MatchTeam team : teams) {
            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                Player player = Bukkit.getPlayer(mp.getUuid());
                if (player == null) continue;
                Objective hp = player.getScoreboard().getObjective("atlas_hp");
                if (hp != null) {hp.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                }}
        }
    }
    public boolean isRanked() { return ranked; }
    protected void setRanked(boolean ranked) { this.ranked = ranked; }

    public UUID getId() {
        return id;
    }

    public Kit getKit() {
        return kit;
    }

    public Arena getArena() {
        return arena;
    }

    public List<MatchTeam> getTeams() {
        return teams;
    }

    public List<UUID> getSpectators() {
        return spectators;
    }

    public MatchState getState() {
        return state;
    }

    public void setState(MatchState state) {
        this.state = state;
    }

    public long getStartTime() {
        return startTime;
    }
    public BoxingComboTracker getComboTracker() {
        return comboTracker;
    }
    protected void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isRedBedAlive() {
        return redBedAlive;
    }

    public void setRedBedAlive(boolean redBedAlive) {
        this.redBedAlive = redBedAlive;
    }

    public boolean isBlueBedAlive() {
        return blueBedAlive;
    }

    public void setBlueBedAlive(boolean blueBedAlive) {
        this.blueBedAlive = blueBedAlive;
    }

    public Set<String> getPlacedBlocks() {
        return placedBlocks;
    }

    public int getBoxingHits(UUID uuid) {
        return boxingHits.getOrDefault(uuid, 0);
    }

    public int addBoxingHit(UUID uuid) {
        int hits = boxingHits.getOrDefault(uuid, 0) + 1;
        boxingHits.put(uuid, hits);
        return hits;
    }

    public void clearBoxingHits() {
        boxingHits.clear();
        comboTracker.clear();
    }

    public void start() {
        System.out.println(
                "[ATLAS-MATCH] START " +
                        arena.getWorld().getName());
        this.startTime = System.currentTimeMillis();
        this.clearBoxingHits();
        this.state = MatchState.FIGHTING;
        enableHealthBelowName();
        this.redBedAlive = true;
        this.blueBedAlive = true;

        Scoreboard board =
                Bukkit.getScoreboardManager()
                        .getMainScoreboard();

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

        if (arena != null) {
            arena.setState(ArenaState.ALLOCATED);
        }

        for (int teamIndex = 0;
             teamIndex < this.teams.size();
             teamIndex++) {

            MatchTeam team = this.teams.get(teamIndex);
            boolean redTeam = teamIndex == 0;
            org.bukkit.Location spawn =
                    teamIndex == 0
                            ? this.arena.getSpawnRed()
                            : this.arena.getSpawnBlue();

            for (MatchTeam.MatchPlayer matchPlayer
                    : team.getPlayers()) {
                if (redTeam) {
                    red.addEntry(matchPlayer.getUsername());
                } else {
                    blue.addEntry(matchPlayer.getUsername());
                }
                Player player =
                        Bukkit.getPlayer(
                                matchPlayer.getUuid()
                        );

                if (player == null) {
                    continue;
                }

                if (spawn != null) {
                    player.teleport(spawn);
                }

                Profile profile = com.ansh.atlaspractice.AtlasPracticePlugin
                        .getInstance()
                        .getProfileManager()
                        .getProfile(player.getUniqueId());

                if (profile != null) {
                    kit.applyToPlayer(
                            player,
                            profile,
                            team.getTeamColor()
                    );
                    if (kit.isComboMode()) {
                        player.setMaximumNoDamageTicks(5);
                    }
                } else {
                    kit.applyToPlayer(
                            player,
                            profile,
                            team.getTeamColor()
                    );
                    if (kit.isBoxingMode()) {
                        player.addPotionEffect(
                                new org.bukkit.potion.PotionEffect(
                                        org.bukkit.potion.PotionEffectType.SPEED,
                                        Integer.MAX_VALUE,
                                        1,
                                        false,
                                        false
                                ),
                                true
                        );
                    }
                    if (kit.isComboMode()) {
                        player.setMaximumNoDamageTicks(5);
                    }
                }
            }
        }
        broadcastMessage(
                "§aThe match has officially begun! Good luck."
        );
    }
    public void end(MatchTeam winnerTeam) {
        System.out.println(
                "[ATLAS-MATCH] END " +
                        arena.getWorld().getName());
        for (String key : placedBlocks) {

            String[] parts = key.split(":");

            if (parts.length != 4) {
                continue;
            }

            World world =
                    Bukkit.getWorld(parts[0]);

            if (world == null) {
                continue;
            }

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            world.getBlockAt(x, y, z)
                    .setType(Material.AIR);
        }

        placedBlocks.clear();

        this.state = MatchState.ENDING;
        disableHealthBelowName();
        if (winnerTeam != null) {

            String kitId = getKit().getId();
            for (MatchTeam team : getTeams()) {
                boolean won = team == winnerTeam;
                for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {
                    Player player = Bukkit.getPlayer(matchPlayer.getUuid());
                    if (player == null) {
                        continue;
                    }
                    Profile profile = AtlasPracticePlugin.getInstance()
                            .getProfileManager()
                            .getProfile(player.getUniqueId());

                    if (profile == null) {
                        continue;
                    }

                    KitStats stats = profile.getKitStats(kitId);

                    if (won) {
                        profile.addWin();
                        stats.addWin();
                        AtlasPracticePlugin.getInstance().getLevelManager().reward(profile, this instanceof BotMatch ? "bot-win" : (isRanked() ? "ranked-win" : "win"));
                        AtlasPracticePlugin.getInstance().getLevelManager().reward(profile, "play-match");
                    } else {
                        profile.addLoss();
                        stats.addLoss();
                        AtlasPracticePlugin.getInstance().getLevelManager().reward(profile, this instanceof BotMatch ? "bot-loss" : (isRanked() ? "ranked-loss" : "loss"));
                        AtlasPracticePlugin.getInstance().getLevelManager().reward(profile, "play-match");
                    }
                }
            }
        }
        broadcastMessage(
                "§6§lMatch Over! §eWinners: §a"
                        + winnerTeam.getLeaderName()
        );

        if (winnerTeam != null) {
            for (MatchTeam.MatchPlayer matchPlayer : winnerTeam.getPlayers()) {
                Player winner = Bukkit.getPlayer(matchPlayer.getUuid());
                if (winner != null && winner.isOnline()) {
                    AtlasPracticePlugin.getInstance().getCosmeticManager().playVictoryEffect(winner);
                }
            }
        }
        if (arena != null) {
            arena.cleanAndResetWorld();
        }
    }

    public void handleBotAttack(PracticeBot bot, Player victim) {

        if (getState() != MatchState.FIGHTING) {
            return;
        }

        if (getKit().isBoxingMode()) {
            victim.setNoDamageTicks(0);
            victim.setHealth(20.0);

            int hits = addBoxingHit(bot.getBotNPC().getUuid());

            Player botPlayer = bot.getBukkitEntity();

            if (botPlayer != null) {
                botPlayer.sendMessage("§6Hits: §e" + hits + "§7/100");
            }
            victim.sendMessage(
                    "§c" + bot.getBukkitEntity().getName()
                            + " §7has §e"
                            + hits
                            + "§7/100 hits."
            );
            if (hits >= 95) {
            }
            if (hits >= 100) {
                MatchTeam winningTeam = getTeams().stream()
                        .filter(team -> team.getPlayers().stream()
                                .anyMatch(mp -> mp.getUuid().equals(bot.getBotNPC().getUuid())))
                        .findFirst()
                        .orElse(null);

                if (this instanceof DuelMatch) {
                    ((DuelMatch) this).handleRoundWin(winningTeam);
                } else {
                    AtlasPracticePlugin.getInstance()
                            .getMatchManager()
                            .triggerMatchEndSequence(this, winningTeam);
                }
            }

            return;
        }

        EntityPlayer attacker = bot.getBotNPC().getEntityPlayer();
        attacker.setSprinting(false);
        attacker.attack(((CraftPlayer) victim).getHandle());
    }

    public TeamColor getTeamColor(Player player) {

        UUID uuid = player.getUniqueId();

        for (MatchTeam team : teams) {

            for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {

                if (matchPlayer.getUuid().equals(uuid)) {
                    return team.getTeamColor();
                }
            }
        }

        return null;
    }

    public void broadcastMessage(String message) {
        this.teams.forEach(team ->
                team.getPlayers().forEach(matchPlayer -> {

                    Player player =
                            Bukkit.getPlayer(
                                    matchPlayer.getUuid()
                            );
                    if (player != null) {
                        player.sendMessage(message);
                    }
                })
        );
        this.spectators.forEach(uuid -> {

            Player player =
                    Bukkit.getPlayer(uuid);

            if (player != null) {
                player.sendMessage(message);
            }
        });
    }

    public enum MatchState {
        STARTING,
        FIGHTING,
        ENDING
    }
}