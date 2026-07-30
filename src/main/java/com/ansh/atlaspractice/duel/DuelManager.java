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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DuelManager {

    private final AtlasPracticePlugin plugin;
    private final Map<UUID, List<DuelChallenge>> incomingChallenges = new ConcurrentHashMap<>();

    public DuelManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void registerChallenge(Player challenger, Player target, Kit kit, int rounds) {
        DuelChallenge challenge = new DuelChallenge(challenger.getUniqueId(), target.getUniqueId(), kit, rounds);
        this.incomingChallenges.computeIfAbsent(target.getUniqueId(), k -> new ArrayList<>()).add(challenge);

        String roundFormat = (rounds == 1) ? "Best of 1" : "Best of " + rounds;

        challenger.sendMessage("§3Duel §8Â» §fSent a §b" + kit.getDisplayName() + " §7(" + roundFormat + ") §fduel request to §b" + target.getName() + "§f.");

        target.sendMessage("§7§mâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€");
        target.sendMessage("§b§lDuel Challenge Received");
        target.sendMessage("§b" + challenger.getName() + " §fhas challenged you to a §b" + kit.getDisplayName() + " §fduel!");
        target.sendMessage("§7Format: §b" + roundFormat);
        TextComponent accept = new TextComponent("§a§l[ACCEPT DUEL]");

        accept.setClickEvent(
                new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/accept " + challenger.getName()
                )
        );
        TextComponent deny = new TextComponent(" §c§l[DENY]");
        deny.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/deny " + challenger.getName()
        ));

        target.spigot().sendMessage(accept, new TextComponent(" "), deny);
        target.sendMessage("§7§mâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€");
        target.playSound(target.getLocation(), Sound.CLICK, 1.0F, 1.2F);
    }

    public void acceptChallenge(Player receiver, Player challenger) {
        List<DuelChallenge> challenges = this.incomingChallenges.get(receiver.getUniqueId());
        if (challenges == null || challenges.isEmpty()) {
            receiver.sendMessage("§cYou do not have any pending duel challenges.");
            return;
        }

        challenges.removeIf(DuelChallenge::isExpired);
        Optional<DuelChallenge> matchChallenge = challenges.stream()
                .filter(c -> c.getChallengerUuid().equals(challenger.getUniqueId()))
                .findFirst();

        if (matchChallenge.isEmpty()) {
            receiver.sendMessage("§cThat specific duel challenge has expired or does not exist.");
            return;
        }

        DuelChallenge activeChallenge = matchChallenge.get();
        challenges.remove(activeChallenge);

        Kit kit = activeChallenge.getKit();

        boolean hasArena = plugin.getSharedArenaService().getAvailableArenaPool(kit).stream()
                .anyMatch(arena -> arena != null && arena.isAvailable());

        if (!hasArena) {
            receiver.sendMessage("§cArena not set for §e" + kit.getDisplayName() + "§c.");
            challenger.sendMessage("§cArena not set for §e" + kit.getDisplayName() + "§c.");
            return;
        }

        Arena arena = plugin.getSharedArenaService().getAvailableArenaPool(kit).stream()
                .filter(a -> a != null && a.isAvailable())
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

        if (!arena.isAvailable()
                || arena.getSpawnRed() == null
                || arena.getSpawnBlue() == null) {

            receiver.sendMessage(
                    "§cThe assigned arena is currently unavailable."
            );

            challenger.sendMessage(
                    "§cThe assigned arena is currently unavailable."
            );

            return;
        }

        List<MatchTeam.MatchPlayer> p1List = new ArrayList<>();
        p1List.add(new MatchTeam.MatchPlayer(challenger.getUniqueId(), challenger.getName()));
        MatchTeam team1 = new MatchTeam(
                p1List,
                TeamColor.RED
        );

        List<MatchTeam.MatchPlayer> p2List = new ArrayList<>();
        p2List.add(new MatchTeam.MatchPlayer(receiver.getUniqueId(), receiver.getName()));
        MatchTeam team2 = new MatchTeam(
                p2List,
                TeamColor.BLUE
        );

        DuelMatch premiumMatch = new DuelMatch(
                activeChallenge.getKit(),
                arena,
                Arrays.asList(team1, team2),
                activeChallenge.getTotalRounds()
        );

        plugin.getMatchManager().hostMatch(premiumMatch);
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
