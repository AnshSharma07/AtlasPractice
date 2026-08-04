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

package com.ansh.atlaspractice.progression.command;
import com.ansh.atlaspractice.AtlasPracticePlugin;import com.ansh.atlaspractice.profile.Profile;import org.bukkit.*;import org.bukkit.command.*;import org.bukkit.entity.Player;import java.util.*;import java.util.stream.Collectors;
public class CoinsCommand implements CommandExecutor, TabCompleter { private final AtlasPracticePlugin plugin; public CoinsCommand(AtlasPracticePlugin p){plugin=p;} public boolean onCommand(CommandSender s,Command c,String l,String[] a){ if(!s.hasPermission("atlaspractice.admin.progression")){s.sendMessage("§cNo permission.");return true;} if(a.length<2){s.sendMessage("§c/coins <give|take|set|info> <player> [amount]");return true;} Player t=Bukkit.getPlayer(a[1]); if(t==null){s.sendMessage("§cPlayer not found.");return true;} Profile p=plugin.getProfileManager().getProfile(t.getUniqueId()); if(p==null){s.sendMessage("§cProfile not loaded.");return true;} String sub=a[0].toLowerCase(); if(sub.equals("info")){s.sendMessage("§e"+t.getName()+" §7Coins: §f"+p.getCoins()+" §7Level: §f"+p.getLevel()+" §7XP: §f"+p.getExperience());return true;} if(a.length<3){s.sendMessage("§cAmount required.");return true;} long amt; try{amt=Long.parseLong(a[2]);}catch(Exception e){s.sendMessage("§cInvalid amount.");return true;} switch(sub){case "give": plugin.getLevelManager().addCoins(p,amt); break; case "take": plugin.getLevelManager().removeCoins(p,amt); break; case "set": plugin.getLevelManager().setCoins(p,amt); break; default: s.sendMessage("§cUnknown subcommand."); return true;} plugin.getDatabaseService().saveProfileDataAsync(p); s.sendMessage("§aUpdated coins for "+t.getName()+"."); return true;} public List<String> onTabComplete(CommandSender s,Command c,String l,String[] a){ if(a.length==1)return Arrays.asList("give","take","set","info").stream().filter(x->x.startsWith(a[0].toLowerCase())).collect(Collectors.toList()); if(a.length==2)return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n->n.toLowerCase().startsWith(a[1].toLowerCase())).collect(Collectors.toList()); return Collections.emptyList();}}
