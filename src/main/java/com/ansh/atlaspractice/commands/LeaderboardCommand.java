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

package com.ansh.atlaspractice.commands;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.leaderboard.LeaderboardManager;
import com.ansh.atlaspractice.leaderboard.StatType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardCommand implements CommandExecutor, TabCompleter {

    private final AtlasPracticePlugin plugin;
    private final Map<UUID, FastSetupSession> sessions = new HashMap<>();

    private static final SetupStep[] STEPS = {
            new SetupStep(StatType.WINS, "Top Wins Leaderboard", true),
            new SetupStep(StatType.KILLS, "Top Kills Leaderboard", true),
            new SetupStep(StatType.DEATHS, "Top Deaths Leaderboard", true),
            new SetupStep(StatType.WINSTREAK, "Top Win Streak Leaderboard", true),
            new SetupStep(StatType.KIT_ELO, "Top ELO Leaderboard", false)
    };

    public LeaderboardCommand(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("atlaspractice.admin")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("fastsetup")) {
            handleFastSetup(player, args);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                createCommand(player, args);
                break;
            case "delete":
                deleteCommand(player, args);
                break;
            case "list":
                listCommand(player);
                break;
            case "reload":
                plugin.reloadConfig();
                plugin.getLeaderboardManager().loadHolograms();
                plugin.getLeaderboardManager().updateAllHolograms();
                plugin.getLeaderboardManager().getCache().refreshNow();
                player.sendMessage(ChatColor.GREEN + "Leaderboards reloaded.");
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void createCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /atlasleaderboard create <name> <type> [kit]");
            return;
        }

        String name = args[1];
        StatType type;
        try {
            type = StatType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid type: " + args[2]);
            return;
        }

        String kit = args.length > 3 ? args[3] : "NONE";
        if (!kit.equalsIgnoreCase("NONE") && plugin.getKitManager().getKit(kit).isEmpty()) {
            player.sendMessage(ChatColor.RED + "Invalid kit.");
            return;
        }

        if (type == StatType.KIT_ELO && kit.equalsIgnoreCase("NONE")) {
            player.sendMessage(ChatColor.RED + "KIT_ELO needs a kit.");
            return;
        }

        if (plugin.getLeaderboardManager().createLeaderboard(name, player.getLocation(), type, kit)) {
            player.sendMessage(ChatColor.GREEN + "Leaderboard " + name + " created.");
            plugin.getLeaderboardManager().getCache().refreshNow();
        } else {
            player.sendMessage(ChatColor.RED + "A leaderboard with that name already exists.");
        }
    }

    private void deleteCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /atlasleaderboard delete <name>");
            return;
        }

        if (plugin.getLeaderboardManager().deleteLeaderboard(args[1])) {
            plugin.getLeaderboardManager().getCache().refreshNow();
            player.sendMessage(ChatColor.GREEN + "Leaderboard deleted.");
        } else {
            player.sendMessage(ChatColor.RED + "Leaderboard not found.");
        }
    }

    private void listCommand(Player player) {
        player.sendMessage(ChatColor.AQUA + "Active Leaderboards:");
        for (com.ansh.atlaspractice.leaderboard.hologram.LeaderboardHologram hologram : plugin.getLeaderboardManager().getHolograms()) {
            String kit = hologram.getKit() == null ? "overall" : hologram.getKit();
            player.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + hologram.getName()
                    + ChatColor.GRAY + " | " + hologram.getType().name()
                    + ChatColor.GRAY + " | " + kit);
        }
    }

    private void handleFastSetup(Player player, String[] args) {
        if (args.length == 1) {
            FastSetupSession session = new FastSetupSession();
            sessions.put(player.getUniqueId(), session);
            player.sendMessage(ChatColor.DARK_AQUA + "---------------- Leaderboard Fast Setup ----------------");
            player.sendMessage(ChatColor.GRAY + "Each YES / ADD places the leaderboard at your current location.");
            player.sendMessage(ChatColor.GRAY + "REMOVE removes the nearest matching leaderboard.");
            player.sendMessage("");
            sendCurrentStep(player, session);
            return;
        }

        if (args.length < 3) {
            sendHelp(player);
            return;
        }

        FastSetupSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "No fast setup is running. Use /atlaslb fastsetup.");
            return;
        }

        String action = args[1].toLowerCase();
        String stepName = args[2].toUpperCase();
        SetupStep step = getStep(stepName);

        if (step == null || step != STEPS[session.stepIndex]) {
            player.sendMessage(ChatColor.RED + "That setup button is no longer active.");
            return;
        }

        switch (action) {
            case "global":
                if (!step.allowGlobal) {
                    player.sendMessage(ChatColor.RED + "This leaderboard is per-kit only.");
                    return;
                }
                if (plugin.getLeaderboardManager().createGeneratedLeaderboard(player.getLocation(), step.type, null)) {
                    player.sendMessage(ChatColor.GREEN + "Added the global " + step.label + ".");
                    plugin.getLeaderboardManager().getCache().refreshNow();
                } else {
                    player.sendMessage(ChatColor.YELLOW + "There is already a matching leaderboard at this location.");
                }
                nextStep(player, session);
                return;

            case "skip":
                nextStep(player, session);
                return;

            case "perkit":
                session.perKit = true;
                sendPerKitStep(player, session, 0);
                return;

            case "add":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Invalid setup button.");
                    return;
                }
                Kit kitToAdd = plugin.getKitManager().getKit(args[3]).orElse(null);
                if (kitToAdd == null) {
                    player.sendMessage(ChatColor.RED + "That kit no longer exists.");
                    sendPerKitStep(player, session, 0);
                    return;
                }
                if (plugin.getLeaderboardManager().createGeneratedLeaderboard(player.getLocation(), step.type, kitToAdd.getId())) {
                    player.sendMessage(ChatColor.GREEN + "Added " + step.label + " for " + kitToAdd.getId() + ".");
                    plugin.getLeaderboardManager().getCache().refreshNow();
                } else {
                    player.sendMessage(ChatColor.YELLOW + "There is already a matching leaderboard at this location.");
                }
                sendPerKitStep(player, session, 0);
                return;

            case "remove":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Invalid setup button.");
                    return;
                }
                Kit kitToRemove = plugin.getKitManager().getKit(args[3]).orElse(null);
                if (kitToRemove == null) {
                    player.sendMessage(ChatColor.RED + "That kit no longer exists.");
                    sendPerKitStep(player, session, 0);
                    return;
                }
                if (plugin.getLeaderboardManager().removeNearest(player.getLocation(), step.type, kitToRemove.getId())) {
                    player.sendMessage(ChatColor.GREEN + "Removed the nearest " + step.label + " for " + kitToRemove.getId() + ".");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "No matching leaderboard was found.");
                }
                sendPerKitStep(player, session, 0);
                return;

            case "page":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Invalid setup button.");
                    return;
                }
                int page;
                try {
                    page = Integer.parseInt(args[3]);
                } catch (NumberFormatException ex) {
                    player.sendMessage(ChatColor.RED + "Invalid page.");
                    return;
                }
                sendPerKitStep(player, session, page);
                return;

            case "next":
                nextStep(player, session);
                return;

            default:
                player.sendMessage(ChatColor.RED + "Invalid setup button.");
                return;
        }
    }

    private void nextStep(Player player, FastSetupSession session) {
        session.perKit = false;
        session.stepIndex++;

        if (session.stepIndex >= STEPS.length) {
            sessions.remove(player.getUniqueId());
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "Leaderboard fast setup is done.");
            player.sendMessage(ChatColor.GRAY + "Use /atlaslb list to see everything currently active.");
            return;
        }

        sendCurrentStep(player, session);
    }

    private void sendCurrentStep(Player player, FastSetupSession session) {
        SetupStep step = STEPS[session.stepIndex];

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + step.label);
        player.sendMessage(ChatColor.GRAY + "Active global leaderboards: "
                + ChatColor.WHITE + plugin.getLeaderboardManager().count(step.type, null));

        if (step.allowGlobal) {
            TextComponent line = new TextComponent(ChatColor.GRAY + "Add global: ");
            line.addExtra(button("[YES]", command("global", step.type), ChatColor.GREEN + "Add it here"));
            line.addExtra(new TextComponent(" "));
            line.addExtra(button("[NO]", command("skip", step.type), ChatColor.RED + "Skip this stat"));
            line.addExtra(new TextComponent(" "));
            line.addExtra(button("[PER-KIT]", command("perkit", step.type), ChatColor.YELLOW + "Manage each kit separately"));
            player.spigot().sendMessage(line);
        } else {
            TextComponent line = new TextComponent(ChatColor.GRAY + "ELO is stored per kit: ");
            line.addExtra(button("[PER-KIT]", command("perkit", step.type), ChatColor.YELLOW + "Manage ELO for each kit"));
            line.addExtra(new TextComponent(" "));
            line.addExtra(button("[SKIP]", command("skip", step.type), ChatColor.RED + "Skip ELO"));
            player.spigot().sendMessage(line);
        }
    }

    private void sendPerKitStep(Player player, FastSetupSession session, int page) {
        SetupStep step = STEPS[session.stepIndex];
        List<Kit> kits = new ArrayList<>(plugin.getKitManager().getAllKits());
        kits.sort(Comparator.comparing(Kit::getId, String.CASE_INSENSITIVE_ORDER));

        int pageSize = 8;
        int maxPage = Math.max(0, (kits.size() - 1) / pageSize);
        int currentPage = Math.max(0, Math.min(page, maxPage));
        int start = currentPage * pageSize;
        int end = Math.min(kits.size(), start + pageSize);

        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + step.label + ChatColor.GRAY + " - per kit");
        player.sendMessage(ChatColor.GRAY + "Add puts one here. Remove takes the nearest one. The number is the active count.");

        for (int i = start; i < end; i++) {
            Kit kit = kits.get(i);
            int count = plugin.getLeaderboardManager().count(step.type, kit.getId());

            TextComponent line = new TextComponent(ChatColor.YELLOW + kit.getId() + ChatColor.GRAY + "  ");
            line.addExtra(button("[ADD]", command("add", step.type, kit.getId()), ChatColor.GREEN + "Add at your current location"));
            line.addExtra(new TextComponent(" "));
            line.addExtra(button("[REMOVE]", command("remove", step.type, kit.getId()), ChatColor.RED + "Remove the nearest one"));
            line.addExtra(new TextComponent(" "));
            line.addExtra(new TextComponent(ChatColor.GRAY + "[" + count + "]"));
            player.spigot().sendMessage(line);
        }

        if (kits.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No kits exist yet.");
        }

        TextComponent bottom = new TextComponent(ChatColor.GRAY + "Page " + (currentPage + 1) + "/" + (maxPage + 1) + "  ");
        if (currentPage > 0) {
            bottom.addExtra(button("[BACK]", command("page", step.type, String.valueOf(currentPage - 1)), ChatColor.YELLOW + "Previous page"));
            bottom.addExtra(new TextComponent(" "));
        }
        if (currentPage < maxPage) {
            bottom.addExtra(button("[NEXT PAGE]", command("page", step.type, String.valueOf(currentPage + 1)), ChatColor.YELLOW + "Next page"));
            bottom.addExtra(new TextComponent(" "));
        }
        bottom.addExtra(button("[NEXT]", command("next", step.type), ChatColor.GREEN + "Continue to the next leaderboard"));
        player.spigot().sendMessage(bottom);
    }

    private TextComponent button(String label, String command, String hover) {
        TextComponent component = new TextComponent(ChatColor.AQUA + label);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        component.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(hover).create()
        ));
        return component;
    }

    private String command(String action, StatType type, String... extra) {
        StringBuilder command = new StringBuilder("/atlasleaderboard fastsetup ")
                .append(action)
                .append(" ")
                .append(type.name());

        for (String value : extra) {
            command.append(" ").append(value);
        }
        return command.toString();
    }

    private SetupStep getStep(String type) {
        for (SetupStep step : STEPS) {
            if (step.type.name().equalsIgnoreCase(type)) {
                return step;
            }
        }
        return null;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.DARK_AQUA + "*** Leaderboard Commands ***");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard fastsetup");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard create <name> <type> [kit]");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard delete <name>");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard list");
        player.sendMessage(ChatColor.AQUA + "/atlasleaderboard reload");
        player.sendMessage(ChatColor.GRAY + "Stat types: " + Arrays.toString(StatType.values()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("fastsetup", "create", "delete", "list", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return Arrays.stream(StatType.values())
                    .map(Enum::name)
                    .filter(s -> s.startsWith(args[2].toUpperCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private static final class SetupStep {
        private final StatType type;
        private final String label;
        private final boolean allowGlobal;

        private SetupStep(StatType type, String label, boolean allowGlobal) {
            this.type = type;
            this.label = label;
            this.allowGlobal = allowGlobal;
        }
    }

    private static final class FastSetupSession {
        private int stepIndex;
        private boolean perKit;
    }
}
