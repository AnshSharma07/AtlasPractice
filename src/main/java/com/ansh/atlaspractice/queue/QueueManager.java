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

package com.ansh.atlaspractice.queue;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.config.RankedConfig;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;


@Getter
public final class QueueManager {

    private final AtlasPracticePlugin plugin;
    private final RankedQueue rankedQueue;
    private final UnrankedQueue unrankedQueue;
    public QueueManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        this.rankedQueue = new RankedQueue();
        this.unrankedQueue = new UnrankedQueue();
    }

    public void joinUnrankedQueue(Player player, Kit kit) {
        joinUnrankedQueue(player, kit, null);
    }

    public void joinUnrankedQueue(Player player, Kit kit, String selectedArenaId
    ) {
        Profile profile =
                this.plugin.getProfileManager()
                        .getProfile(player.getUniqueId());

        if (profile == null ||
                profile.getState() != ProfileState.LOBBY) {

            player.sendMessage("§cInteraction Failed.");
            return;
        }

        boolean hasArena =
                !plugin.getSharedArenaService().getAvailableArenaPool(kit).isEmpty();

        if (!hasArena) {
            player.sendMessage(
                    "§cArena not set for §e" + kit.getDisplayName() + "§c."
            );
            return;
        }

        if (selectedArenaId != null &&
                !plugin.getSharedArenaService()
                        .isAssigned(kit, selectedArenaId)) {

            player.sendMessage("§cThat map is no longer assigned to this kit.");
            return;
        }

        int layout =
                profile.getSelectedLayout(
                        kit.getId()
                );

        QueueEntry entry =
                new QueueEntry(player.getUniqueId(), kit, false, false, 0, layout, selectedArenaId);

        this.unrankedQueue.addEntry(entry);

        profile.setLastQueuedKitId(
                kit.getId()
        );

        profile.setState(
                ProfileState.QUEUING
        );

        if (selectedArenaId == null) {
            player.sendMessage(
                    "§eEntered unranked queue for §6" + kit.getDisplayName() + "§e."
            );
        } else {
            plugin.getArenaManager()
                    .getArena(selectedArenaId)
                    .ifPresent(arena ->
                            player.sendMessage("§eQueued for §6" + kit.getDisplayName() + " §7on §b" + arena.getDisplayName() + "§e.")
                    );
        }

        this.plugin.getInventoryUtil()
                .applyQueueHotbarItems(player);
    }

    public void joinRankedQueue(Player player, Kit kit) {
        joinRankedQueue(player, kit, null);
    }

    public void joinRankedQueue(Player player, Kit kit, String selectedArenaId) {
        Profile profile =
                this.plugin.getProfileManager()
                        .getProfile(player.getUniqueId());

        if (profile == null ||
                profile.getState() != ProfileState.LOBBY) {

            player.sendMessage(
                    "§cYou cannot enter a competitive queue right now."
            );
            return;
        }

        com.ansh.atlaspractice.config.RankedConfig rankedConfig =
                this.plugin.getRankedConfig();

        if (!rankedConfig.isEnabled()) {
            player.sendMessage(
                    "§cRanked queue is currently disabled."
            );
            return;
        }

        if (!rankedConfig.isKitEnabled(kit.getId())) {
            player.sendMessage(
                    "§cRanked is not enabled for §e" +
                            kit.getDisplayName() + "§c."
            );
            return;
        }

        boolean hasArena =
                !plugin.getSharedArenaService()
                        .getAvailableArenaPool(kit)
                        .isEmpty();

        if (!hasArena) {
            player.sendMessage(
                    "§cArena not set for §e" +
                            kit.getDisplayName() + "§c."
            );
            return;
        }

        if (selectedArenaId != null &&
                !plugin.getSharedArenaService()
                        .isAssigned(kit, selectedArenaId)) {

            player.sendMessage(
                    "§cThat map is no longer assigned to this kit."
            );
            return;
        }

        RankedConfig.WinsMode mode =
                rankedConfig.getWinsMode();

        if (mode == RankedConfig.WinsMode.GLOBAL) {

            int required = rankedConfig.getGlobalWins();

            if (required > 0 &&
                    profile.getWins() < required) {

                player.sendMessage("§cYou need §e" + required + " §cwins to join Ranked. You have §e" + profile.getWins() + "§c."
                );
                return;
            }

        } else {

            int required =
                    rankedConfig.getKitWins(
                            kit.getId()
                    );

            if (required > 0) {

                int kitWins =
                        profile.getKitStats(
                                kit.getId()
                        ).getWins();

                if (kitWins < required) {

                    player.sendMessage("§cYou need §e" + required + " §c" + kit.getDisplayName() + " wins to join Ranked. You have §e" + kitWins + "§c.");
                    return;
                }
            }
        }

        int eloValue =
                profile.getEloForKit(kit.getId());

        int layout =
                profile.getSelectedLayout(
                        kit.getId()
                );

        QueueEntry entry =
                new QueueEntry(player.getUniqueId(), kit, false, true, eloValue, layout, selectedArenaId);

        this.rankedQueue.addEntry(entry);

        profile.setLastQueuedKitId(
                kit.getId()
        );

        profile.setState(
                ProfileState.QUEUING
        );

        if (selectedArenaId == null) {

            player.sendMessage("§eEntered ranked queue for §a" + kit.getDisplayName() + " §7(ELO: " + eloValue + ")§e."
            );

        } else {

            plugin.getArenaManager()
                    .getArena(selectedArenaId)
                    .ifPresent(arena ->
                            player.sendMessage("§eQueued ranked for §a" + kit.getDisplayName() + " §7(ELO: " + eloValue + ", Map: " + arena.getDisplayName() + ")§e."
                            )
                    );
        }
        this.plugin.getInventoryUtil()
                .applyQueueHotbarItems(player);
    }
    public void leaveQueue(Player player) {
        UUID uuid = player.getUniqueId();

        boolean removed =
                this.unrankedQueue.removeEntry(uuid)
                        || this.rankedQueue.removeEntry(uuid);

        if (removed) {
            Profile profile =
                    this.plugin.getProfileManager()
                            .getProfile(uuid);

            if (profile != null) {
                profile.setState(ProfileState.LOBBY);
            }

            player.getInventory().clear();

            this.plugin.getInventoryUtil()
                    .applyLobbyHotbarItems(player);

            player.sendMessage("§cYou left the queue.");
        }
    }
}
