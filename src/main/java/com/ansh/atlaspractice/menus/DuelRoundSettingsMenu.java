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
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DuelRoundSettingsMenu extends Menu {

    private final AtlasPracticePlugin plugin;
    private final UUID targetUuid;
    private final Kit chosenKit;

    public DuelRoundSettingsMenu(AtlasPracticePlugin plugin, UUID targetUuid, Kit chosenKit) {
        super("Select Match Format", 9);
        this.plugin = plugin;
        this.targetUuid = targetUuid;
        this.chosenKit = chosenKit;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(2, createRoundButton(
                Material.IRON_SWORD,
                "§b§lBest of 1",
                "§7Standard single-round match layout.",
                1
        ));

        buttons.put(4, createRoundButton(
                Material.DIAMOND_SWORD,
                "§b§lBest of 3",
                "§7First player to win 2 rounds wins.",
                3
        ));

        buttons.put(6, createRoundButton(
                Material.NETHER_STAR,
                "§b§lBest of 5",
                "§7First player to win 3 rounds wins.",
                5
        ));

        return buttons;
    }

    private Button createRoundButton(
            Material material,
            String name,
            String description,
            int rounds
    ) {
        return new Button(
                createFormatIcon(material, name, description),
                (player, click) -> {
                    Player target = Bukkit.getPlayer(targetUuid);

                    if (target == null) {
                        player.closeInventory();
                        player.sendMessage("§cThat player is no longer online.");
                        return;
                    }

                    Profile profile = plugin.getProfileManager().getProfile(player.getUniqueId());

                    if (profile != null && profile.getMapSelectionPreference().allowsDuel()) {
                        new MapSelectionMenu(
                                plugin,
                                chosenKit,
                                targetUuid,
                                rounds
                        ).openMenu(player);
                        return;
                    }

                    player.closeInventory();
                    plugin.getDuelManager().registerChallenge(
                            player,
                            target,
                            chosenKit,
                            rounds
                    );
                }
        );
    }

    private ItemStack createFormatIcon(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(description));
            item.setItemMeta(meta);
        }

        return item;
    }
}