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

package com.ansh.atlaspractice.tasks;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaState;
import com.ansh.atlaspractice.arena.RuntimeArena;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.profile.ProfileState;
import com.ansh.atlaspractice.match.MatchContext;
import com.ansh.atlaspractice.match.MatchFactory;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.queue.QueueEntry;
import com.ansh.atlaspractice.team.TeamColor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@RequiredArgsConstructor
public final class QueueMatchmakingTask extends BukkitRunnable {

    private static final AtomicLong RUNTIME_COUNTER = new AtomicLong();

    private final AtlasPracticePlugin plugin;

    @Override
    public void run() {
        matchUnrankedQueue();
        matchRankedQueue();
    }

    private void matchRankedQueue() {
        List<QueueEntry> rankedCandidates = this.plugin.getQueueManager().getRankedQueue().getSnapshot();

        for (int i = 0; i < rankedCandidates.size(); i++) {
            QueueEntry firstCandidate = rankedCandidates.get(i);

            for (int j = i + 1; j < rankedCandidates.size(); j++) {
                QueueEntry secondCandidate = rankedCandidates.get(j);

                // Enforce mode uniformity matching filters
                if (!firstCandidate.getKit().getId().equals(secondCandidate.getKit().getId())) continue;

                if (evaluateRatingThresholds(firstCandidate, secondCandidate)) {
                    this.plugin.getQueueManager().getRankedQueue().removeEntry(firstCandidate.getEntryId());
                    this.plugin.getQueueManager().getRankedQueue().removeEntry(secondCandidate.getEntryId());

                    initializeMatchFromQueue(firstCandidate, secondCandidate);
                    return;
                }
            }
        }
    }

    private void matchUnrankedQueue() {
        List<QueueEntry> unrankedCandidates = this.plugin.getQueueManager().getUnrankedQueue().getSnapshot();

        for (int i = 0; i < unrankedCandidates.size(); i++) {
            QueueEntry firstCandidate = unrankedCandidates.get(i);
            for (int j = i + 1; j < unrankedCandidates.size(); j++) {
                QueueEntry secondCandidate = unrankedCandidates.get(j);
                if (!firstCandidate.getKit().getId().equals(secondCandidate.getKit().getId())) continue;

                this.plugin.getQueueManager().getUnrankedQueue().removeEntry(firstCandidate.getEntryId());
                this.plugin.getQueueManager().getUnrankedQueue().removeEntry(secondCandidate.getEntryId());
                initializeMatchFromQueue(firstCandidate, secondCandidate);
                return;
            }
        }
    }

    private boolean evaluateRatingThresholds(QueueEntry entryA, QueueEntry entryB) {
        int rangeA = 100 + (entryA.getSecondsInQueue() * 10);
        int rangeB = 100 + (entryB.getSecondsInQueue() * 10);
        int activeDelta = Math.abs(entryA.getBaselineElo() - entryB.getBaselineElo());

        return activeDelta <= rangeA || activeDelta <= rangeB;
    }

    private void initializeMatchFromQueue(QueueEntry entryA, QueueEntry entryB) {
        Player playerA = Bukkit.getPlayer(entryA.getEntryId());
        Player playerB = Bukkit.getPlayer(entryB.getEntryId());

        if (playerA == null || playerB == null) {
            resetQueuedProfile(entryA);
            resetQueuedProfile(entryB);
            return;
        }

        Kit kit = entryA.getKit();

        if (plugin.getSharedArenaService().getArenaIds(kit).isEmpty()) {

            playerA.sendMessage("§cThis kit has no arena assigned.");
            playerB.sendMessage("§cThis kit has no arena assigned.");

            return;
        }

        Arena arena = findAvailablePermanentArena(kit);

        if (arena == null) {
            if (!plugin.getConfig().getBoolean("runtime-overflow.enabled", false)) {
                playerA.sendMessage("§cAssigned arena could not be found.");
                playerB.sendMessage("§cAssigned arena could not be found.");
                resetQueuedProfile(entryA);
                resetQueuedProfile(entryB);
                return;
            }

            Arena sourceArena = findOverflowSourceArena(kit);
            if (sourceArena == null) {
                playerA.sendMessage("§cAssigned arena could not be found.");
                playerB.sendMessage("§cAssigned arena could not be found.");
                resetQueuedProfile(entryA);
                resetQueuedProfile(entryB);
                return;
            }

            createRuntimeAndStartMatch(sourceArena, entryA, entryB, playerA, playerB);
            return;
        }

        if (!isUsableArena(arena)) {

            playerA.sendMessage("§cAssigned arena is unavailable.");
            playerB.sendMessage("§cAssigned arena is unavailable.");

            resetQueuedProfile(entryA);
            resetQueuedProfile(entryB);

            return;
        }
        arena.setState(ArenaState.ALLOCATED);
        startMatch(entryA, entryB, playerA, playerB, arena);
    }

    private void startMatch(QueueEntry entryA, QueueEntry entryB, Player playerA, Player playerB, Arena arena) {
        MatchTeam teamA = new MatchTeam(
                List.of(new MatchTeam.MatchPlayer(playerA.getUniqueId(), playerA.getName())),
                TeamColor.RED
        );

        MatchTeam teamB = new MatchTeam(
                List.of(new MatchTeam.MatchPlayer(playerB.getUniqueId(), playerB.getName())),
                TeamColor.BLUE
        );

        arena.setState(ArenaState.ALLOCATED);

        try {

            boolean ranked = entryA.isRanked(); // both entries in the same queue share the same ranked flag
            Match match = MatchFactory.buildMatch(
                    MatchContext.builder().kit(entryA.getKit()).arena(arena).teams(Arrays.asList(teamA, teamB)).ranked(ranked).build());
            String foundMsg = ranked
                    ? "§aOpponent found! Starting ranked match..."
                    : "§aOpponent found! Starting match...";
            playerA.sendMessage(foundMsg);
            playerB.sendMessage(foundMsg);

            this.plugin.getMatchManager().hostMatch(match);

        } catch (Exception e) {

            if (arena instanceof RuntimeArena) {
                arena.cleanAndResetWorld();
            } else {
                arena.setState(ArenaState.FREE);
            }
            resetQueuedProfile(entryA);
            resetQueuedProfile(entryB);
            throw e;

        }
    }

    private Arena findAvailablePermanentArena(Kit kit) {
        return plugin.getSharedArenaService().getAvailableArenaPool(kit).stream()
                .filter(a -> a != null && !(a instanceof RuntimeArena) && a.isAvailable())
                .findAny()
                .orElse(null);
    }

    private Arena findOverflowSourceArena(Kit kit) {
        return plugin.getSharedArenaService().getAvailableArenaPool(kit).stream()
                .filter(a -> a != null && !(a instanceof RuntimeArena) && isUsableArena(a))
                .findAny()
                .orElse(null);
    }

    private boolean isUsableArena(Arena arena) {
        return arena != null
                && !arena.isDisabled()
                && arena.getSpawnRed() != null
                && arena.getSpawnBlue() != null
                && arena.getTemplateWorld() != null
                && !arena.getTemplateWorld().isBlank();
    }

    private void createRuntimeAndStartMatch(Arena sourceArena, QueueEntry entryA, QueueEntry entryB, Player playerA, Player playerB) {
        String runtimeWorldName = sourceArena.getWorldName() + "_temp_" + RUNTIME_COUNTER.incrementAndGet();
        String runtimeId = sourceArena.getId() + "#runtime#" + RUNTIME_COUNTER.get();
        RuntimeArena runtimeArena = new RuntimeArena(runtimeId, sourceArena, runtimeWorldName);
        plugin.getArenaManager().registerArena(runtimeArena);
        plugin.getLogger().info("[AtlasPractice] Creating overflow runtime for " + sourceArena.getId());

        plugin.getWorldService().loadArenaWorld(runtimeArena).whenComplete((world, throwable) -> {
            if (throwable != null) {
                plugin.getArenaManager().unregisterArena(runtimeArena);
                plugin.getLogger().warning("[AtlasPractice] Failed to create overflow runtime " + runtimeWorldName + ": " + throwable.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    playerA.sendMessage("§cNo arena is available right now. Please try again.");
                    playerB.sendMessage("§cNo arena is available right now. Please try again.");
                    resetQueuedProfile(entryA);
                    resetQueuedProfile(entryB);
                });
                return;
            }

            runtimeArena.bindToWorld(world);
            plugin.getLogger().info("[AtlasPractice] Runtime loaded: " + runtimeWorldName);
            Bukkit.getScheduler().runTask(plugin, () -> startMatch(entryA, entryB, playerA, playerB, runtimeArena));
        });
    }

    private void resetQueuedProfile(QueueEntry entry) {
        Profile profile = this.plugin.getProfileManager().getProfile(entry.getEntryId());
        if (profile != null && profile.getState() == ProfileState.QUEUING) {
            profile.setState(ProfileState.LOBBY);
        }
    }
}