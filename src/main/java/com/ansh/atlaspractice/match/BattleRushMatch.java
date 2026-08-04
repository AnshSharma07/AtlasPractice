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
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.util.ActionBarUtil;
import com.ansh.atlaspractice.util.TitleUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * BattleRush match â€” 3-goal scoring, timed wool blocks, no bow, no breakable-block region.
 * Intentionally kept completely separate from BridgeMatch.
 */
@Getter
public class BattleRushMatch extends Match {

    /** First team to reach this many goals wins. */
    public static final int SCORE_LIMIT = 3;

    protected final Map<MatchTeam, Integer> teamScores = new HashMap<>();

    // â”€â”€ Wool tracking â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    /** Maps a placed-wool block key ("world:x:y:z") to the UUID of the player who placed it. */
    private final Map<String, UUID> placedWoolOwners = new HashMap<>();
    /** Maps a block key to the scheduled removal task so it can be cancelled early. */
    private final Map<String, BukkitRunnable> woolRemovalTasks = new HashMap<>();

    public BattleRushMatch(Kit kit, Arena arena, List<MatchTeam> teams, boolean ranked) {
        super(kit, arena, teams);
        setRanked(ranked);
        teams.forEach(t -> teamScores.put(t, 0));
    }

    // â”€â”€ Match lifecycle â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Override
    public void start() {
        this.setStartTime(System.currentTimeMillis());

        if (getArena() != null) {
            getArena().setState(com.ansh.atlaspractice.arena.ArenaState.ALLOCATED);
        }

        // Scoreboard colour teams (shared global scoreboard, same convention as Bridge)
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
                else        blue.addEntry(mp.getUsername());
            }
        }

        resetPlayersForNextRound();
    }

    // â”€â”€ Scoring â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public int getScore(MatchTeam team) {
        return teamScores.getOrDefault(team, 0);
    }

    /**
     * Called by BattleRushListener when a player steps into the enemy goal.
     */
    public void handleGoalScore(MatchTeam scoringTeam) {
        int fresh = teamScores.getOrDefault(scoringTeam, 0) + 1;
        teamScores.put(scoringTeam, fresh);

        broadcastMessage("§e§lGoal! §a" + scoringTeam.getLeaderName()
                + " §7scored! (§6" + fresh + "/" + SCORE_LIMIT + "§7)");

        if (fresh >= SCORE_LIMIT) {
            AtlasPracticePlugin.getInstance()
                    .getMatchManager()
                    .triggerMatchEndSequence(this, scoringTeam);
        } else {
            clearWoolTracking();
            resetPlayersForNextRound();
        }
    }

    // â”€â”€ Wool block tracking â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Registers a placed wool block and schedules its automatic removal after 10 seconds.
     *
     * @param blockKey  Serialised location key "world:x:y:z"
     * @param placerUuid UUID of the player who placed the block
     */
    public void trackWoolBlock(String blockKey, UUID placerUuid) {
        placedWoolOwners.put(blockKey, placerUuid);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                removeWoolBlock(blockKey, true);
            }
        };
        woolRemovalTasks.put(blockKey, task);
        task.runTaskLater(AtlasPracticePlugin.getInstance(), 200L); // 10 seconds
    }

    /**
     * Called when a player manually breaks a tracked wool block (before the 10-second timer).
     * Cancels the pending task and gives the wool back immediately.
     */
    public void onWoolBrokenByPlayer(String blockKey, Player breaker) {
        BukkitRunnable task = woolRemovalTasks.remove(blockKey);
        if (task != null) task.cancel();
        placedWoolOwners.remove(blockKey);
        returnWool(breaker);
    }

    /**
     * Removes a wool block from the world and optionally returns one wool to the original placer.
     */
    private void removeWoolBlock(String blockKey, boolean returnToOwner) {
        woolRemovalTasks.remove(blockKey);
        UUID ownerUuid = placedWoolOwners.remove(blockKey);

        String[] parts = blockKey.split(":");
        if (parts.length != 4) return;
        org.bukkit.World world = Bukkit.getWorld(parts[0]);
        if (world == null) return;
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        world.getBlockAt(x, y, z).setType(Material.AIR);

        if (returnToOwner && ownerUuid != null) {
            Player owner = Bukkit.getPlayer(ownerUuid);
            if (owner != null && owner.isOnline()) {
                returnWool(owner);
            }
        }
    }

    /** Gives one wool back; drops at player location if inventory is full. */
    private void returnWool(Player player) {
        short data = (short) (isRedTeam(getTeam(player)) ? 14 : 11);
        ItemStack wool = new ItemStack(Material.WOOL, 1, data);
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(wool);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }
    }

    /**
     * Cancels all pending wool timers and removes all placed wool blocks from the world.
     * Called on goal score or match end. Wool is NOT returned (players get a fresh kit).
     */
    public void clearWoolTracking() {
        // Cancel all pending tasks
        for (BukkitRunnable task : woolRemovalTasks.values()) {
            try { task.cancel(); } catch (IllegalStateException ignored) {}
        }
        woolRemovalTasks.clear();

        // Remove all placed blocks from the world
        for (String key : placedWoolOwners.keySet()) {
            String[] parts = key.split(":");
            if (parts.length != 4) continue;
            org.bukkit.World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;
            world.getBlockAt(
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            ).setType(Material.AIR);
        }
        placedWoolOwners.clear();
    }

    /** Returns true if the given block key belongs to a wool block placed by a player. */
    public boolean isTrackedWool(String blockKey) {
        return placedWoolOwners.containsKey(blockKey);
    }

    // â”€â”€ Team utilities â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public MatchTeam getTeam(Player player) {
        for (MatchTeam team : getTeams()) {
            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                if (mp.getUuid().equals(player.getUniqueId())) {
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
        if (spawn != null) player.teleport(spawn);
    }

    // â”€â”€ Round reset â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void resetPlayersForNextRound() {
        this.setState(MatchState.STARTING);
        this.setStartTime(System.currentTimeMillis());

        for (int i = 0; i < getTeams().size(); i++) {
            MatchTeam team = getTeams().get(i);
            Location spawn = i == 0 ? getArena().getSpawnRed() : getArena().getSpawnBlue();
            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                Player player = Bukkit.getPlayer(mp.getUuid());
                if (player == null || spawn == null) continue;
                applyRoundReset(player, spawn, team);
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
                    forEachOnlinePlayer(p -> {
                        TitleUtil.send(p, "§a§lFIGHT!", "§7Good luck!", 3, 14, 6);
                        ActionBarUtil.send(p, "");
                        p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0F, 2.0F);
                    });
                    cancel();
                    return;
                }

                String colour = countdown == 1 ? "§c§l" : countdown <= 3 ? "§e§l" : "§a§l";
                float pitch = 0.6F + (0.2F * (5 - countdown));

                forEachOnlinePlayer(p -> {
                    TitleUtil.send(p, colour + countdown, "§7Match starting...", 2, 22, 2);
                    ActionBarUtil.send(p,
                            "§7Starting in " + colour + countdown
                                    + " §7second" + (countdown == 1 ? "" : "s"));
                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 0.8F, pitch);
                });

                countdown--;
            }
        }.runTaskTimer(AtlasPracticePlugin.getInstance(), 0L, 20L);
    }

    private void applyRoundReset(Player player, Location spawn, MatchTeam team) {
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
        Profile profile = AtlasPracticePlugin.getInstance()
                .getProfileManager().getProfile(player.getUniqueId());
        if (profile != null) {
            getKit().applyToPlayer(player, profile, team.getTeamColor());
        } else {
            getKit().applyToPlayer(player, team.getTeamColor());
        }
    }

    private void forEachOnlinePlayer(Consumer<Player> action) {
        for (MatchTeam team : getTeams()) {
            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                Player player = Bukkit.getPlayer(mp.getUuid());
                if (player != null) action.accept(player);
            }
        }
    }
}