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

import lombok.RequiredArgsConstructor;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


@RequiredArgsConstructor
public final class DataMigrationHandler {

    private final HikariConnectionProvider connectionProvider;
    private final Logger logger;

    public void executeMigrations() {
        try (Connection connection = this.connectionProvider.getConnection();
             Statement statement = connection.createStatement()) {

            // Table 1: Profiles Table
            try {
                statement.executeUpdate(
                        "ALTER TABLE atlas_profiles ADD COLUMN allow_duels INT NOT NULL DEFAULT 1;");
            } catch (SQLException ignored) {}

            try {
                statement.executeUpdate(
                        "ALTER TABLE atlas_profiles ADD COLUMN allow_party_invites INT NOT NULL DEFAULT 1;");
            } catch (SQLException ignored) {}

            try {
                statement.executeUpdate(
                        "ALTER TABLE atlas_profiles ADD COLUMN scoreboard_visible INT NOT NULL DEFAULT 1;");
            } catch (SQLException ignored) {}
            try {
                statement.executeUpdate(
                        "ALTER TABLE atlas_profiles ADD COLUMN scoreboard_enabled INT NOT NULL DEFAULT 1;");
            } catch (SQLException ignored) {}

            try {
                statement.executeUpdate(
                        "ALTER TABLE atlas_profiles ADD COLUMN time_mode TEXT NOT NULL DEFAULT 'SERVER';");
            } catch (SQLException ignored) {}

            try {
                statement.executeUpdate(
                        "ALTER TABLE atlas_profiles ADD COLUMN chat_mode TEXT NOT NULL DEFAULT 'ALL';");
            } catch (SQLException ignored) {}

            try {
                statement.executeUpdate(
                        "ALTER TABLE atlas_profiles ADD COLUMN auto_gg INT NOT NULL DEFAULT 0;");
            } catch (SQLException ignored) {}

            try {
                statement.executeUpdate(
                        "ALTER TABLE atlas_profiles ADD COLUMN auto_requeue INT NOT NULL DEFAULT 0;");
            } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN cosmetic_kill_effect TEXT NOT NULL DEFAULT 'none';"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN cosmetic_victory_effect TEXT NOT NULL DEFAULT 'none';"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN cosmetic_projectile_trail TEXT NOT NULL DEFAULT 'none';"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN cosmetic_walking_trail TEXT NOT NULL DEFAULT 'none';"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN cosmetic_aura TEXT NOT NULL DEFAULT 'none';"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN cosmetic_chat_color TEXT NOT NULL DEFAULT 'white';"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN cosmetic_kill_message TEXT NOT NULL DEFAULT 'default';"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN experience INTEGER NOT NULL DEFAULT 0;"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN level INT NOT NULL DEFAULT 1;"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN coins INTEGER NOT NULL DEFAULT 0;"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN owned_cosmetics TEXT NOT NULL DEFAULT '';"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profiles ADD COLUMN last_daily_login TEXT NOT NULL DEFAULT '';"); } catch (SQLException ignored) {}
            statement.executeUpdate("""
CREATE TABLE IF NOT EXISTS atlas_profiles (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    kills INT NOT NULL DEFAULT 0,
    deaths INT NOT NULL DEFAULT 0,
    wins INT NOT NULL DEFAULT 0,

allow_duels INT NOT NULL DEFAULT 1,
allow_party_invites INT NOT NULL DEFAULT 1,
scoreboard_visible INT NOT NULL DEFAULT 1,

scoreboard_enabled INT NOT NULL DEFAULT 1,
time_mode TEXT NOT NULL DEFAULT 'SERVER',
chat_mode TEXT NOT NULL DEFAULT 'ALL',
auto_gg INT NOT NULL DEFAULT 0,
auto_requeue INT NOT NULL DEFAULT 0,
cosmetic_kill_effect TEXT NOT NULL DEFAULT 'none',
cosmetic_victory_effect TEXT NOT NULL DEFAULT 'none',
cosmetic_projectile_trail TEXT NOT NULL DEFAULT 'none',
cosmetic_walking_trail TEXT NOT NULL DEFAULT 'none',
cosmetic_aura TEXT NOT NULL DEFAULT 'none',
cosmetic_chat_color TEXT NOT NULL DEFAULT 'white',
cosmetic_kill_message TEXT NOT NULL DEFAULT 'default',
experience INTEGER NOT NULL DEFAULT 0,
level INT NOT NULL DEFAULT 1,
coins INTEGER NOT NULL DEFAULT 0,
owned_cosmetics TEXT NOT NULL DEFAULT '',
last_daily_login TEXT NOT NULL DEFAULT '',

first_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
""");
            // Ensure older databases get the new columns without dropping the table
            try { statement.executeUpdate("ALTER TABLE atlas_profile_stats ADD COLUMN kills INT NOT NULL DEFAULT 0;"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profile_stats ADD COLUMN deaths INT NOT NULL DEFAULT 0;"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profile_stats ADD COLUMN matches INT NOT NULL DEFAULT 0;"); } catch (SQLException ignored) {}
            try { statement.executeUpdate("ALTER TABLE atlas_profile_stats ADD COLUMN best_winstreak INT NOT NULL DEFAULT 0;"); } catch (SQLException ignored) {}

            // Table 2: Kit Specific ELO and tracking stas
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS atlas_profile_stats (
                    uuid VARCHAR(36) NOT NULL,
                    kit_id VARCHAR(32) NOT NULL,
                    elo INT NOT NULL DEFAULT 1000,
                    kills INT NOT NULL DEFAULT 0,
                    deaths INT NOT NULL DEFAULT 0,
                    wins INT NOT NULL DEFAULT 0,
                    losses INT NOT NULL DEFAULT 0,
                    matches INT NOT NULL DEFAULT 0,
                    winstreak INT NOT NULL DEFAULT 0,
                    best_winstreak INT NOT NULL DEFAULT 0,
                    PRIMARY KEY (uuid, kit_id),
                    FOREIGN KEY (uuid) REFERENCES atlas_profiles(uuid)
                );
            """);
            // Table 3: Custom Player Kit Layouts
            statement.executeUpdate("""
    CREATE TABLE IF NOT EXISTS atlas_custom_layouts (
        uuid VARCHAR(36) NOT NULL,
        kit_id VARCHAR(32) NOT NULL,
        layout_data TEXT NOT NULL,
        PRIMARY KEY (uuid, kit_id),
        FOREIGN KEY (uuid) REFERENCES atlas_profiles(uuid)
    );
""");

            // Index tuning optimizations
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_kit_elo ON atlas_profile_stats (kit_id, elo DESC);");

            this.logger.info("Database schema checks completed. SQLite structural tables are validated.");

        } catch (SQLException exception) {
            this.logger.log(Level.SEVERE, "CRITICAL: Schema migration pipeline failed to compile structural layout tables!", exception);
            throw new RuntimeException(exception);
        }
    }
}
