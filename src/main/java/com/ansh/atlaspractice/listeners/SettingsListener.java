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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.settings.ChatMode;
import com.ansh.atlaspractice.settings.TimeMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class SettingsListener implements Listener {

    private final AtlasPracticePlugin plugin;

    public SettingsListener(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());

        if (profile != null) {
            // Restore Time Changer
            if (profile.getTimeMode() != TimeMode.SERVER) {
                player.setPlayerTime(profile.getTimeMode().getTime(), false);
            }

            // Enforce Scoreboard Visibility on join
            if (!profile.isScoreboardEnabled()) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        plugin.getScoreboardUpdateTask().unregisterPlayer(player);
                    }
                }, 10L); // Slight delay to ensure it overrides default plugin application
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setMessage(plugin.getCosmeticManager().formatChat(event.getPlayer(), event.getMessage()));
        UUID senderMatch = plugin.getMatchManager().getPlayerMatchId(event.getPlayer().getUniqueId());

        event.getRecipients().removeIf(recipient -> {
            Profile recipientProfile = plugin.getProfileManager().getProfile(recipient.getUniqueId());
            if (recipientProfile == null) return false;

            ChatMode mode = recipientProfile.getChatMode();
            if (mode == ChatMode.ALL) return false;

            if (mode == ChatMode.NONE) {
                // Completely hide player chat
                return true; 
            }

            if (mode == ChatMode.MATCH_ONLY) {
                UUID recipientMatch = plugin.getMatchManager().getPlayerMatchId(recipient.getUniqueId());
                // Only allow message if both are in the EXACT SAME match
                if (recipientMatch != null) {
                    return !recipientMatch.equals(senderMatch);
                } else {
                    return true; // Recipient is in lobby, so block global chat
                }
            }
            return false;
        });
    }
}