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
import com.ansh.atlaspractice.arena.ArenaState;
import com.ansh.atlaspractice.arena.RuntimeArena;
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MapSelectionMenu extends Menu {

    private static final int[] MAP_SLOTS = {
            1, 2, 3, 5, 6, 7,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24
    };

    private static final int PREVIOUS_SLOT = 0;
    private static final int PAGE_SLOT = 4;
    private static final int NEXT_SLOT = 8;
    private static final int BACK_SLOT = 26;

    private final AtlasPracticePlugin plugin;
    private final Kit kit;

    private final boolean duel;
    private final UUID duelTarget;
    private final int duelRounds;
    private final boolean ranked;
    private final int page;

    public MapSelectionMenu(AtlasPracticePlugin plugin, Kit kit, UUID duelTarget, int duelRounds) {
        this(plugin, kit, true, duelTarget, duelRounds, false, 1);
    }

    public MapSelectionMenu(AtlasPracticePlugin plugin, Kit kit, boolean ranked, int page) {
        this(plugin, kit, false, null, 0, ranked, page);
    }

    private MapSelectionMenu(
            AtlasPracticePlugin plugin,
            Kit kit,
            boolean duel,
            UUID duelTarget,
            int duelRounds,
            boolean ranked,
            int page
    ) {
        super("§8Select Map §7• §f" + kit.getDisplayName(), 27);

        this.plugin = plugin;
        this.kit = kit;
        this.duel = duel;
        this.duelTarget = duelTarget;
        this.duelRounds = duelRounds;
        this.ranked = ranked;
        this.page = Math.max(1, page);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        ItemStack filler = createItem(
                Material.STAINED_GLASS_PANE,
                " ",
                Collections.emptyList()
        );

        for (int slot = 0; slot < getSize(); slot++) {
            buttons.put(slot, new Button(filler, (p, click) -> {}));
        }

        List<Arena> arenas = new ArrayList<>();

        for (Arena arena : plugin.getSharedArenaService().getAvailableArenaPool(kit)) {
            if (arena instanceof RuntimeArena || arena.getState() == ArenaState.DISABLED) {
                continue;
            }

            arenas.add(arena);
        }

        arenas.sort(Comparator
                .comparing(Arena::getDisplayName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Arena::getId, String.CASE_INSENSITIVE_ORDER));

        int maxPages = Math.max(1, (arenas.size() + MAP_SLOTS.length - 1) / MAP_SLOTS.length);
        int currentPage = Math.min(page, maxPages);

        int start = (currentPage - 1) * MAP_SLOTS.length;
        int end = Math.min(start + MAP_SLOTS.length, arenas.size());

        boolean runtimeOverflow = plugin.getConfig().getBoolean("runtime-overflow.enabled", false);
        int slotIndex = 0;

        for (int i = start; i < end; i++) {
            Arena arena = arenas.get(i);
            boolean free = arena.isAvailable();
            boolean selectable = free || runtimeOverflow;

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Kit: " + ChatColor.WHITE + kit.getDisplayName());
            lore.add("");

            if (free) {
                lore.add(ChatColor.GREEN + "● Available");
            } else if (runtimeOverflow) {
                lore.add(ChatColor.YELLOW + "● Busy");
                lore.add(ChatColor.GRAY + "Runtime arena can be created.");
            } else {
                lore.add(ChatColor.RED + "● Currently occupied");
            }

            lore.add("");
            lore.add(selectable
                    ? ChatColor.YELLOW + "Click to select."
                    : ChatColor.RED + "Currently unavailable.");

            ItemStack icon = createItem(
                    Material.PAPER,
                    ChatColor.AQUA + arena.getDisplayName(),
                    lore
            );

            int slot = MAP_SLOTS[slotIndex++];

            if (selectable) {
                buttons.put(slot, new Button(
                        icon,
                        (p, click) -> selectArena(p, arena.getId())
                ));
            } else {
                buttons.put(slot, new Button(icon, (p, click) ->
                        p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1.0F, 1.0F)
                ));
            }
        }

        if (currentPage > 1) {
            buttons.put(PREVIOUS_SLOT, new Button(
                    createItem(
                            Material.ARROW,
                            ChatColor.GREEN + "Previous Page",
                            Collections.singletonList(ChatColor.GRAY + "Go back one page.")
                    ),
                    (p, click) -> new MapSelectionMenu(
                            plugin,
                            kit,
                            duel,
                            duelTarget,
                            duelRounds,
                            ranked,
                            currentPage - 1
                    ).openMenu(p)
            ));
        }

        if (currentPage < maxPages) {
            buttons.put(NEXT_SLOT, new Button(
                    createItem(
                            Material.ARROW,
                            ChatColor.GREEN + "Next Page",
                            Collections.singletonList(ChatColor.GRAY + "View more maps.")
                    ),
                    (p, click) -> new MapSelectionMenu(
                            plugin,
                            kit,
                            duel,
                            duelTarget,
                            duelRounds,
                            ranked,
                            currentPage + 1
                    ).openMenu(p)
            ));
        }

        buttons.put(PAGE_SLOT, new Button(
                createItem(
                        Material.PAPER,
                        ChatColor.YELLOW + "Page " + currentPage + "/" + maxPages,
                        Collections.singletonList(ChatColor.GRAY + String.valueOf(arenas.size()) + " map(s) available.")
                ),
                (p, click) -> {}
        ));

        buttons.put(BACK_SLOT, new Button(
                createItem(
                        Material.BARRIER,
                        ChatColor.RED + "Back",
                        Collections.singletonList(ChatColor.GRAY + "Return to the previous menu.")
                ),
                (p, click) -> openPreviousMenu(p)
        ));

        return buttons;
    }

    private void selectArena(Player player, String arenaId) {
        player.closeInventory();

        if (duel) {
            Player target = Bukkit.getPlayer(duelTarget);

            if (target == null) {
                player.sendMessage("§cThat player is no longer online.");
                return;
            }

            plugin.getDuelManager().registerChallenge(
                    player,
                    target,
                    kit,
                    duelRounds,
                    arenaId
            );
            return;
        }

        if (ranked) {
            plugin.getQueueManager().joinRankedQueue(player, kit, arenaId);
        } else {
            plugin.getQueueManager().joinUnrankedQueue(player, kit, arenaId);
        }
    }

    private void openPreviousMenu(Player player) {
        if (duel) {
            new DuelRoundSettingsMenu(plugin, duelTarget, kit).openMenu(player);
            return;
        }

        if (ranked) {
            new RankedMenu(plugin).openMenu(player);
        } else {
            new UnrankedMenu(plugin, null).openMenu(player);
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}