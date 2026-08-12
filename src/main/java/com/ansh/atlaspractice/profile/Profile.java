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

package com.ansh.atlaspractice.profile;

import com.ansh.atlaspractice.cosmetics.CosmeticPlayerData;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Profile {

    private final UUID uniqueId;
    private final String lastKnownName;
    private boolean allowDuels = true;
    private boolean scoreboardVisible = true;
    private boolean allowPartyInvites = true;
    private boolean scoreboardEnabled = true;
    private com.ansh.atlaspractice.settings.MapSelectionPreference mapSelectionPreference = com.ansh.atlaspractice.settings.MapSelectionPreference.NONE;
    private com.ansh.atlaspractice.settings.TimeMode timeMode = com.ansh.atlaspractice.settings.TimeMode.SERVER;
    private com.ansh.atlaspractice.settings.ChatMode chatMode = com.ansh.atlaspractice.settings.ChatMode.ALL;
    private boolean autoGg = false;
    private boolean autoRequeue = false;
    private String lastQueuedKitId = null;
    private ProfileState state;

    private final Map<ProfileCooldown.CooldownType, ProfileCooldown> activeCooldowns;
    private final CosmeticPlayerData cosmetics = new CosmeticPlayerData();
    private final Map<String, KitStats> kitStats = new ConcurrentHashMap<>();
    private final Map<String, org.bukkit.inventory.ItemStack[]> customLayouts = new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String, org.bukkit.inventory.ItemStack[]> customArmor = new java.util.concurrent.ConcurrentHashMap<>();

    private UUID activePartyId;
    private UUID activeMatchId;
    private final Map<String, Integer> selectedLayouts;

    // Stats
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private int winStreak;
    private int bestWinStreak;
    private int matchesPlayed;
    private long experience;
    private int level = 1;
    private long coins;
    private String lastDailyLogin = "";

    public Profile(UUID uniqueId, String lastKnownName) {
        this.uniqueId = uniqueId;
        this.lastKnownName = lastKnownName;
        this.state = ProfileState.LOBBY;
        this.activeCooldowns = new ConcurrentHashMap<>();
        this.activePartyId = null;
        this.activeMatchId = null;
        this.selectedLayouts = new ConcurrentHashMap<>();
    }

    public org.bukkit.inventory.ItemStack[] getCustomLayout(String kitId) {
        return customLayouts.get(kitId.toLowerCase());
    }

    public void setCustomLayout(String kitId, org.bukkit.inventory.ItemStack[] layout) {
        customLayouts.put(kitId.toLowerCase(), layout);
    }

    public Map<String, org.bukkit.inventory.ItemStack[]> getCustomLayouts() {
        return customLayouts;
    }
    public boolean isAllowDuels() { return allowDuels; }
    public void setAllowDuels(boolean allowDuels) { this.allowDuels = allowDuels; }
    public boolean isAllowPartyInvites() { return allowPartyInvites; }
    public void setAllowPartyInvites(boolean allowPartyInvites) { this.allowPartyInvites = allowPartyInvites; }
    public boolean isScoreboardVisible() { return scoreboardVisible; }
    public void setScoreboardVisible(boolean scoreboardVisible) { this.scoreboardVisible = scoreboardVisible; }
    public UUID getUniqueId() { return uniqueId; }
    public int getKills() { return kills; }
    public void addKill() { this.kills++; }
    public void setKills(int kills) { this.kills = kills; }
    public int getDeaths() { return deaths; }
    public void addDeath() { this.deaths++; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public int getWins() { return wins; }
    public void addWin() {
        this.wins++;
        this.winStreak++;
        if (this.winStreak > this.bestWinStreak) {
            this.bestWinStreak = this.winStreak;
        }
    }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void addLoss() {
        this.losses++;
        this.winStreak = 0;
    }
    public void setLosses(int losses) { this.losses = losses; }
    public CosmeticPlayerData getCosmetics() {return cosmetics;}
    public int getWinStreak() { return winStreak; }
    public void setWinStreak(int winStreak) { this.winStreak = winStreak; }

    public int getBestWinStreak() { return bestWinStreak; }
    public void setBestWinStreak(int bestWinStreak) { this.bestWinStreak = bestWinStreak; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public void addMatchPlayed() { this.matchesPlayed++; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    public void setCustomArmor(String kitId, org.bukkit.inventory.ItemStack[] armor) { customArmor.put(kitId.toLowerCase(), armor); }
    public org.bukkit.inventory.ItemStack[] getCustomArmor(String kitId) { return customArmor.get(kitId.toLowerCase()); }
    public Map<String, org.bukkit.inventory.ItemStack[]> getCustomArmorLayouts() { return customArmor; }

    public String getLastKnownName() { return lastKnownName; }
    public ProfileState getState() { return state; }
    public void setState(ProfileState state) { this.state = state; }
    public Map<ProfileCooldown.CooldownType, ProfileCooldown> getActiveCooldowns() { return activeCooldowns; }
    public Map<String, KitStats> getAllKitStats() { return kitStats; }
    public KitStats getKitStats(String kitId) {
        return kitStats.computeIfAbsent(kitId.toLowerCase(), KitStats::new);
    }
    public UUID getPartyId() { return activePartyId; }
    public void setPartyId(UUID partyId) { this.activePartyId = partyId; }
    public UUID getActivePartyId() { return activePartyId; }
    public void setActivePartyId(UUID activePartyId) { this.activePartyId = activePartyId; }
    public UUID getActiveMatchId() { return activeMatchId; }
    public void setActiveMatchId(UUID activeMatchId) { this.activeMatchId = activeMatchId; }

    public int getSelectedLayout(String kitId) {
        return selectedLayouts.getOrDefault(kitId.toLowerCase(), 1);
    }

    public void setSelectedLayout(String kitId, int layoutIndex) {
        selectedLayouts.put(kitId.toLowerCase(), layoutIndex);
    }

    public Map<String,Integer> getSelectedLayouts() {
        return selectedLayouts;
    }

    public void applyCooldown(ProfileCooldown.CooldownType type, long durationMs) {
        this.activeCooldowns.put(type, new ProfileCooldown(System.currentTimeMillis() + durationMs));
    }

    public void resetCustomLayout(String kitId) {
        if (this.customLayouts != null) {
            this.customLayouts.remove(kitId.toLowerCase());
        }
    }

    public ProfileCooldown getCooldown(ProfileCooldown.CooldownType type) {
        return this.activeCooldowns.get(type);
    }

    public boolean hasCooldown(ProfileCooldown.CooldownType type) {
        ProfileCooldown cooldown = this.activeCooldowns.get(type);
        if (cooldown == null) return false;

        if (!cooldown.isActive()) {
            this.activeCooldowns.remove(type);
            return false;
        }
        return true;
    }

    public int getEloForKit(String kitId) {
        return getKitStats(kitId).getElo();
    }

    public void clearTemporaryMatchState() {
        this.activeMatchId = null;
        this.activeCooldowns.clear();
        this.state = ProfileState.LOBBY;
    }
    public com.ansh.atlaspractice.settings.MapSelectionPreference getMapSelectionPreference() {
        return mapSelectionPreference;
    }
    public void setMapSelectionPreference(
            com.ansh.atlaspractice.settings.MapSelectionPreference mapSelectionPreference) {
        this.mapSelectionPreference = mapSelectionPreference == null
                        ? com.ansh.atlaspractice.settings.MapSelectionPreference.NONE
                        : mapSelectionPreference;
    }
    public boolean isScoreboardEnabled() {
        return scoreboardEnabled;
    }

    public void setScoreboardEnabled(boolean scoreboardEnabled) {
        this.scoreboardEnabled = scoreboardEnabled;
    }

    public com.ansh.atlaspractice.settings.TimeMode getTimeMode() {
        return timeMode;
    }

    public void setTimeMode(com.ansh.atlaspractice.settings.TimeMode timeMode) {
        this.timeMode = timeMode;
    }

    public com.ansh.atlaspractice.settings.ChatMode getChatMode() {
        return chatMode;
    }

    public void setChatMode(com.ansh.atlaspractice.settings.ChatMode chatMode) {
        this.chatMode = chatMode;
    }

    public boolean isAutoGg() {
        return autoGg;
    }

    public void setAutoGg(boolean autoGg) {
        this.autoGg = autoGg;
    }

    public boolean isAutoRequeue() {
        return autoRequeue;
    }

    public void setAutoRequeue(boolean autoRequeue) {
        this.autoRequeue = autoRequeue;
    }

    public long getExperience() { return experience; }
    public void setExperienceRaw(long experience) { this.experience = Math.max(0L, experience); }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = Math.max(1, level); }
    public long getCoins() { return coins; }
    public void setCoinsRaw(long coins) { this.coins = Math.max(0L, coins); }

    public String getLastDailyLogin() { return lastDailyLogin; }
    public void setLastDailyLogin(String lastDailyLogin) { this.lastDailyLogin = lastDailyLogin == null ? "" : lastDailyLogin; }

    public String getLastQueuedKitId() {
        return lastQueuedKitId;
    }

    public void setLastQueuedKitId(String lastQueuedKitId) {
        this.lastQueuedKitId = lastQueuedKitId;
    }
}