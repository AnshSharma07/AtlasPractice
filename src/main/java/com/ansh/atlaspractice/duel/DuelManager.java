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

package com.ansh.atlaspractice.duel;
import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.match.DuelMatch;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.team.TeamColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public final class DuelManager {

    private final AtlasPracticePlugin plugin;
    private final Map<UUID, List<DuelChallenge>> incomingChallenges = new ConcurrentHashMap<>();

    public DuelManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerChallenge(
            Player challenger,
            Player target,
            Kit kit,
            int rounds
    ) {
        registerChallenge(
                challenger,
                target,
                kit,
                rounds,
                null
        );
    }

    public void registerChallenge(
            Player challenger,
            Player target,
            Kit kit,
            int rounds,
            String selectedArenaId
    ) {
        DuelChallenge challenge =
                new DuelChallenge(
                        challenger.getUniqueId(),
                        target.getUniqueId(),
                        kit,
                        rounds,
                        selectedArenaId
                );

        this.incomingChallenges
                .computeIfAbsent(
                        target.getUniqueId(),
                        k -> new ArrayList<>()
                )
                .add(challenge);

        String roundFormat =
                rounds == 1
                        ? "Best of 1"
                        : "Best of " + rounds;

        challenger.sendMessage(
                "§3Duel §8» §fSent a §b" +
                        kit.getDisplayName() +
                        " §7(" + roundFormat + ") §fduel request to §b" +
                        target.getName() + "§f."
        );

        target.sendMessage(
                "§7§m─────────────────────────────────"
        );
        target.sendMessage("§b§lDuel Challenge Received");
        target.sendMessage(
                "§b" + challenger.getName() +
                        " §fhas challenged you to a §b" +
                        kit.getDisplayName() +
                        " §fduel!"
        );
        target.sendMessage(
                "§7Format: §b" + roundFormat
        );

        if (selectedArenaId != null) {
            plugin.getArenaManager()
                    .getArena(selectedArenaId)
                    .ifPresent(arena ->
                            target.sendMessage(
                                    "§7Map: §b" +
                                            arena.getDisplayName()
                            )
                    );
        }

        TextComponent accept =
                new TextComponent("§a§l[ACCEPT DUEL]");

        accept.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/accept " + challenger.getName()
                )
        );

        TextComponent deny =
                new TextComponent(" §c§l[DENY]");

        deny.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/deny " + challenger.getName()
                )
        );

        target.spigot().sendMessage(
                accept,
                new TextComponent(" "),
                deny
        );

        target.sendMessage(
                "§7§m─────────────────────────────────"
        );

        target.playSound(
                target.getLocation(),
                Sound.CLICK,
                1.0F,
                1.2F
        );
    }

    public void acceptChallenge(Player receiver, Player challenger) {
        List<DuelChallenge> challenges =
                this.incomingChallenges.get(
                        receiver.getUniqueId()
                );

        if (challenges == null ||
                challenges.isEmpty()) {

            receiver.sendMessage(
                    "§cYou do not have any pending duel challenges."
            );
            return;
        }

        challenges.removeIf(
                DuelChallenge::isExpired
        );

        Optional<DuelChallenge> matchChallenge =
                challenges.stream()
                        .filter(c ->
                                c.getChallengerUuid()
                                        .equals(
                                                challenger.getUniqueId()
                                        )
                        )
                        .findFirst();

        if (matchChallenge.isEmpty()) {

            receiver.sendMessage(
                    "§cThat specific duel challenge has expired or does not exist."
            );
            return;
        }

        DuelChallenge activeChallenge =
                matchChallenge.get();

        challenges.remove(activeChallenge);

        Kit kit =
                activeChallenge.getKit();

        String selectedArenaId =
                activeChallenge.getSelectedArenaId();

        if (selectedArenaId != null) {

            Arena selectedArena =
                    plugin.getArenaManager()
                            .getArena(selectedArenaId)
                            .orElse(null);

            if (selectedArena == null ||
                    !plugin.getSharedArenaService()
                            .isAssigned(
                                    kit,
                                    selectedArenaId
                            )) {

                receiver.sendMessage(
                        "§cThe selected map is no longer assigned to this kit."
                );

                challenger.sendMessage(
                        "§cThe selected map is no longer assigned to this kit."
                );
                return;
            }

            if (selectedArena.getSpawnRed() == null ||
                    selectedArena.getSpawnBlue() == null ||
                    selectedArena.getTemplateWorld() == null ||
                    selectedArena.getTemplateWorld().isBlank() ||
                    selectedArena.isDisabled()) {

                receiver.sendMessage(
                        "§cThe selected map is currently unavailable."
                );

                challenger.sendMessage(
                        "§cThe selected map is currently unavailable."
                );
                return;
            }

            if (selectedArena.isAvailable()) {

                startDuel(
                        challenger,
                        receiver,
                        activeChallenge,
                        selectedArena
                );

                return;
            }

            if (plugin.getConfig().getBoolean(
                    "runtime-overflow.enabled",
                    false
            )) {

                createRuntimeDuel(
                        challenger,
                        receiver,
                        activeChallenge,
                        selectedArena
                );

                return;
            }

            receiver.sendMessage(
                    "§cThe selected map is currently occupied."
            );

            challenger.sendMessage(
                    "§cThe selected map is currently occupied."
            );

            return;
        }

        Arena arena =
                plugin.getSharedArenaService()
                        .getAvailableArenaPool(kit)
                        .stream()
                        .filter(a ->
                                a != null &&
                                        a.isAvailable() &&
                                        a.getSpawnRed() != null &&
                                        a.getSpawnBlue() != null
                        )
                        .findAny()
                        .orElse(null);

        if (arena == null) {

            receiver.sendMessage(
                    "§cThe assigned arena could not be found."
            );

            challenger.sendMessage(
                    "§cThe assigned arena could not be found."
            );

            return;
        }

        startDuel(
                challenger,
                receiver,
                activeChallenge,
                arena
        );
    }
    private static final java.util.concurrent.atomic.AtomicLong
            RUNTIME_COUNTER =
            new java.util.concurrent.atomic.AtomicLong();

    private void startDuel(
            Player challenger,
            Player receiver,
            DuelChallenge challenge,
            Arena arena
    ) {
        if (!arena.isAvailable()) {
            challenger.sendMessage(
                    "§cThe selected arena is no longer available."
            );
            receiver.sendMessage(
                    "§cThe selected arena is no longer available."
            );
            return;
        }

        arena.setState(
                com.ansh.atlaspractice.arena.ArenaState.ALLOCATED
        );

        MatchTeam team1 =
                new MatchTeam(
                        Collections.singletonList(
                                new MatchTeam.MatchPlayer(
                                        challenger.getUniqueId(),
                                        challenger.getName()
                                )
                        ),
                        TeamColor.RED
                );

        MatchTeam team2 =
                new MatchTeam(
                        Collections.singletonList(
                                new MatchTeam.MatchPlayer(
                                        receiver.getUniqueId(),
                                        receiver.getName()
                                )
                        ),
                        TeamColor.BLUE
                );

        DuelMatch match =
                new DuelMatch(
                        challenge.getKit(),
                        arena,
                        Arrays.asList(team1, team2),
                        challenge.getTotalRounds()
                );

        try {
            plugin.getMatchManager()
                    .hostMatch(match);

        } catch (Exception exception) {

            if (arena instanceof com.ansh.atlaspractice.arena.RuntimeArena) {
                arena.cleanAndResetWorld();
            } else {
                arena.setState(
                        com.ansh.atlaspractice.arena.ArenaState.FREE
                );
            }

            throw exception;
        }
    }

    private void createRuntimeDuel(
            Player challenger,
            Player receiver,
            DuelChallenge challenge,
            Arena sourceArena
    ) {
        long runtimeNumber =
                RUNTIME_COUNTER.incrementAndGet();

        String runtimeWorldName =
                sourceArena.getWorldName() +
                        "_temp_duel_" +
                        runtimeNumber;

        String runtimeId =
                sourceArena.getId() +
                        "#duel-runtime#" +
                        runtimeNumber;

        com.ansh.atlaspractice.arena.RuntimeArena runtimeArena =
                new com.ansh.atlaspractice.arena.RuntimeArena(
                        runtimeId,
                        sourceArena,
                        runtimeWorldName
                );

        plugin.getArenaManager()
                .registerArena(runtimeArena);

        plugin.getLogger().info(
                "[AtlasPractice] Creating duel overflow runtime for " +
                        sourceArena.getId()
        );

        plugin.getWorldService()
                .loadArenaWorld(runtimeArena)
                .whenComplete((world, throwable) -> {

                    if (throwable != null) {

                        plugin.getArenaManager()
                                .unregisterArena(runtimeArena);

                        plugin.getLogger().warning(
                                "[AtlasPractice] Failed to create duel runtime " +
                                        runtimeWorldName +
                                        ": " +
                                        throwable.getMessage()
                        );

                        Bukkit.getScheduler()
                                .runTask(
                                        plugin,
                                        () -> {
                                            challenger.sendMessage(
                                                    "§cNo arena is available right now. Please try again."
                                            );

                                            receiver.sendMessage(
                                                    "§cNo arena is available right now. Please try again."
                                            );
                                        }
                                );

                        return;
                    }

                    runtimeArena.bindToWorld(world);

                    plugin.getLogger().info(
                            "[AtlasPractice] Duel runtime loaded: " +
                                    runtimeWorldName
                    );

                    Bukkit.getScheduler()
                            .runTask(
                                    plugin,
                                    () -> startDuel(
                                            challenger,
                                            receiver,
                                            challenge,
                                            runtimeArena
                                    )
                            );
                });
    }
    public void denyChallenge(Player receiver, Player challenger) {
        List<DuelChallenge> challenges = incomingChallenges.get(receiver.getUniqueId());

        if (challenges == null) {
            return;
        }

        challenges.removeIf(c ->
                c.getChallengerUuid().equals(challenger.getUniqueId()));

        receiver.sendMessage("§cDuel request denied.");
        challenger.sendMessage("§c" + receiver.getName() + " denied your duel request.");
    }
}
