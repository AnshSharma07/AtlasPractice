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

package com.ansh.atlaspractice.party;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class Party {

    public static final int MAX_PARTY_SIZE = 20;

    private final UUID id;
    private UUID leaderUuid;

    private final Set<UUID> members;
    private PartyState state;

    public Party(Player leader) {
        this.id = UUID.randomUUID();
        this.leaderUuid = leader.getUniqueId();
        this.members = new LinkedHashSet<>();
        this.members.add(leader.getUniqueId());
        this.state = PartyState.LOBBY;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLeaderUuid() {
        return leaderUuid;
    }

    public void setLeaderUuid(UUID leaderUuid) {
        this.leaderUuid = leaderUuid;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public PartyState getState() {
        return state;
    }

    public void setState(PartyState state) {
        this.state = state;
    }

    public boolean isLeader(UUID uuid) {
        return leaderUuid.equals(uuid);
    }

    public boolean contains(UUID uuid) {
        return members.contains(uuid);
    }

    public int size() {
        return members.size();
    }

    public void addMember(UUID uuid) {
        if (members.size() < MAX_PARTY_SIZE) {
            members.add(uuid);
        }
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public Player getLeader() {
        return Bukkit.getPlayer(leaderUuid);
    }

    public String getLeaderName() {
        Player leader = Bukkit.getPlayer(leaderUuid);

        if (leader != null) {
            return leader.getName();
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(leaderUuid);
        return offline.getName() != null ? offline.getName() : "Unknown";
    }

    public ArrayList<Player> getOnlineMembers() {
        ArrayList<Player> online = new ArrayList<>();

        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    public void broadcastMessage(String message) {
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    public void broadcastSound(Sound sound, float volume, float pitch) {
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }
    public boolean transferLeadership() {
        if (members.isEmpty()) {
            return false;
        }
        UUID newLeader = members.iterator().next();
        this.leaderUuid = newLeader;
        return true;
    }
}
