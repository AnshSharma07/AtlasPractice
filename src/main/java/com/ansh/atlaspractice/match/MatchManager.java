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
import com.ansh.atlaspractice.party.*;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchManager implements Listener {

    private final AtlasPracticePlugin plugin;
    private final Map<UUID, Match> liveMatchesMap = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToMatchIdMap = new ConcurrentHashMap<>();

    // Centralized cache to track the last combat engagement per player
    private final Map<UUID, UUID> lastAttackers = new ConcurrentHashMap<>();

    public MatchManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void setLastAttacker(UUID victim, UUID attacker) {
        this.lastAttackers.put(victim, attacker);
    }
    public Match getMatch(Player player) {
        return getMatchByPlayer(player.getUniqueId()).orElse(null);
    }
    public UUID getLastAttacker(UUID victim) {
        return this.lastAttackers.get(victim);
    }

    /**
     * Purges a player completely from the tracking map to prevent memory leaks.
     */
    public void clearCombatTracking(UUID uuid) {
        this.lastAttackers.remove(uuid);
        this.lastAttackers.values().removeIf(attackerId -> attackerId.equals(uuid));
    }

    private void teleportToLobby(Player player) {

        player.getActivePotionEffects().forEach(effect ->
                player.removePotionEffect(effect.getType()));

        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        player.setNoDamageTicks(0);

        if (plugin.getConfig().contains("lobby-spawn.world")) {
            World world = Bukkit.getWorld(plugin.getConfig().getString("lobby-spawn.world"));
            if (world != null) {
                player.teleport(new Location(
                        world,
                        plugin.getConfig().getDouble("lobby-spawn.x"),
                        plugin.getConfig().getDouble("lobby-spawn.y"),
                        plugin.getConfig().getDouble("lobby-spawn.z"),
                        (float) plugin.getConfig().getDouble("lobby-spawn.yaw"),
                        (float) plugin.getConfig().getDouble("lobby-spawn.pitch")
                ));
                return;
            }
        }

        player.teleport(player.getWorld().getSpawnLocation());
    }

    public void leaveSpectator(Player player) {
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return;

        UUID matchId = profile.getActiveMatchId();
        Match match = null;
        if (matchId != null) {
            match = liveMatchesMap.get(matchId);
        }
        if (match != null) {
            match.getSpectators().remove(player.getUniqueId());
            match.getTeams().forEach(team -> team.getPlayers().forEach(mp -> {
                Player fighter = Bukkit.getPlayer(mp.getUuid());
                if (fighter != null) fighter.showPlayer(player);
            }));
        }

        profile.clearTemporaryMatchState();
        player.getInventory().clear();
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        for (Player online : Bukkit.getOnlinePlayers()) {
            player.showPlayer(online);
            online.showPlayer(player);
        }
        if (profile.getPartyId() != null) {
            profile.setState(ProfileState.PARTY);
            plugin.getInventoryUtil().applyPartyHotbarItems(player);
        } else {
            profile.setState(ProfileState.LOBBY);
            plugin.getInventoryUtil().applyLobbyHotbarItems(player);
        }

        teleportToLobby(player);

        this.plugin.getPostMatchManager().executePostMatchTasks(player);

        if (profile != null && profile.getPartyId() != null)
            this.plugin.getInventoryUtil().applyPartyHotbarItems(player);
        else
            this.plugin.getInventoryUtil().applyLobbyHotbarItems(player);
    }

    @EventHandler
    public void onSpecQuit(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) {
            return;
        }

        if (profile.getState() != ProfileState.SPECTATE
                && profile.getState() != ProfileState.MATCH) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BED) return;

        event.setCancelled(true);
        leaveSpectator(player);
    }


    public void registerExternalParticipant(UUID participantUuid, UUID matchId) {
        if (participantUuid != null && matchId != null) {
            this.playerToMatchIdMap.put(participantUuid, matchId);
        }
    }

    public void unregisterExternalParticipant(UUID participantUuid) {
        if (participantUuid != null) {
            this.playerToMatchIdMap.remove(participantUuid);
            this.clearCombatTracking(participantUuid);
        }
    }

    public void hostMatch(Match match) {
        this.liveMatchesMap.put(match.getId(), match);
        match.getTeams().forEach(team -> team.getPlayers().forEach(mp -> {
            this.playerToMatchIdMap.put(mp.getUuid(), match.getId());
            Profile profile = this.plugin.getProfileManager().getProfile(mp.getUuid());
            if (profile != null) {
                profile.setState(ProfileState.MATCH);
                profile.setActiveMatchId(match.getId());
            }
        }));
        match.start();
        this.plugin.getReplayManager().startRecording(match);

        for (Party party : getMatchParties(match)) {
            if (party != null) party.setState(PartyState.FIGHTING);
        }
    }

    public void makeInMatchSpectator(Player player, Match match) {
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(2.0F);
        player.setExhaustion(0.0F);
        player.setFireTicks(0);
        player.setFallDistance(0.0F);
        player.setNoDamageTicks(0);

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

        player.setAllowFlight(true);
        player.setFlying(true);

        for (MatchTeam team : match.getTeams()) {
            for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                Player other = Bukkit.getPlayer(mp.getUuid());
                if (other == null || !other.isOnline()) continue;

                if (match.getSpectators().contains(mp.getUuid())) {
                    player.showPlayer(other);
                    other.showPlayer(player);
                } else {
                    other.hidePlayer(player);
                    player.hidePlayer(other);
                }
            }
        }

        player.sendMessage("§eYou are now spectating the match.");
        ItemStack leaveItem = new ItemStack(Material.BED);
        ItemMeta meta = leaveItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cLeave Spectator");
            leaveItem.setItemMeta(meta);
        }
        player.getInventory().setItem(8, leaveItem);
        player.updateInventory();
    }

    public void handleQuit(Player quitter) {
        UUID matchId = this.playerToMatchIdMap.get(quitter.getUniqueId());
        if (matchId == null) return;

        Match match = this.liveMatchesMap.get(matchId);
        if (match == null || match.getState() == Match.MatchState.ENDING) return;

        // Clean up tracking mappings for disconnecting users immediately
        this.clearCombatTracking(quitter.getUniqueId());

        match.getTeams().forEach(team -> team.getPlayers().forEach(mp -> {
            Player p = Bukkit.getPlayer(mp.getUuid());
            if (p != null) p.sendMessage("§c" + quitter.getName() + " disconnected from the match.");
        }));

        if (match instanceof PartyPvPMatch) {
            ((PartyPvPMatch) match).handleDeath(quitter);
            return;
        } else if (match instanceof PartyMatch) {
            ((PartyMatch) match).handlePlayerQuit(quitter);
            return;
        } else if (match instanceof PartyFFAMatch) {
            ((PartyFFAMatch) match).handleDeath(quitter, null);
            return;
        }

        MatchTeam winningTeam = match.getTeams().stream()
                .filter(team -> team.getPlayers().stream().noneMatch(mp -> mp.getUuid().equals(quitter.getUniqueId())))
                .findFirst().orElse(match.getTeams().get(0));
        triggerMatchEndSequence(match, winningTeam);
    }

    public void triggerMatchEndSequence(Match match, MatchTeam winningTeam) {
        if (match == null || match.getState() == Match.MatchState.ENDING) return;

        match.setState(Match.MatchState.ENDING);
        this.plugin.getReplayManager().stopRecording(match, winningTeam);
        match.end(winningTeam);

        for (Party party : getMatchParties(match)) {
            if (party != null) party.setState(PartyState.LOBBY);
        }

        String winnerName = (winningTeam != null) ? winningTeam.getLeaderName() : "No one";
        match.getTeams().forEach(team -> team.getPlayers().forEach(mp -> {
            Player player = Bukkit.getPlayer(mp.getUuid());
            if (player == null) return;

            player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 1.0F, 1.0F);

            player.sendMessage("§7§m─────────────────────────────────");
            player.sendMessage(" §b§lPost-Match Summary");
            player.sendMessage(" ");
            player.sendMessage(" §7◆ §fWinner: §b" + winnerName);
            player.sendMessage("§7§m─────────────────────────────────");
            boolean isWinner = winningTeam != null && winningTeam.getPlayers().stream().anyMatch(w -> w.getUuid().equals(player.getUniqueId()));
            if (isWinner) player.sendTitle("§b§lVICTORY!", "§7You won the match.");
            else player.sendTitle("§c§lDEFEAT!", "§7Winner: §b" + winnerName);
        }));

        new BukkitRunnable() {
            @Override
            public void run() { removeMatchTrack(match.getId()); }
        }.runTaskLater(this.plugin, 60L);
    }

    public void removeMatchTrack(UUID matchId) {
        Match match = this.liveMatchesMap.remove(matchId);
        if (match == null) return;

        for (Party party : getMatchParties(match)) {
            if (party != null) party.setState(PartyState.LOBBY);
        }

        match.getPlacedBlocks().clear();
        match.getTeams().forEach(team -> team.getPlayers().forEach(mp -> {
            this.playerToMatchIdMap.remove(mp.getUuid());

            // Clean up mappings when the match completely tears down
            this.clearCombatTracking(mp.getUuid());

            Profile profile = this.plugin.getProfileManager().getProfile(mp.getUuid());
            Player player = Bukkit.getPlayer(mp.getUuid());

            if (profile != null) {
                profile.clearTemporaryMatchState();
                if (profile.getPartyId() != null) profile.setState(ProfileState.PARTY);
                else profile.setState(ProfileState.LOBBY);
            }

            if (player != null) {
                player.getInventory().clear();
                player.getInventory().setArmorContents(new ItemStack[4]);
                player.setHealth(20.0D);
                player.setFoodLevel(20);
                player.setSaturation(2.0F);
                player.setExhaustion(0.0F);
                player.setFireTicks(0);
                player.setFallDistance(0.0F);
                player.setNoDamageTicks(0);
                player.setAllowFlight(false);
                player.setMaximumNoDamageTicks(20);
                player.setNoDamageTicks(0);
                player.setFlying(false);
                player.setWalkSpeed(0.2F);
                Scoreboard board =
                        Bukkit.getScoreboardManager()
                                .getMainScoreboard();
                Team red = board.getTeam("ap_red");
                Team blue = board.getTeam("ap_blue");

                if (red != null) {
                    red.removeEntry(player.getName());
                }

                if (blue != null) {
                    blue.removeEntry(player.getName());
                }
                player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                player.setAllowFlight(false);
                player.setFlying(false);
                for (Player online : Bukkit.getOnlinePlayers()) {
                    player.showPlayer(online);
                    online.showPlayer(player);
                }
                teleportToLobby(player);

                if (profile != null && profile.getPartyId() != null) this.plugin.getInventoryUtil().applyPartyHotbarItems(player);
                else this.plugin.getInventoryUtil().applyLobbyHotbarItems(player);
                this.plugin.getPostMatchManager().executePostMatchTasks(player);
            }
        }));

        for (UUID uuid : match.getSpectators()) {
            Player spectator = Bukkit.getPlayer(uuid);
            if (spectator != null) leaveSpectator(spectator);
        }

        match.getSpectators().clear();
        if (match.getArena() != null) match.getArena().cleanAndResetWorld();
    }

    private List<Party> getMatchParties(Match match) {
        List<Party> parties = new ArrayList<>();
        if (match instanceof PartyMatch) parties.add(((PartyMatch) match).getParty());
        else if (match instanceof PartyFFAMatch) parties.add(((PartyFFAMatch) match).getParty());
        else if (match instanceof PartyManagedMatch) parties.addAll(((PartyManagedMatch) match).getParties());
        return parties;
    }

    public Collection<Match> getLiveMatches() { return this.liveMatchesMap.values(); }
    public UUID getPlayerMatchId(UUID uuid) { return this.playerToMatchIdMap.get(uuid); }
    public Match getLiveMatch(UUID id) { return id == null ? null : this.liveMatchesMap.get(id); }
    public Optional<Match> getMatchByPlayer(UUID uuid) {
        UUID matchId = this.playerToMatchIdMap.get(uuid);
        return matchId == null ? Optional.empty() : Optional.ofNullable(this.liveMatchesMap.get(matchId));
    }
}