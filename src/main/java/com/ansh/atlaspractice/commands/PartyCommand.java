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
import com.ansh.atlaspractice.menus.PartyDuelKitMenu;
import com.ansh.atlaspractice.party.Party;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileState;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
public final class PartyCommand implements CommandExecutor, TabCompleter {

    private final AtlasPracticePlugin plugin;
    public PartyCommand(AtlasPracticePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("§8§m----------------------------------------------------");
            player.sendMessage("                 §b§l§nPARTY COMMANDS§r                 ");
            player.sendMessage(" ");
            player.sendMessage(" §bâ”ƒ §f/party create §7- §7Create your party");
            player.sendMessage(" §bâ”ƒ §f/party invite §7<player> §7- §7Invite a member");
            player.sendMessage(" §bâ”ƒ §f/party accept §7<leader> §7- §7Join a party / accept duel");
            player.sendMessage(" §bâ”ƒ §f/party deny §7<leader> §7- §7Decline party / decline duel");
            player.sendMessage(" §bâ”ƒ §f/party duel §7<leader> §7- §7Challenge another party");
            player.sendMessage(" §bâ”ƒ §f/party leave §7- §7Leave your current party");
            player.sendMessage(" §bâ”ƒ §f/party kick §7<player> §7- §7Remove a member");
            player.sendMessage(" §bâ”ƒ §f/party promote §7<player> §7- §7Promote a member");
            player.sendMessage(" §bâ”ƒ §f/party disband §7- §7Disband the party");
            player.sendMessage(" §bâ”ƒ §f/party info §7- §7View party statistics");
            player.sendMessage(" §bâ”ƒ §f/party split §7- §7Split the party");
            player.sendMessage(" §bâ”ƒ §f/party chat §7<message> §7- §7Talk in party");
            player.sendMessage(" ");
            player.sendMessage("§8§m----------------------------------------------------");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create": plugin.getPartyManager().createParty(player); return true;
            case "invite":
                if (args.length < 2) { player.sendMessage("§cUsage: /party invite <player>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { player.sendMessage("§cPlayer not found."); return true; }
                plugin.getPartyManager().sendInvite(player, target);
                return true;
            case "accept":
                if (args.length < 2) { player.sendMessage("§cUsage: /party accept <leader>"); return true; }
                Player leader = Bukkit.getPlayer(args[1]);
                if (leader == null) { player.sendMessage("§cLeader not found."); return true; }
                plugin.getPartyManager().acceptInvite(player, leader);
                return true;
            case "deny":
                if (args.length < 2) { player.sendMessage("§cUsage: /party deny <leader>"); return true; }
                Player dLeader = Bukkit.getPlayer(args[1]);
                if (dLeader == null) { player.sendMessage("§cLeader not found."); return true; }
                plugin.getPartyManager().denyInvite(player, dLeader);
                return true;
            case "duel":
                if (args.length < 2) { player.sendMessage("§cUsage: /party duel <leader>"); return true; }
                Player duelTarget = Bukkit.getPlayer(args[1]);
                if (duelTarget == null) { player.sendMessage("§cPlayer not found."); return true; }
                new PartyDuelKitMenu(plugin, duelTarget.getUniqueId()).openMenu(player);
                return true;
            case "leave": plugin.getPartyManager().leaveParty(player); return true;
            case "disband": plugin.getPartyManager().disbandParty(player); return true;
            case "promote":
                if (args.length < 2) { player.sendMessage("§cUsage: /party promote <player>"); return true; }
                Player promoteTarget = Bukkit.getPlayer(args[1]);
                if (promoteTarget == null) { player.sendMessage("§cPlayer not found."); return true; }
                plugin.getPartyManager().promoteLeader(player, promoteTarget);
                return true;
            case "kick":
                if (args.length < 2) { player.sendMessage("§cUsage: /party kick <player>"); return true; }
                Player kickTarget = Bukkit.getPlayer(args[1]);
                if (kickTarget == null) { player.sendMessage("§cPlayer not found."); return true; }
                Profile kickerProfile = plugin.getProfileManager().getProfile(player.getUniqueId());
                if (kickerProfile == null || kickerProfile.getPartyId() == null) { player.sendMessage("§cYou are not in a party."); return true; }
                Party party = plugin.getPartyManager().getParty(kickerProfile.getPartyId()).orElse(null);
                if (party == null || !party.isLeader(player.getUniqueId()) || party.getState() != com.ansh.atlaspractice.party.PartyState.LOBBY) { player.sendMessage("§cCannot kick right now."); return true; }
                if (!party.contains(kickTarget.getUniqueId()) || kickTarget.equals(player)) return true;
                party.removeMember(kickTarget.getUniqueId());
                Profile kickedProfile = plugin.getProfileManager().getProfile(kickTarget.getUniqueId());
                if (kickedProfile != null) { kickedProfile.setPartyId(null); kickedProfile.setState(ProfileState.LOBBY); }
                plugin.getInventoryUtil().applyLobbyHotbarItems(kickTarget);
                party.broadcastMessage("§c" + kickTarget.getName() + " was kicked.");
                kickTarget.sendMessage("§cYou were kicked.");
                return true;
            case "info":
                Profile infoProfile = plugin.getProfileManager().getProfile(player.getUniqueId());
                if (infoProfile == null || infoProfile.getPartyId() == null) return true;
                Party infoParty = plugin.getPartyManager().getParty(infoProfile.getPartyId()).orElse(null);
                if (infoParty == null) return true;
                player.sendMessage("§b§lParty Info");
                player.sendMessage("§bLeader: §f" + infoParty.getLeaderName());
                player.sendMessage("§7Members (§f" + infoParty.size() + "§7):");
                infoParty.getOnlineMembers().forEach(member -> player.sendMessage(" §8- §f" + member.getName()));
                return true;
            case "split": plugin.getPartyManager().openSplitMenu(player); return true;
            case "chat":
                if (args.length < 2) return true;
                Profile chatProfile = plugin.getProfileManager().getProfile(player.getUniqueId());
                if (chatProfile == null || chatProfile.getPartyId() == null) return true;
                Party chatParty = plugin.getPartyManager().getParty(chatProfile.getPartyId()).orElse(null);
                if (chatParty == null) return true;
                StringBuilder message = new StringBuilder();
                for (int i = 1; i < args.length; i++) message.append(args[i]).append(i != args.length - 1 ? " " : "");
                chatParty.broadcastMessage("§d[Party] §f" + player.getName() + "§7: " + message);
                return true;
            default: player.sendMessage("§cUnknown subcommand."); return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) if (args.length == 1)
            return Arrays.asList(
                            "create",
                            "invite",
                            "accept",
                            "deny",
                            "duel",
                            "leave",
                            "kick",
                            "promote",
                            "disband",
                            "info",
                            "split",
                            "chat"
                    ).stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 2 && (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny") || args[0].equalsIgnoreCase("duel"))) {
            List<String> players = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.getUniqueId().equals(player.getUniqueId())) players.add(online.getName());
            }
            return players.stream()
                    .filter(p -> p.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}