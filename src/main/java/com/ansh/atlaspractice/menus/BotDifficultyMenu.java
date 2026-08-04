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

package com.ansh.atlaspractice.menus;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.bots.BotDifficulty;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.match.BotMatch;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.team.TeamColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class BotDifficultyMenu extends Menu {

    private final Kit selectedKit;

    public BotDifficultyMenu(Kit selectedKit) {
        super("&8Select Bot Difficulty", 27);
        this.selectedKit = selectedKit;
    }

    public BotDifficultyMenu() {
        this(null);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(8, new Button(
                createItem(Material.SLIME_BALL, "&a&lEASY",
                        "&7Slow reactions",
                        "&7Lower CPS and imperfect aim",
                        "",
                        "&eClick to start duel!"),
                (p, clickType) -> startBotMatch(p, BotDifficulty.EASY)
        ));

        buttons.put(10, new Button(
                createItem(Material.IRON_SWORD, "&e&lMEDIUM",
                        "&7Average reactions",
                        "&7Balanced tracking and strafing",
                        "",
                        "&eClick to start duel!"),
                (p, clickType) -> startBotMatch(p, BotDifficulty.MEDIUM)
        ));

        buttons.put(12, new Button(
                createItem(Material.DIAMOND_SWORD, "&c&lHARD",
                        "&7Fast reactions",
                        "&7Strong tracking and combo pressure",
                        "",
                        "&eClick to start duel!"),
                (p, clickType) -> startBotMatch(p, BotDifficulty.HARD)
        ));

        buttons.put(14, new Button(
                createItem(Material.NETHER_STAR, "&4&lINSANE",
                        "&7Elite reactions",
                        "&7Prediction, strafing, and sprint resets",
                        "",
                        "&eClick to start duel!"),
                (p, clickType) -> startBotMatch(p, BotDifficulty.INSANE)
        ));

        buttons.put(16, new Button(
                createItem(Material.SKULL_ITEM, "&5&lIMPOSSIBLE",
                        "&7Near-perfect reactions",
                        "&7Tightest timing, max aggression",
                        "&7Only for the best players.",
                        "",
                        "&eClick to start duel!"),
                (p, clickType) -> startBotMatch(p, BotDifficulty.IMPOSSIBLE)
        ));

        return buttons;
    }

    private void startBotMatch(Player player, BotDifficulty difficulty) {
        player.closeInventory();
        AtlasPracticePlugin plugin = AtlasPracticePlugin.getInstance();

        com.ansh.atlaspractice.profile.Profile profile =
                plugin.getProfileManager().getProfile(player.getUniqueId());

        if (profile == null
                || profile.getState() != com.ansh.atlaspractice.profile.ProfileState.LOBBY) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cYou must be in the lobby to start a bot fight."));
            return;
        }

        if (!plugin.getConfig().getBoolean("bot.enabled", plugin.getConfig().getBoolean("bot.boxing-enabled", true))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cBot fights are currently disabled."));
            return;
        }

        Kit boxingKit = selectedKit;
        if (boxingKit == null) {
            Optional<Kit> kitOptional = plugin.getKitManager().getKit("Boxing");
            if (!kitOptional.isPresent()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&cSelected kit is not configured."));
                return;
            }
            boxingKit = kitOptional.get();
        }
        Arena arena = null;

        for (Arena candidate : plugin.getSharedArenaService().getAvailableArenaPool(boxingKit)) {
            if (candidate.isAvailable()) {
                arena = candidate;
                break;
            }
        }

        if (arena == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cNo available arenas for " + boxingKit.getDisplayName() + " right now!"));
            return;
        }

        List<MatchTeam.MatchPlayer> playerMembers = new ArrayList<>();
        playerMembers.add(new MatchTeam.MatchPlayer(player.getUniqueId(), player.getName()));
        MatchTeam playerTeam = new MatchTeam(playerMembers, TeamColor.RED);

        UUID botUuid = UUID.randomUUID();
        List<MatchTeam.MatchPlayer> botMembers = new ArrayList<>();
        botMembers.add(new MatchTeam.MatchPlayer(botUuid, "AtlasBot"));
        MatchTeam botTeam = new MatchTeam(botMembers, TeamColor.BLUE);

        List<MatchTeam> teams = new ArrayList<>();
        teams.add(playerTeam);
        teams.add(botTeam);

        BotMatch match = new BotMatch(boxingKit, arena, teams,
                player.getUniqueId(), botUuid, difficulty);

        plugin.getMatchManager().hostMatch(match);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            if (lore.length > 0) {
                meta.setLore(Arrays.stream(lore)
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList()));
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}