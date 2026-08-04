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

package com.ansh.atlaspractice.party;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.arena.ArenaState;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.team.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class PartyFFAMatch extends Match {

    private final Party party;
    private final Set<UUID> eliminatedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Integer> killTracker = new ConcurrentHashMap<>();

    public PartyFFAMatch(Party party, Kit kit, Arena arena) {
        super(kit, arena, new ArrayList<>());
        this.party = party;

        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);

            TeamColor color = (this.getTeams().size() % 2 == 0)
                    ? TeamColor.RED
                    : TeamColor.BLUE;

            MatchTeam soloTeam = new MatchTeam(
                    new ArrayList<>(Collections.singletonList(
                            new MatchTeam.MatchPlayer(
                                    memberId,
                                    member.getName()
                            )
                    )),
                    color
            );

            this.getTeams().add(soloTeam);
        }
    }

    public Party getParty() {
        return this.party;
    }

    @Override
    public void start() {
        this.setState(MatchState.STARTING);

        if (this.getArena() != null) {
            this.getArena().setState(ArenaState.ALLOCATED);
        }

        // same spawn for alll players
        Location sharedBase = (ThreadLocalRandom.current().nextBoolean())
                ? this.getArena().getSpawnRed()
                : this.getArena().getSpawnBlue();

        for (MatchTeam team : this.getTeams()) {
            for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {
                Player player = Bukkit.getPlayer(matchPlayer.getUuid());

                if (player == null) {
                    continue;
                }

                if (sharedBase != null) {
                    Location spawn = sharedBase.clone();

                    double offsetX = ThreadLocalRandom.current()
                            .nextDouble(-3.0, 3.0);

                    double offsetZ = ThreadLocalRandom.current()
                            .nextDouble(-3.0, 3.0);

                    spawn.add(offsetX, 0.5D, offsetZ);

                    player.teleport(spawn);
                }

                this.getKit().applyToPlayer(player);
            }
        }

        if (this.party != null) {
            this.party.setState(PartyState.FIGHTING);
        }
        new BukkitRunnable() {

            int countdown = 5;

            @Override
            public void run() {

                if (countdown == 0) {

                    setState(MatchState.FIGHTING);
                    setStartTime(System.currentTimeMillis());
                    for (MatchTeam team : getTeams()) {
                        for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                            Player player = Bukkit.getPlayer(mp.getUuid());
                            if (player == null) {
                                continue;
                            }
                            player.sendTitle("§a§lFIGHT!", "");
                            player.playSound(player.getLocation(),
                                    org.bukkit.Sound.NOTE_PLING,
                                    1F,
                                    2F);
                        }
                    }

                    broadcastMessage("§a§lFight!");
                    cancel();
                    return;
                }

                for (MatchTeam team : getTeams()) {
                    for (MatchTeam.MatchPlayer mp : team.getPlayers()) {

                        Player player = Bukkit.getPlayer(mp.getUuid());

                        if (player == null) {
                            continue;
                        }
                        player.sendTitle(
                                "§e" + countdown, "§7Prepare..."
                        );

                        player.playSound(player.getLocation(),
                                org.bukkit.Sound.NOTE_PLING,
                                1F,
                                1F);
                    }
                }

                countdown--;

            }

        }.runTaskTimer(AtlasPracticePlugin.getInstance(), 0L, 20L);
    }

    public void handleDeath(Player victim) {
        handleDeath(victim, victim.getKiller());
    }

    public void handleDeath(Player victim, Player killer) {
        if (!this.eliminatedPlayers.add(victim.getUniqueId())) {
            return;
        }

        if (killer != null
                && !killer.getUniqueId().equals(victim.getUniqueId())) {

            this.killTracker.put(
                    killer.getUniqueId(),
                    this.killTracker.getOrDefault(
                            killer.getUniqueId(),
                            0
                    ) + 1
            );
        }

        this.getSpectators().add(victim.getUniqueId());

        int remaining =
                this.getTeams().size() - this.eliminatedPlayers.size();

        broadcastMessage(
                "§c" + victim.getName()
                        + " has been eliminated! §e("
                        + remaining
                        + " remaining)"
        );

        AtlasPracticePlugin.getInstance()
                .getMatchManager()
                .makeInMatchSpectator(victim, this);

        checkWinCondition();
    }

    private void checkWinCondition() {
        int aliveCount = 0;
        MatchTeam winningTeam = null;

        for (MatchTeam team : this.getTeams()) {
            boolean alive = false;

            for (MatchTeam.MatchPlayer player : team.getPlayers()) {
                if (!this.eliminatedPlayers.contains(player.getUuid())) {
                    alive = true;
                    break;
                }
            }

            if (alive) {
                aliveCount++;
                winningTeam = team;
            }
        }

        if (aliveCount <= 1) {
            AtlasPracticePlugin.getInstance()
                    .getMatchManager()
                    .triggerMatchEndSequence(
                            this,
                            winningTeam
                    );
        }
    }

    @Override
    public void end(MatchTeam winnerTeam) {
        List<Map.Entry<UUID, Integer>> topKillers =
                new ArrayList<>(this.killTracker.entrySet());

        topKillers.sort(
                (a, b) -> Integer.compare(
                        b.getValue(),
                        a.getValue()
                )
        );

        broadcastMessage("§8§m---------------------------------");
        broadcastMessage("§6§lTop Killers:");

        int rank = 1;

        for (int i = 0;
             i < Math.min(3, topKillers.size());
             i++) {

            Map.Entry<UUID, Integer> entry =
                    topKillers.get(i);

            if (entry.getValue() <= 0) {
                continue;
            }

            String name =
                    Bukkit.getOfflinePlayer(entry.getKey())
                            .getName();

            if (name == null) {
                name = "Unknown";
            }

            broadcastMessage(
                    "§e"
                            + rank
                            + ". §f"
                            + name
                            + " §7- §6"
                            + entry.getValue()
                            + " kills"
            );

            rank++;
        }

        if (rank == 1) {
            broadcastMessage("§7No kills were recorded.");
        }

        broadcastMessage("§8§m---------------------------------");

        if (this.party != null) {
            this.party.setState(PartyState.LOBBY);
        }

        super.end(winnerTeam);
    }
}