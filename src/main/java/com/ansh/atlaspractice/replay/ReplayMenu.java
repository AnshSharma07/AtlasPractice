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
import com.ansh.atlaspractice.menus.Button;
import com.ansh.atlaspractice.menus.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplayMenu extends Menu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final AtlasPracticePlugin plugin;
    private final ReplayManager replayManager;
    private final boolean yourReplays;
    private final int page;
    private final List<ReplayMetadata> searchResults;

    public ReplayMenu(AtlasPracticePlugin plugin, ReplayManager replayManager,
                      boolean yourReplays, int page, List<ReplayMetadata> searchResults) {
        super(yourReplays ? "§8Your Replays" : "§8All Replays", 54);

        this.plugin = plugin;
        this.replayManager = replayManager;
        this.yourReplays = yourReplays;
        this.page = page < 1 ? 1 : page;
        this.searchResults = searchResults;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<ReplayMetadata> replays = getReplays(player);

        int totalPages = (replays.size() + SLOTS.length - 1) / SLOTS.length;
        if (totalPages < 1) {
            totalPages = 1;
        }

        final int pages = totalPages;

        int currentPage = page;
        if (currentPage > pages) {
            currentPage = pages;
        }

        final int pageNumber = currentPage;
        int start = (pageNumber - 1) * SLOTS.length;

        ItemStack glass = makeItem(
                Material.STAINED_GLASS_PANE,
                15,
                " ",
                new ArrayList<String>()
        );

        for (int i = 0; i < 54; i++) {
            buttons.put(i, new Button(glass, (p, click) -> {
            }));
        }

        ItemStack allReplays = makeItem(
                Material.CHEST,
                0,
                "§e§lAll Replays",
                Arrays.asList("§7Every saved match replay.")
        );

        buttons.put(3, new Button(allReplays, (p, click) -> {
            new ReplayMenu(
                    plugin,
                    replayManager,
                    false,
                    1,
                    null
            ).openMenu(p);
        }));

        ItemStack yourReplaysItem = makeItem(
                Material.PAPER,
                0,
                "§a§lYour Replays",
                Arrays.asList("§7Replays from your matches.")
        );

        buttons.put(5, new Button(yourReplaysItem, (p, click) -> {
            new ReplayMenu(
                    plugin,
                    replayManager,
                    true,
                    1,
                    null
            ).openMenu(p);
        }));

        int slot = 0;

        for (int i = start; i < replays.size() && slot < SLOTS.length; i++) {
            ReplayMetadata replay = replays.get(i);
            int menuSlot = SLOTS[slot];

            buttons.put(menuSlot, new Button(
                    getReplayItem(replay),
                    (p, click) -> {
                        new ReplayDetailsMenu(
                                plugin,
                                replayManager,
                                replay,
                                this
                        ).openMenu(p);
                    }
            ));

            slot++;
        }

        buttons.put(48, new Button(
                makeItem(
                        Material.BARRIER,
                        0,
                        "§c§lBack",
                        new ArrayList<String>()
                ),
                (p, click) -> p.closeInventory()
        ));

        buttons.put(49, new Button(
                makeItem(
                        Material.PAPER,
                        0,
                        "§fPage " + pageNumber + "/" + pages,
                        new ArrayList<String>()
                ),
                (p, click) -> {
                }
        ));

        buttons.put(50, new Button(
                makeItem(
                        Material.ARROW,
                        0,
                        "§a§lNext Page",
                        new ArrayList<String>()
                ),
                (p, click) -> {
                    if (pageNumber >= pages) {
                        return;
                    }

                    new ReplayMenu(
                            plugin,
                            replayManager,
                            yourReplays,
                            pageNumber + 1,
                            searchResults
                    ).openMenu(p);
                }
        ));

        return buttons;
    }

    private List<ReplayMetadata> getReplays(Player player) {
        if (searchResults != null) {
            return searchResults;
        }

        List<ReplayMetadata> available = replayManager.getAvailableReplays();

        if (!yourReplays) {
            return available;
        }

        List<ReplayMetadata> ownReplays = new ArrayList<>();
        String playerName = player.getName();

        for (ReplayMetadata replay : available) {
            for (String name : replay.getPlayers()) {
                if (name.equalsIgnoreCase(playerName)) {
                    ownReplays.add(replay);
                    break;
                }
            }
        }

        return ownReplays;
    }

    private ItemStack getReplayItem(ReplayMetadata replay) {
        List<String> lore = new ArrayList<>();

        lore.add("§7Players: §f" + replay.getPlayers().size());
        lore.add("§7Duration: §f" + formatDuration(replay.getDurationMillis()));
        lore.add("§7Kit: §f" + replay.getKit());
        lore.add("§7Arena: §f" + replay.getArena());
        lore.add("§7Date: §f" + new SimpleDateFormat("dd/MM/yyyy HH:mm")
                .format(Date.from(replay.getDate())));
        lore.add("");
        lore.add("§eClick to view replay");

        return makeItem(
                Material.BOOK,
                0,
                "§e§lReplay",
                lore
        );
    }

    private ItemStack makeItem(Material material, int data, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1, (short) data);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    static ItemStack simpleItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    public static class ReplayDetailsMenu extends Menu {

        private final AtlasPracticePlugin plugin;
        private final ReplayManager replayManager;
        private final ReplayMetadata replay;
        private final ReplayMenu previousMenu;

        public ReplayDetailsMenu(AtlasPracticePlugin plugin, ReplayManager replayManager,
                                 ReplayMetadata replay, ReplayMenu previousMenu) {
            super("§8Replay", 9);

            this.plugin = plugin;
            this.replayManager = replayManager;
            this.replay = replay;
            this.previousMenu = previousMenu;
        }

        @Override
        public Map<Integer, Button> getButtons(Player player) {
            Map<Integer, Button> buttons = new HashMap<>();

            ItemStack glass = simpleItem(
                    Material.STAINED_GLASS_PANE,
                    " ",
                    new ArrayList<String>()
            );

            for (int i = 0; i < 9; i++) {
                buttons.put(i, new Button(glass, (p, click) -> {
                }));
            }

            buttons.put(1, new Button(
                    simpleItem(
                            Material.BARRIER,
                            "§c§lBack",
                            new ArrayList<String>()
                    ),
                    (p, click) -> previousMenu.openMenu(p)
            ));

            buttons.put(3, new Button(
                    simpleItem(
                            Material.EYE_OF_ENDER,
                            "§a§lView Replay",
                            Arrays.asList("§7Click to watch the match.")
                    ),
                    (p, click) -> {
                        p.closeInventory();
                        replayManager.playReplay(p, replay.getReplayId());
                    }
            ));

            buttons.put(4, new Button(
                    simpleItem(
                            Material.WATCH,
                            "§e§lDuration",
                            Arrays.asList(
                                    "§f" + ReplayMenu.formatDuration(
                                            replay.getDurationMillis()
                                    )
                            )
                    ),
                    (p, click) -> {
                    }
            ));

            List<String> players = new ArrayList<>();

            for (String name : replay.getPlayers()) {
                players.add("§f" + name);
            }

            buttons.put(5, new Button(
                    simpleItem(
                            Material.SKULL_ITEM,
                            "§b§lPlayers",
                            players
                    ),
                    (p, click) -> {
                    }
            ));

            return buttons;
        }
    }
}