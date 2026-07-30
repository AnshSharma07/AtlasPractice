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

package com.ansh.atlaspractice.scoreboard;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.ArenaMode;
import com.ansh.atlaspractice.match.BridgeMatch;
import com.ansh.atlaspractice.match.DuelMatch;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.party.PartyPvPMatch;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.ansh.atlaspractice.party.Party;
import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import com.ansh.atlaspractice.match.BattleRushMatch;
public final class PracticeScoreboardAdapter implements ScoreboardAdapter {

    private final AtlasPracticePlugin plugin;
    private final ProfileManager profileManager;

    public PracticeScoreboardAdapter(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        this.profileManager = plugin.getProfileManager();
    }

    @Override
    public String getTitle(Player player) {
        String title = plugin.getConfig().getString("scoreboard.title", "&6&lAtlas PvP &7[Practice]");
        return ChatColor.translateAlternateColorCodes('&', title);
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();
        Profile profile = this.profileManager.getProfile(player.getUniqueId());
        if (profile == null) {
            return lines;
        }
        if (!profile.isScoreboardEnabled()) {
            return lines;
        }
        if (profile == null) return lines;
        FileConfiguration config = plugin.getScoreboardConfig();
        List<String> rawLines;

        switch (profile.getState()) {
            case MATCH -> {

                Optional<Match> matchOpt =
                        plugin.getMatchManager()
                                .getMatchByPlayer(player.getUniqueId());

                if (matchOpt.isPresent()) {

                    Match match = matchOpt.get();
                    // KIT-SPECIFIC SCOREBOARDS ALWAYS HAVE HIGHEST PRIORITY
                    if (match.getKit() != null) {

                        String kitId = match.getKit().getId();

                        if (kitId.equalsIgnoreCase("bridge")) {

                            rawLines = config.getStringList(
                                    "scoreboard.bridge.lines"
                            );

                        }
                        else if (kitId.equalsIgnoreCase("battlerush")) {

                            rawLines = config.getStringList(
                                    "scoreboard.battleRush.lines"
                            );

                        }
                        else if (match.getKit().isBoxingMode()) {

                            rawLines = config.getStringList(
                                    "scoreboard.boxing.lines"
                            );

                        }
                        else if (match.getArena() != null
                                && match.getArena().getMode() == ArenaMode.BEDFIGHT) {

                            rawLines = config.getStringList(
                                    "scoreboard.special.lines"
                            );

                        }
                        else {

                            rawLines = config.getStringList(
                                    "scoreboard.match.lines"
                            );

                        }

                    }
                    else {

                        rawLines = config.getStringList(
                                "scoreboard.match.lines"
                        );

                    }

                } else {

                    rawLines = config.getStringList(
                            "scoreboard.match.lines"
                    );
                }
            }
            case QUEUE, QUEUING -> rawLines = config.getStringList("scoreboard.queuing.lines");
            case SPECTATE, SPECTATING -> rawLines = config.getStringList("scoreboard.spectate.lines");
            case LOBBY, PARTY -> {
                boolean insideParty = profile.getPartyId() != null;
                if (insideParty) rawLines = config.getStringList("scoreboard.party.lines");
                else rawLines = config.getStringList("scoreboard.lobby.lines");
            }
            default -> rawLines = config.getStringList("scoreboard.lobby.lines");
        }

        if (rawLines == null || rawLines.isEmpty()) {
            lines.add("&7&m------------------");
            lines.add("&eState: &f" + profile.getState().name());
            lines.add("&cMissing Config Lines!");
            lines.add("&7&m------------------");
            return lines;
        }

        for (String raw : rawLines) {
            lines.add(translatePlaceholders(raw, player, profile));
        }

        return lines;
    }

    private String bridgeDots(int score) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            builder.append(i < score ? "●" : "○");
        }
        return builder.toString();
    }

    private String translatePlaceholders(String text, Player player, Profile profile) {
        if (text == null) return "";

        int playerPing = getPlayerPing(player);
        text = text.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%in_matches%", String.valueOf(
                        plugin.getMatchManager().getLiveMatches().stream()
                                .flatMap(match -> match.getTeams().stream())
                                .mapToInt(team -> team.getPlayers().size())
                                .sum()
                ))
                .replace("%ping%", String.valueOf(playerPing))
                .replace("%atlas_level%", String.valueOf(profile.getLevel()))
                .replace("%atlas_xp%", String.valueOf(profile.getExperience()))
                .replace("%atlas_xp_current%", String.valueOf(AtlasPracticePlugin.getInstance().getLevelManager().getXPIntoCurrentLevel(profile.getExperience())))
                .replace("%atlas_xp_needed%", String.valueOf(AtlasPracticePlugin.getInstance().getLevelManager().getXPForNextLevel(profile.getLevel())))
                .replace("%atlas_coins%", String.format("%,d", profile.getCoins()));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }

        Optional<Match> matchOpt = plugin.getMatchManager().getMatchByPlayer(player.getUniqueId());

        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            UUID opponentUuid = null;

            for (MatchTeam team : match.getTeams()) {
                for (MatchTeam.MatchPlayer mp : team.getPlayers()) {

                    if (!mp.getUuid().equals(player.getUniqueId())) {
                        opponentUuid = mp.getUuid();
                        break;
                    }
                }

                if (opponentUuid != null)
                    break;
            }

            int yourHits = match.getBoxingHits(player.getUniqueId());

            int opponentHits = opponentUuid == null
                    ? 0
                    : match.getBoxingHits(opponentUuid);
            text = text.replace(
                    "%red_bed%",
                    match.isRedBedAlive()
                            ? "✔"
                            : "✘"
            );

            text = text.replace(
                    "%blue_bed%",
                    match.isBlueBedAlive()
                            ? "✔"
                            : "✘"
            );
            String opponentName = "Searching...";
            int enemyCount = 0;
            int opponentPing = 0;

            for (MatchTeam team : match.getTeams()) {
                for (MatchTeam.MatchPlayer mp : team.getPlayers()) {
                    if (!mp.getUuid().equals(player.getUniqueId())) {
                        Player opponent = Bukkit.getPlayer(mp.getUuid());
                        enemyCount++;

                        if (enemyCount == 1) {

                            opponentName = opponent != null
                                    ? opponent.getName()
                                    : "Enemy";

                        } else {

                            opponentName += " +" + (enemyCount - 1);
                        }
                        if (opponent != null) opponentPing = getPlayerPing(opponent);
                    }
                }
            }

            text = text.replace("%opponent%", opponentName)
                    .replace("%opponent_ping%", String.valueOf(opponentPing))
                    .replace("%arena%", match.getKit().getDisplayName())
                    .replace("%duration%", formatDuration(System.currentTimeMillis() - match.getStartTime()));

            if (match instanceof com.ansh.atlaspractice.party.PartyFFAMatch) {
                int alive = match.getTeams().size() - match.getSpectators().size();
                text = text.replace("%round%", "FFA");
                text = text.replace("%score%", String.valueOf(alive));
            } else if (match instanceof PartyPvPMatch pvpMatch) {
                text = text.replace("%round%", pvpMatch.getCurrentRound() + "/" + ((pvpMatch.getRequiredWins() * 2) - 1));
                text = text.replace("%score%", pvpMatch.getTeamOneWins() + "-" + pvpMatch.getTeamTwoWins());
            }
            else if (match instanceof BattleRushMatch battleRushMatch) {

                int red = battleRushMatch.getTeams().isEmpty()
                        ? 0
                        : battleRushMatch.getScore(battleRushMatch.getTeams().get(0));

                int blue = battleRushMatch.getTeams().size() < 2
                        ? 0
                        : battleRushMatch.getScore(battleRushMatch.getTeams().get(1));

                text = text.replace("%round%", "BattleRush");
                text = text.replace("%score%", red + "-" + blue);
                text = text.replace("%red_score%", bridgeDots(red));
                text = text.replace("%blue_score%", bridgeDots(blue));
            }else if (match instanceof BridgeMatch bridgeMatch) {
                int red = bridgeMatch.getTeams().isEmpty() ? 0 : bridgeMatch.getScore(bridgeMatch.getTeams().get(0));
                int blue = bridgeMatch.getTeams().size() < 2 ? 0 : bridgeMatch.getScore(bridgeMatch.getTeams().get(1));
                text = text.replace("%round%", "Bridge");
                text = text.replace("%score%", red + "-" + blue);
                text = text.replace("%red_score%", bridgeDots(red));
                text = text.replace("%blue_score%", bridgeDots(blue));
            } else if (match instanceof DuelMatch duelMatch) {
                text = text.replace("%round%", duelMatch.getCurrentRound() + "/" + duelMatch.getMaxRounds());
                text = text.replace("%score%", duelMatch.getPlayerScore(player) + "-" + duelMatch.getOpponentScore(player));
            } else {
                text = text.replace("%round%", "1/1");
                text = text.replace("%score%", "-");

            }
            text = text.replace("%your_hits%", String.valueOf(yourHits));
            text = text.replace("%enemy_hits%", String.valueOf(opponentHits));
            int combo =
                    match.getComboTracker()
                            .getCombo(player.getUniqueId());

            text = text.replace(
                    "%combo%",
                    combo <= 0
                            ? "&7No Combo"
                            : "&aCombo: &f" + combo
            );
        } else {
            text = text.replace("%round%", "-");
            text = text.replace("%score%", "-");
        }

        if (text.contains("%party_leader%") || text.contains("%party_size%") || text.contains("%party_state%")) {
            if (profile.getPartyId() != null) {

                Optional<Party> partyOpt = plugin.getPartyManager().getParty(profile.getPartyId());

                if (partyOpt.isPresent()) {
                    Party party = partyOpt.get();

                    UUID leaderUuid = party.getLeaderUuid();
                    String leaderName = "Leader";

                    if (leaderUuid != null) {
                        Player leader = Bukkit.getPlayer(leaderUuid);
                        leaderName = (leader != null)
                                ? leader.getName()
                                : Bukkit.getOfflinePlayer(leaderUuid).getName();

                        if (leaderName == null) {
                            leaderName = "Leader";
                        }
                    }

                    text = text.replace("%party_leader%", leaderName)
                            .replace("%party_size%", String.valueOf(party.getMembers().size()))
                            .replace("%party_state%", party.getState() != null
                                    ? party.getState().toString()
                                    : "Idle");

                } else {
                    text = text.replace("%party_leader%", "Leader")
                            .replace("%party_size%", "1")
                            .replace("%party_state%", "Idle");
                }

            } else {
                text = text.replace("%party_leader%", "None")
                        .replace("%party_size%", "0")
                        .replace("%party_state%", "Idle");
            }
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private int getPlayerPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Field pingField = entityPlayer.getClass().getField("ping");
            return pingField.getInt(entityPlayer);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String formatDuration(long ms) {
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}