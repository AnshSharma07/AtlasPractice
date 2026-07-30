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

package com.ansh.atlaspractice.database;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.profile.Profile;
import lombok.RequiredArgsConstructor;
import com.ansh.atlaspractice.profile.ProfileState;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public final class SQLiteDatabase implements DatabaseService {

    private final AtlasPracticePlugin plugin;
    private final HikariConnectionProvider provider = new HikariConnectionProvider();
    private DataMigrationHandler migrationHandler;

    public HikariConnectionProvider getProvider() {
        return provider;
    }
    
    @Override
    public void connect() {
        this.plugin.getLogger().info("Connecting to local SQLite database...");
        this.provider.initialize(this.plugin);
        this.migrationHandler = new DataMigrationHandler(this.provider, this.plugin.getLogger());
        this.migrationHandler.executeMigrations();
    }

    @Override
    public void shutdown() {
        this.plugin.getLogger().info("Draining pending database worker threads safely...");
        this.provider.close();
    }

    @Override
    public Profile loadProfileData(UUID uniqueId, String username) {
        Profile profile = new Profile(uniqueId, username);
        String stringUuid = uniqueId.toString();

        String statsFetchQuery = "SELECT * FROM atlas_profile_stats WHERE uuid = ?;";

        try (Connection connection = this.provider.getConnection()) {

            // LOAD GLOBAL METRICS: Pull username, kills, deaths, wins directly from atlas_profiles
            try (PreparedStatement insertStatement = connection.prepareStatement("INSERT OR IGNORE INTO atlas_profiles (uuid, username, kills, deaths, wins) VALUES (?, ?, 0, 0, 0);");
                 PreparedStatement updateStatement = connection.prepareStatement("UPDATE atlas_profiles SET username = ? WHERE uuid = ?;")) {

                insertStatement.setString(1, stringUuid);
                insertStatement.setString(2, username);
                insertStatement.executeUpdate();

                updateStatement.setString(1, username);
                updateStatement.setString(2, stringUuid);
                updateStatement.executeUpdate();
            }

            // READ STRUCTURAL DATA FIELDS
            try (PreparedStatement fetchGlobalStatement =
                         connection.prepareStatement(
                                 "SELECT kills, deaths, wins, allow_duels, allow_party_invites, scoreboard_visible, scoreboard_enabled, time_mode, chat_mode, auto_gg, auto_requeue, cosmetic_kill_effect, cosmetic_victory_effect, cosmetic_projectile_trail, cosmetic_walking_trail, cosmetic_aura, cosmetic_chat_color, cosmetic_kill_message, experience, level, coins, owned_cosmetics, last_daily_login FROM atlas_profiles WHERE uuid = ?;")) {
                fetchGlobalStatement.setString(1, stringUuid);
                try (ResultSet rs = fetchGlobalStatement.executeQuery()) {
                    if (rs.next()) {
                        profile.setKills(rs.getInt("kills"));
                        profile.setDeaths(rs.getInt("deaths"));
                        profile.setWins(rs.getInt("wins"));
                        profile.setAllowDuels(
                                rs.getInt("allow_duels") == 1);
                        profile.setAllowPartyInvites(
                                rs.getInt("allow_party_invites") == 1);
                        profile.setScoreboardVisible(
                                rs.getInt("scoreboard_visible") == 1);
                        profile.setScoreboardEnabled(
                                rs.getInt("scoreboard_enabled") == 1);

                        String timeMode = rs.getString("time_mode");
                        if (timeMode != null) {
                            profile.setTimeMode(com.ansh.atlaspractice.settings.TimeMode.valueOf(timeMode));
                        }

                        String chatMode = rs.getString("chat_mode");
                        if (chatMode != null) {
                            profile.setChatMode(com.ansh.atlaspractice.settings.ChatMode.valueOf(chatMode));
                        }

                        profile.setAutoGg(
                                rs.getInt("auto_gg") == 1);

                        profile.setAutoRequeue(
                                rs.getInt("auto_requeue") == 1);
                        profile.getCosmetics().setKillEffect(rs.getString("cosmetic_kill_effect"));
                        profile.getCosmetics().setVictoryEffect(rs.getString("cosmetic_victory_effect"));
                        profile.getCosmetics().setProjectileTrail(rs.getString("cosmetic_projectile_trail"));
                        profile.getCosmetics().setWalkingTrail(rs.getString("cosmetic_walking_trail"));
                        profile.getCosmetics().setAura(rs.getString("cosmetic_aura"));
                        profile.getCosmetics().setChatColor(rs.getString("cosmetic_chat_color"));
                        profile.getCosmetics().setKillMessage(rs.getString("cosmetic_kill_message"));
                        profile.setExperienceRaw(rs.getLong("experience"));
                        profile.setLevel(rs.getInt("level"));
                        profile.setCoinsRaw(rs.getLong("coins"));
                        profile.getCosmetics().loadOwnedCosmetics(rs.getString("owned_cosmetics"));
                        profile.setLastDailyLogin(rs.getString("last_daily_login"));
                    }
                }
            }

            // loads per kit stats
            try (PreparedStatement fetchStatsStatement = connection.prepareStatement(statsFetchQuery)) {
                fetchStatsStatement.setString(1, stringUuid);
                try (ResultSet resultSet = fetchStatsStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String kitId = resultSet.getString("kit_id");
                        com.ansh.atlaspractice.profile.KitStats stats = profile.getKitStats(kitId);

                        stats.setElo(resultSet.getInt("elo"));
                        stats.setKills(resultSet.getInt("kills"));
                        stats.setDeaths(resultSet.getInt("deaths"));
                        stats.setWins(resultSet.getInt("wins"));
                        stats.setLosses(resultSet.getInt("losses"));
                        stats.setMatches(resultSet.getInt("matches"));
                        stats.setWinstreak(resultSet.getInt("winstreak"));
                        stats.setBestWinstreak(resultSet.getInt("best_winstreak"));
                    }
                }
            }

            String layoutFetchQuery = "SELECT kit_id, layout_data FROM atlas_custom_layouts WHERE uuid = ?;";
            try (PreparedStatement fetchLayoutStatement = connection.prepareStatement(layoutFetchQuery)) {
                fetchLayoutStatement.setString(1, stringUuid);
                try (ResultSet resultSet = fetchLayoutStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String kitId = resultSet.getString("kit_id");
                        String base64Data = resultSet.getString("layout_data");
                        profile.setCustomLayout(kitId, com.ansh.atlaspractice.util.InventoryUtil.deserializeItemStacks(base64Data));
                    }
                }
            }

        } catch (SQLException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to load profile data for UUID: " + stringUuid, exception);
        }

        return profile;
    }

    @Override
    public void saveProfileData(Profile profile) {
        String stringUuid = profile.getUniqueId().toString();
        String upsertQuery = "INSERT OR REPLACE INTO atlas_profile_stats (uuid, kit_id, elo, kills, deaths, wins, losses, matches, winstreak, best_winstreak) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = this.provider.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // SAVE global metrics: Write core profile variables back to main table layout
                try (PreparedStatement saveGlobalStatement =
                             connection.prepareStatement(
                                     "UPDATE atlas_profiles SET kills = ?, deaths = ?, wins = ?, allow_duels = ?, allow_party_invites = ?, scoreboard_visible = ?, scoreboard_enabled = ?, time_mode = ?, chat_mode = ?, auto_gg = ?, auto_requeue = ?, cosmetic_kill_effect = ?, cosmetic_victory_effect = ?, cosmetic_projectile_trail = ?, cosmetic_walking_trail = ?, cosmetic_aura = ?, cosmetic_chat_color = ?, cosmetic_kill_message = ?, experience = ?, level = ?, coins = ?, owned_cosmetics = ?, last_daily_login = ? WHERE uuid = ?;")) {
                    saveGlobalStatement.setInt(1, profile.getKills());
                    saveGlobalStatement.setInt(2, profile.getDeaths());
                    saveGlobalStatement.setInt(3, profile.getWins());
                    saveGlobalStatement.setInt(4, profile.isAllowDuels() ? 1 : 0);
                    saveGlobalStatement.setInt(5, profile.isAllowPartyInvites() ? 1 : 0);
                    saveGlobalStatement.setInt(6, profile.isScoreboardVisible() ? 1 : 0);
                    saveGlobalStatement.setInt(7, profile.isScoreboardEnabled() ? 1 : 0);
                    saveGlobalStatement.setString(8, profile.getTimeMode().name());
                    saveGlobalStatement.setString(9, profile.getChatMode().name());
                    saveGlobalStatement.setInt(10, profile.isAutoGg() ? 1 : 0);
                    saveGlobalStatement.setInt(11, profile.isAutoRequeue() ? 1 : 0);
                    saveGlobalStatement.setString(12, profile.getCosmetics().getKillEffect());
                    saveGlobalStatement.setString(13, profile.getCosmetics().getVictoryEffect());
                    saveGlobalStatement.setString(14, profile.getCosmetics().getProjectileTrail());
                    saveGlobalStatement.setString(15, profile.getCosmetics().getWalkingTrail());
                    saveGlobalStatement.setString(16, profile.getCosmetics().getAura());
                    saveGlobalStatement.setString(17, profile.getCosmetics().getChatColor());
                    saveGlobalStatement.setString(18, profile.getCosmetics().getKillMessage());
                    saveGlobalStatement.setLong(19, profile.getExperience());
                    saveGlobalStatement.setInt(20, profile.getLevel());
                    saveGlobalStatement.setLong(21, profile.getCoins());
                    saveGlobalStatement.setString(22, profile.getCosmetics().serializeOwnedCosmetics());
                    saveGlobalStatement.setString(23, profile.getLastDailyLogin());
                    saveGlobalStatement.setString(24, stringUuid);
                    saveGlobalStatement.executeUpdate();
                }
// save perkit stats
                try (PreparedStatement saveStatsStatement = connection.prepareStatement(upsertQuery)) {
                    for (com.ansh.atlaspractice.profile.KitStats stats : profile.getAllKitStats().values()) {
                        saveStatsStatement.setString(1, stringUuid);
                        saveStatsStatement.setString(2, stats.getKitName());
                        saveStatsStatement.setInt(3, stats.getElo());
                        saveStatsStatement.setInt(4, stats.getKills());
                        saveStatsStatement.setInt(5, stats.getDeaths());
                        saveStatsStatement.setInt(6, stats.getWins());
                        saveStatsStatement.setInt(7, stats.getLosses());
                        saveStatsStatement.setInt(8, stats.getMatches());
                        saveStatsStatement.setInt(9, stats.getWinstreak());
                        saveStatsStatement.setInt(10, stats.getBestWinstreak());
                        saveStatsStatement.addBatch();
                    }
                    saveStatsStatement.executeBatch();
                }

                String upsertLayoutQuery = "INSERT OR REPLACE INTO atlas_custom_layouts (uuid, kit_id, layout_data) VALUES (?, ?, ?);";
                try (PreparedStatement saveLayoutStatement = connection.prepareStatement(upsertLayoutQuery)) {
                    for (var entry : profile.getCustomLayouts().entrySet()) {
                        saveLayoutStatement.setString(1, stringUuid);
                        saveLayoutStatement.setString(2, entry.getKey());
                        saveLayoutStatement.setString(3, com.ansh.atlaspractice.util.InventoryUtil.serializeItemStacks(entry.getValue()));
                        saveLayoutStatement.addBatch();
                    }
                    saveLayoutStatement.executeBatch();
                }

                connection.commit();

            } catch (SQLException batchException) {
                connection.rollback();
                throw batchException;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to execute profile serialization save for UUID: " + stringUuid, exception);
        }
    }
}
