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

package com.ansh.atlaspractice.commands;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import com.ansh.atlaspractice.profile.ProfileManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


@RequiredArgsConstructor
public final class SpecCommand implements CommandExecutor {

    private final AtlasPracticePlugin plugin;
    private final ProfileManager profileManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSpectator tracks are bound to graphics contexts and require a physical client.");
            return true;
        }

        Profile spectatorProfile = this.profileManager.getProfile(player.getUniqueId());
        if (spectatorProfile == null) return true;

        if (spectatorProfile.getState() != ProfileState.LOBBY) {
            player.sendMessage("§cYou cannot switch to spectator unless idle in the lobby.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cUsage: /spectate <targetPlayerName>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage("§cTargeted combat participant could not be matched or resolved.");
            return true;
        }

        Optional<Match> targetedMatch = this.plugin.getMatchManager().getMatchByPlayer(target.getUniqueId());
        if (targetedMatch.isEmpty()) {
            player.sendMessage("§cThat player is not currently engaged inside an active match.");
            return true;
        }

        Match match = targetedMatch.get();
        spectatorProfile.setState(ProfileState.SPECTATE);
        spectatorProfile.setActiveMatchId(match.getId());
        match.getSpectators().add(player.getUniqueId());

        player.setGameMode(GameMode.ADVENTURE);

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        player.setAllowFlight(true);
        player.setFlying(true);

        ItemStack leaveItem = new ItemStack(Material.BED);

        ItemMeta meta = leaveItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§cLeave Spectator");
            leaveItem.setItemMeta(meta);
        }

        player.getInventory().setItem(8, leaveItem);

        player.updateInventory();

        player.teleport(target.getLocation().add(0, 2, 0));

        // hide spectator from active players
        match.getTeams().forEach(team -> team.getPlayers().forEach(mp -> {
            Player matchParticipant = Bukkit.getPlayer(mp.getUuid());
            if (matchParticipant != null) {
                matchParticipant.hidePlayer(player);
            }
        }));

        player.sendMessage("§eNow spectating match: §a" + match.getKit().getDisplayName() + " §7(" + match.getArena().getDisplayName() + ")");
        return true;
    }
}
