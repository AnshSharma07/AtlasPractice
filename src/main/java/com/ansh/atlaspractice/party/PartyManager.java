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
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.party.PartyPvPMatch;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import com.ansh.atlaspractice.team.TeamColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PartyManager {

    private final AtlasPracticePlugin plugin;
    private final Map<UUID, Party> activePartiesMap = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, PartyInvite>> pendingInvitesMap = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, PartyPvPChallenge>> pendingPartyDuels = new ConcurrentHashMap<>();

    public PartyManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        startInviteCleanupTask();
    }

    public AtlasPracticePlugin getPlugin() {
        return plugin;
    }

    private void startInviteCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredInvites();
            }
        }.runTaskTimerAsynchronously(plugin, 1200L, 1200L);
    }

    public void cleanupExpiredInvites() {
        for (Map<UUID, PartyInvite> map : pendingInvitesMap.values()) {
            map.values().removeIf(PartyInvite::isExpired);
        }
        pendingInvitesMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        for (Map<UUID, PartyPvPChallenge> map : pendingPartyDuels.values()) {
            map.values().removeIf(PartyPvPChallenge::isExpired);
        }
        pendingPartyDuels.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public Optional<Party> getParty(UUID partyId) {
        if (partyId == null) return Optional.empty();
        return Optional.ofNullable(activePartiesMap.get(partyId));
    }

    /**
     * Returns a read-only view of every currently active party.
     * Used by {@link PartyListMenu} to populate the challenge GUI.
     */
    public Collection<Party> getActiveParties() {
        return Collections.unmodifiableCollection(activePartiesMap.values());
    }

    public Party createParty(Player leader) {
        Profile profile = plugin.getProfileManager().getProfile(leader.getUniqueId());
        if (profile == null) return null;
        if (profile.getPartyId() != null) {
            leader.sendMessage("§cYou are already in a party.");
            return null;
        }
        if (profile.getState() != ProfileState.LOBBY) {
            leader.sendMessage("§cYou must be in the lobby.");
            return null;
        }

        Party party = new Party(leader);
        activePartiesMap.put(party.getId(), party);
        profile.setPartyId(party.getId());
        profile.setState(ProfileState.PARTY);
        plugin.getInventoryUtil().applyPartyHotbarItems(leader);
        leader.sendMessage("§aParty created.");
        return party;
    }

    public void sendInvite(Player sender, Player target) {
        if (sender.equals(target)) {
            sender.sendMessage("§cYou cannot invite yourself.");
            return;
        }
        Profile senderProfile = plugin.getProfileManager().getProfile(sender.getUniqueId());
        Profile targetProfile = plugin.getProfileManager().getProfile(target.getUniqueId());
        if (!targetProfile.isAllowPartyInvites()) {
            sender.sendMessage("§cThat player has party invites disabled.");
            return;
        }
        if (senderProfile == null || targetProfile == null) return;
        if (targetProfile.getPartyId() != null) {
            sender.sendMessage("§cThat player is already in a party.");
            return;
        }
        if (targetProfile.getState() != ProfileState.LOBBY) {
            sender.sendMessage("§cThat player is busy.");
            return;
        }

        Party party = getParty(senderProfile.getPartyId()).orElse(null);
        if (party == null) {
            sender.sendMessage("§cYou are not in a party.");
            return;
        }
        if (!party.isLeader(sender.getUniqueId())) {
            sender.sendMessage("§cOnly the leader can invite.");
            return;
        }
        if (party.getState() != PartyState.LOBBY) {
            sender.sendMessage("§cParty is currently in a match.");
            return;
        }
        if (party.size() >= Party.MAX_PARTY_SIZE) {
            sender.sendMessage("§cYour party is full (" + Party.MAX_PARTY_SIZE + " max).");
            return;
        }

        PartyInvite invite = new PartyInvite(party.getId(), sender.getUniqueId(), target.getUniqueId());
        pendingInvitesMap.computeIfAbsent(target.getUniqueId(), k -> new ConcurrentHashMap<>()).put(sender.getUniqueId(), invite);

        sender.sendMessage("§aInvited §e" + target.getName());
        TextComponent join = new TextComponent("§a§l[JOIN PARTY]");
        join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + sender.getName()));
        TextComponent deny = new TextComponent(" §c§l[DECLINE]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party deny " + sender.getName()));
        target.sendMessage("§e" + sender.getName() + " invited you to their party.");
        target.spigot().sendMessage(join, deny);
        target.playSound(target.getLocation(), Sound.NOTE_PLING, 1F, 1F);
    }

    public void sendPartyDuel(Player sender, Player target, Kit kit, int requiredWins) {
        Profile senderProfile = plugin.getProfileManager().getProfile(sender.getUniqueId());
        Profile targetProfile = plugin.getProfileManager().getProfile(target.getUniqueId());

        if (senderProfile == null || targetProfile == null) return;

        Party senderParty = getParty(senderProfile.getPartyId()).orElse(null);
        Party targetParty = getParty(targetProfile.getPartyId()).orElse(null);

        if (senderParty == null || targetParty == null) {
            sender.sendMessage("§cBoth players must be in a party to duel.");
            return;
        }
        if (!senderParty.isLeader(sender.getUniqueId())) {
            sender.sendMessage("§cOnly the party leader can challenge other parties.");
            return;
        }
        if (!targetParty.isLeader(target.getUniqueId())) {
            sender.sendMessage("§cYou can only send challenges to another party's leader.");
            return;
        }
        if (senderParty.getId().equals(targetParty.getId())) {
            sender.sendMessage("§cYou cannot duel your own party.");
            return;
        }
        if (senderParty.size() != targetParty.size()) {
            sender.sendMessage("§cParty sizes must be equal to duel. Your party: " + senderParty.size() + " | Their party: " + targetParty.size());
            return;
        }
        if (senderParty.getState() != PartyState.LOBBY || targetParty.getState() != PartyState.LOBBY) {
            sender.sendMessage("§cOne of the parties is currently busy.");
            return;
        }
        Map<UUID, PartyPvPChallenge> existing =
                pendingPartyDuels.get(target.getUniqueId());

        if (existing != null
                && existing.containsKey(sender.getUniqueId())) {

            sender.sendMessage(
                    "§cYou already sent a duel request to this party."
            );

            return;
        }
        PartyPvPChallenge challenge = new PartyPvPChallenge(senderParty.getId(), targetParty.getId(), sender.getUniqueId(), target.getUniqueId(), kit, requiredWins);
        pendingPartyDuels.computeIfAbsent(target.getUniqueId(), k -> new ConcurrentHashMap<>()).put(sender.getUniqueId(), challenge);

        String format = (requiredWins == 1) ? "Best of 1" : "Best of " + ((requiredWins * 2) - 1);

        sender.sendMessage("§aYou have sent a Party Duel request to §e" + target.getName() + " §7(" + format + " " + kit.getDisplayName() + ")");

        target.sendMessage("§e" + sender.getName() + " §aand their party has challenged your party to a duel!");
        target.sendMessage("§7Format: §b" + format + " " + kit.getDisplayName());
        TextComponent join = new TextComponent("§a§l[ACCEPT DUEL]");
        join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + sender.getName()));
        TextComponent deny = new TextComponent(" §c§l[DECLINE]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party deny " + sender.getName()));
        target.spigot().sendMessage(join, deny);
        target.playSound(target.getLocation(), Sound.NOTE_PLING, 1F, 1F);
    }

    public void denyInvite(Player receiver, Player senderPlayer) {
        // Check normal invites
        Map<UUID, PartyInvite> invites = pendingInvitesMap.get(receiver.getUniqueId());
        if (invites != null && invites.remove(senderPlayer.getUniqueId()) != null) {
            receiver.sendMessage("§cParty invite declined.");
            senderPlayer.sendMessage("§c" + receiver.getName() + " declined your party invite.");
            return;
        }

        // Check Party duels
        Map<UUID, PartyPvPChallenge> duels = pendingPartyDuels.get(receiver.getUniqueId());
        if (duels != null && duels.remove(senderPlayer.getUniqueId()) != null) {
            receiver.sendMessage("§cParty duel declined.");
            senderPlayer.sendMessage("§c" + receiver.getName() + " declined your party duel request.");
            return;
        }
    }

    public void acceptInvite(Player receiver, Player senderPlayer) {
        Profile receiverProfile = plugin.getProfileManager().getProfile(receiver.getUniqueId());
        if (receiverProfile == null) return;

        // Check Party Duels First
        Map<UUID, PartyPvPChallenge> duels = pendingPartyDuels.get(receiver.getUniqueId());
        if (duels != null && duels.containsKey(senderPlayer.getUniqueId())) {
            PartyPvPChallenge challenge = duels.remove(senderPlayer.getUniqueId());
            if (challenge.isExpired()) {
                receiver.sendMessage("§cThat challenge has expired.");
                return;
            }
            startPartyDuel(challenge);
            return;
        }

        // Fallback to normal party invite
        if (receiverProfile.getPartyId() != null) {
            receiver.sendMessage("§cYou are already in a party.");
            return;
        }
        if (receiverProfile.getState() != ProfileState.LOBBY) {
            receiver.sendMessage("§cYou must be in the lobby.");
            return;
        }

        Map<UUID, PartyInvite> invites = pendingInvitesMap.get(receiver.getUniqueId());
        if (invites == null || !invites.containsKey(senderPlayer.getUniqueId())) {
            receiver.sendMessage("§cInvite expired or not found.");
            return;
        }

        PartyInvite invite = invites.remove(senderPlayer.getUniqueId());
        Party party = activePartiesMap.get(invite.getSenderPartyId());
        if (party == null || party.size() >= Party.MAX_PARTY_SIZE) {
            receiver.sendMessage("§cParty no longer exists or is full.");
            return;
        }

        party.addMember(receiver.getUniqueId());
        receiverProfile.setPartyId(party.getId());
        receiverProfile.setState(ProfileState.PARTY);
        plugin.getInventoryUtil().applyPartyHotbarItems(receiver);
        party.broadcastMessage("§b" + receiver.getName() + " joined the party.");
        party.broadcastSound(Sound.NOTE_PLING, 1F, 1F);
    }

    private void startPartyDuel(PartyPvPChallenge challenge) {
        Party teamA = activePartiesMap.get(challenge.getChallengerPartyId());
        Party teamB = activePartiesMap.get(challenge.getTargetPartyId());

        if (teamA == null || teamB == null || teamA.getState() != PartyState.LOBBY || teamB.getState() != PartyState.LOBBY) {
            Player p1 = Bukkit.getPlayer(challenge.getChallengerLeader());
            Player p2 = Bukkit.getPlayer(challenge.getTargetLeader());
            if (p1 != null) p1.sendMessage("§cOne of the parties is no longer available.");
            if (p2 != null) p2.sendMessage("§cOne of the parties is no longer available.");
            return;
        }

        Arena arena = plugin.getSharedArenaService().getAvailableArenaPool(challenge.getKit()).stream()
                .filter(a -> a != null && a.isAvailable())
                .findAny().orElse(null);

        if (arena == null) {
            teamA.broadcastMessage("§cNo open arenas found for this kit.");
            teamB.broadcastMessage("§cNo open arenas found for this kit.");
            return;
        }

        List<MatchTeam.MatchPlayer> playersA = new ArrayList<>();
        List<MatchTeam.MatchPlayer> playersB = new ArrayList<>();

        teamA.getOnlineMembers().forEach(p -> playersA.add(new MatchTeam.MatchPlayer(p.getUniqueId(), p.getName())));
        teamB.getOnlineMembers().forEach(p -> playersB.add(new MatchTeam.MatchPlayer(p.getUniqueId(), p.getName())));

        MatchTeam mtA =
                new MatchTeam(
                        playersA,
                        TeamColor.RED
                );

        MatchTeam mtB =
                new MatchTeam(
                        playersB,
                        TeamColor.BLUE
                );

        com.ansh.atlaspractice.match.Match match = createPartyPvPMatch(java.util.Arrays.asList(teamA, teamB), challenge.getKit(), arena, java.util.Arrays.asList(mtA, mtB), challenge.getRequiredWins());
        plugin.getMatchManager().hostMatch(match);
    }

    private com.ansh.atlaspractice.match.Match createPartyPvPMatch(List<Party> parties, Kit kit, Arena arena, List<MatchTeam> teams, int requiredWins) {
        String kitId = kit == null ? "" : kit.getId();
        if (kitId.equalsIgnoreCase("bridge")) {
            return new PartyBridgeMatch(parties, kit, arena, teams, requiredWins);
        }
        if (kitId.equalsIgnoreCase("battlerush")) {
            return new PartyBattleRushMatch(parties, kit, arena, teams, requiredWins);
        }
        return new PartyPvPMatch(parties, kit, arena, teams, requiredWins);
    }

    public void leaveParty(Player player) {
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null || profile.getPartyId() == null) return;

        Party party = activePartiesMap.get(profile.getPartyId());
        if (party == null) {
            profile.setPartyId(null);
            profile.setState(ProfileState.LOBBY);
            plugin.getInventoryUtil().applyLobbyHotbarItems(player);
            return;
        }

        if (party.getState() != PartyState.LOBBY) {
            player.sendMessage("§cParty is currently in a match.");
            return;
        }

        party.removeMember(player.getUniqueId());
        profile.setPartyId(null);
        profile.setState(ProfileState.LOBBY);
        plugin.getInventoryUtil().applyLobbyHotbarItems(player);
        party.broadcastMessage("§c" + player.getName() + " left the party.");

        if (party.isEmpty()) {
            activePartiesMap.remove(party.getId());
        } else if (party.getLeaderUuid().equals(player.getUniqueId())) {
            if (party.transferLeadership()) {
                Player newLeader = Bukkit.getPlayer(party.getLeaderUuid());
                if (newLeader != null) {
                    party.broadcastMessage("§e" + newLeader.getName() + " is now the leader.");
                    // Refresh hotbar so the new leader gets the Disband item.
                    plugin.getInventoryUtil().applyPartyHotbarItems(newLeader);
                }
            }
        }
    }

    public void disbandParty(Player leader) {
        Profile profile = plugin.getProfileManager().getProfile(leader.getUniqueId());
        if (profile == null || profile.getPartyId() == null) return;

        Party party = activePartiesMap.get(profile.getPartyId());
        if (party == null) return;

        if (!party.isLeader(leader.getUniqueId())) {
            leader.sendMessage("§cOnly the leader can disband.");
            return;
        }
        if (party.getState() != PartyState.LOBBY) {
            leader.sendMessage("§cParty is in a match.");
            return;
        }

        for (UUID uuid : party.getMembers()) {
            Profile memberProfile = plugin.getProfileManager().getProfile(uuid);
            Player member = Bukkit.getPlayer(uuid);
            if (memberProfile != null) {
                memberProfile.setPartyId(null);
                memberProfile.setState(ProfileState.LOBBY);
            }
            if (member != null) {
                plugin.getInventoryUtil().applyLobbyHotbarItems(member);
                member.sendMessage("§cParty disbanded.");
            }
        }
        activePartiesMap.remove(party.getId());
    }

    public void promoteLeader(Player leader, Player target) {
        Profile profile = plugin.getProfileManager().getProfile(leader.getUniqueId());
        if (profile == null || profile.getPartyId() == null) return;

        Party party = activePartiesMap.get(profile.getPartyId());
        if (party == null || party.getState() != PartyState.LOBBY) return;
        if (!party.isLeader(leader.getUniqueId())) return;
        if (!party.contains(target.getUniqueId()) || target.equals(leader)) return;

        party.setLeaderUuid(target.getUniqueId());
        party.broadcastMessage("§e" + target.getName() + " is now the party leader.");
        // Refresh every member's hotbar so Disband appears for the new leader only.
        for (Player member : party.getOnlineMembers()) {
            plugin.getInventoryUtil().applyPartyHotbarItems(member);
        }
    }

    public void openSplitMenu(Player player) {
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null || profile.getPartyId() == null) return;

        Party party = getParty(profile.getPartyId()).orElse(null);
        if (party == null || !party.isLeader(player.getUniqueId())) return;

        player.sendMessage("§aSelect a kit to start Party Split.");
        player.setMetadata("party-split", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        plugin.getMenuManager().openUnrankedMenu(player, null);
    }

    public void forceLeaveParty(Player player) {
        leaveParty(player);
    }

    /**
     * Starts a Party FFA match for the given leader's party using the selected kit.
     * Called by {@link com.ansh.atlaspractice.menus.PartyFFAKitMenu} after kit selection.
     */
    public void startPartyFFA(Player leader, Kit kit) {
        Profile profile = plugin.getProfileManager().getProfile(leader.getUniqueId());
        if (profile == null || profile.getPartyId() == null) {
            leader.sendMessage("§cYou are not in a party.");
            return;
        }

        Party party = getParty(profile.getPartyId()).orElse(null);
        if (party == null) {
            leader.sendMessage("§cParty not found.");
            return;
        }
        if (!party.isLeader(leader.getUniqueId())) {
            leader.sendMessage("§cOnly the party leader can start Party FFA.");
            return;
        }
        if (party.getState() != PartyState.LOBBY) {
            leader.sendMessage("§cThe party is already in a match.");
            return;
        }
        if (party.size() < 2) {
            leader.sendMessage("§cYou need at least 2 players to start Party FFA.");
            return;
        }

        Arena arena = plugin.getSharedArenaService().getAvailableArenaPool(kit).stream()
                .filter(a -> a != null && a.isAvailable())
                .findAny().orElse(null);

        if (arena == null) {
            leader.sendMessage("§cNo available arena found for " + kit.getDisplayName() + ".");
            return;
        }

        PartyFFAMatch match = new PartyFFAMatch(party, kit, arena);
        plugin.getMatchManager().hostMatch(match);
    }

    public void executePartySplit(Player leader, Kit kit) {
        Profile profile = plugin.getProfileManager().getProfile(leader.getUniqueId());
        if (profile == null || profile.getPartyId() == null) return;

        Party party = getParty(profile.getPartyId()).orElse(null);
        if (party == null || !party.isLeader(leader.getUniqueId())) return;

        List<Player> onlinePlayers = party.getOnlineMembers();
        if (onlinePlayers.size() < 2) {
            leader.sendMessage("§cNot enough players online to split the party.");
            return;
        }

        Collections.shuffle(onlinePlayers);
        List<MatchTeam.MatchPlayer> team1 = new ArrayList<>();
        List<MatchTeam.MatchPlayer> team2 = new ArrayList<>();

        for (int i = 0; i < onlinePlayers.size(); i++) {
            Player online = onlinePlayers.get(i);
            MatchTeam.MatchPlayer mp = new MatchTeam.MatchPlayer(online.getUniqueId(), online.getName());
            if (i % 2 == 0) team1.add(mp);
            else team2.add(mp);
        }

        MatchTeam t1 =
                new MatchTeam(
                        team1,
                        TeamColor.RED
                );

        MatchTeam t2 =
                new MatchTeam(
                        team2,
                        TeamColor.BLUE
                );

        Arena arena = plugin.getSharedArenaService().getAvailableArenaPool(kit).stream()
                .filter(a -> a != null && a.isAvailable())
                .findAny().orElse(null);

        if (arena == null) {
            leader.sendMessage("§cNo available arena found for this kit.");
            return;
        }
        com.ansh.atlaspractice.match.Match match =
                createPartyPvPMatch(
                        java.util.Arrays.asList(party, party), kit, arena, java.util.Arrays.asList(t1, t2), 1);
        plugin.getMatchManager().hostMatch(match);
    }
}