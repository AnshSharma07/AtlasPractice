/*
 * AtlasPractice - Open-source Minecraft Practice plugin.
 * Copyright (C) 2026 Ansh Sharma (Modular Boy Ansh)
 *
 * This file is part of AtlasPractice.
 *
 * AtlasPractice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

package com.ansh.atlaspractice.replay;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.match.DuelMatch;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class ReplayManager {

    private static final DateTimeFormatter ID_DATE = DateTimeFormatter
            .ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);

    private final AtlasPracticePlugin plugin;
    private final ReplayStorage storage;

    private final Map<UUID, ReplayMetadata> active = new ConcurrentHashMap<>();
    private final Map<String, ReplayMetadata> saved = new ConcurrentHashMap<>();

    private final boolean available;

    public ReplayManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
        this.storage = new ReplayStorage(plugin);

        available = Bukkit.getPluginManager().getPlugin("AdvancedReplay") != null
                || classExists("me.jumper251.replay.api.ReplayAPI");

        if (!available) {
            plugin.getLogger().warning(
                    "AtlasReplay was not found; Atlas replay recording is disabled."
            );
        }
    }

    public void startRecording(Match match) {
        if (!available || active.containsKey(match.getId())) {
            return;
        }

        List<Player> players = getOnlinePlayers(match);
        if (players.isEmpty()) {
            return;
        }

        ReplayMetadata metadata = createMetadata(match);

        try {
            Object api = getReplayApi();
            Method record = api.getClass().getMethod(
                    "recordReplay",
                    String.class,
                    CommandSender.class,
                    List.class
            );

            record.invoke(
                    api,
                    metadata.getReplayId(),
                    Bukkit.getConsoleSender(),
                    players
            );

            active.put(match.getId(), metadata);

            match.broadcastMessage(
                    "§aReplay recording started.\n\nThis match is now being recorded."
            );
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Unable to start replay recording for match " + match.getId(),
                    exception
            );
        }
    }

    public void stopRecording(Match match, MatchTeam winnerTeam) {
        ReplayMetadata metadata = active.remove(match.getId());

        if (!available || metadata == null) {
            return;
        }

        metadata.setDurationMillis(
                System.currentTimeMillis() - match.getStartTime()
        );

        metadata.setWinner(
                winnerTeam == null ? "No one" : winnerTeam.getLeaderName()
        );

        metadata.setLoser(resolveLoser(match, winnerTeam));

        try {
            Object api = getReplayApi();
            Method stop = api.getClass().getMethod(
                    "stopReplay",
                    String.class,
                    boolean.class
            );

            stop.invoke(api, metadata.getReplayId(), true);

            saved.put(metadata.getReplayId(), metadata);
            storage.save(metadata);

            sendSavedMessage(match, metadata.getReplayId());
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Unable to stop replay recording " + metadata.getReplayId(),
                    exception
            );
        }
    }

    public void playReplay(Player player, String replayId) {
        if (!available) {
            player.sendMessage("§cReplay playback is unavailable.");
            return;
        }

        if (!saved.containsKey(replayId)) {
            player.sendMessage(
                    "§cThat replay is not available on this server session."
            );
            return;
        }

        resetToLobbyState(player);

        try {
            Object api = getReplayApi();
            Method play = api.getClass().getMethod(
                    "playReplay",
                    String.class,
                    Player.class
            );

            play.invoke(api, replayId, player);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Unable to play replay " + replayId + " for " + player.getName(),
                    exception
            );

            resetToLobbyState(player);
            player.sendMessage("§cUnable to open that replay right now.");
        }
    }

    public void shutdown() {
        for (UUID matchId : new ArrayList<>(active.keySet())) {
            ReplayMetadata metadata = active.remove(matchId);

            if (metadata == null) {
                continue;
            }

            try {
                Object api = getReplayApi();

                api.getClass()
                        .getMethod("stopReplay", String.class, boolean.class)
                        .invoke(api, metadata.getReplayId(), true);
            } catch (ReflectiveOperationException | RuntimeException exception) {
                plugin.getLogger().log(
                        Level.WARNING,
                        "Unable to stop replay during shutdown: "
                                + metadata.getReplayId(),
                        exception
                );
            }
        }
    }

    private ReplayMetadata createMetadata(Match match) {
        String kit = match.getKit() != null
                ? match.getKit().getId()
                : "unknown";

        String arena = match.getArena() != null
                ? match.getArena().getDisplayName()
                : "unknown";

        String type;

        if (match instanceof DuelMatch && ((DuelMatch) match).isRanked()) {
            type = "Ranked";
        } else {
            type = match.getClass().getSimpleName();
        }

        String replayId = "atlas-"
                + ID_DATE.format(Instant.now())
                + "-"
                + match.getId().toString().substring(0, 8);

        return new ReplayMetadata(
                replayId,
                UUID.randomUUID(),
                match.getId(),
                arena,
                kit,
                getPlayerNames(match),
                Instant.now(),
                type
        );
    }

    private List<Player> getOnlinePlayers(Match match) {
        List<Player> players = new ArrayList<>();

        for (MatchTeam team : match.getTeams()) {
            for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {
                Player player = Bukkit.getPlayer(matchPlayer.getUuid());

                if (player != null && player.isOnline()) {
                    players.add(player);
                }
            }
        }

        return players;
    }

    private List<String> getPlayerNames(Match match) {
        List<String> players = new ArrayList<>();

        for (MatchTeam team : match.getTeams()) {
            for (MatchTeam.MatchPlayer matchPlayer : team.getPlayers()) {
                players.add(matchPlayer.getUsername());
            }
        }

        return players;
    }

    private String resolveLoser(Match match, MatchTeam winnerTeam) {
        if (winnerTeam == null) {
            return "Unknown";
        }

        for (MatchTeam team : match.getTeams()) {
            if (team != winnerTeam) {
                return team.getLeaderName();
            }
        }

        return "Unknown";
    }

    private void sendSavedMessage(Match match, String replayId) {
        TextComponent click = new TextComponent("§e[CLICK HERE]");
        click.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/atlasreplayview " + replayId
        ));

        TextComponent view = new TextComponent("\n§7View Match Replay");

        for (Player player : getOnlinePlayers(match)) {
            player.sendMessage("§aReplay saved successfully!\n");
            player.spigot().sendMessage(click, view);
        }
    }

    private void resetToLobbyState(Player player) {
        Profile profile = plugin.getProfileManager()
                .getProfile(player.getUniqueId());

        if (profile != null) {
            profile.clearTemporaryMatchState();

            if (profile.getPartyId() == null) {
                profile.setState(ProfileState.LOBBY);
            } else {
                profile.setState(ProfileState.PARTY);
            }
        }

        player.closeInventory();
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setAllowFlight(false);
        player.setFlying(false);

        if (profile != null && profile.getPartyId() != null) {
            plugin.getInventoryUtil().applyPartyHotbarItems(player);
        } else {
            plugin.getInventoryUtil().applyLobbyHotbarItems(player);
        }

        teleportToLobby(player);
    }

    private void teleportToLobby(Player player) {
        if (!plugin.getConfig().contains("lobby-spawn.world")) {
            player.teleport(player.getWorld().getSpawnLocation());
            return;
        }

        String worldName = plugin.getConfig().getString("lobby-spawn.world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            player.teleport(player.getWorld().getSpawnLocation());
            return;
        }

        Location location = new Location(
                world,
                plugin.getConfig().getDouble("lobby-spawn.x"),
                plugin.getConfig().getDouble("lobby-spawn.y"),
                plugin.getConfig().getDouble("lobby-spawn.z"),
                (float) plugin.getConfig().getDouble("lobby-spawn.yaw"),
                (float) plugin.getConfig().getDouble("lobby-spawn.pitch")
        );

        player.teleport(location);
    }

    private Object getReplayApi() throws ReflectiveOperationException {
        Class<?> apiClass = Class.forName(
                "me.jumper251.replay.api.ReplayAPI"
        );

        return apiClass.getMethod("getInstance").invoke(null);
    }

    private boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
}