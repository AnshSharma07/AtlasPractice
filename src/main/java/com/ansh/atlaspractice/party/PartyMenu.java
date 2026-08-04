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

package com.ansh.atlaspractice.party;

import com.ansh.atlaspractice.menus.Button;
import com.ansh.atlaspractice.menus.Menu;
import com.ansh.atlaspractice.menus.PartyFFAKitMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public final class PartyMenu extends Menu {

    private final PartyManager partyManager;

    public PartyMenu(PartyManager partyManager) {
        super("Party Menu", 3 * 9);
        this.partyManager = partyManager;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        ItemStack split = new ItemStack(Material.IRON_SWORD);
        ItemMeta splitMeta = split.getItemMeta();
        splitMeta.setDisplayName("§a§lParty Split");
        split.setItemMeta(splitMeta);

        buttons.put(11, new Button(split, (p, click) -> {
            p.closeInventory();
            partyManager.openSplitMenu(p);
        }));

        ItemStack ffa = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta ffaMeta = ffa.getItemMeta();
        ffaMeta.setDisplayName("§6§lParty FFA");
        ffa.setItemMeta(ffaMeta);

        buttons.put(12, new Button(ffa, (p, click) -> {
            p.closeInventory();
            com.ansh.atlaspractice.profile.Profile leaderProfile =
                    partyManager.getPlugin().getProfileManager().getProfile(p.getUniqueId());
            if (leaderProfile == null || leaderProfile.getPartyId() == null) {
                p.sendMessage("§cYou are not in a party.");
                return;
            }
            Party currentParty = partyManager.getParty(leaderProfile.getPartyId()).orElse(null);
            if (currentParty == null || !currentParty.isLeader(p.getUniqueId())) {
                p.sendMessage("§cOnly the party leader can start Party FFA.");
                return;
            }
            new PartyFFAKitMenu(partyManager.getPlugin()).openMenu(p);
        }));

        ItemStack pvp = new ItemStack(Material.GOLD_SWORD);
        ItemMeta pvpMeta = pvp.getItemMeta();
        pvpMeta.setDisplayName("§b§lParty vs Party");
        pvp.setItemMeta(pvpMeta);

        buttons.put(13, new Button(pvp, (p, click) -> {
            p.closeInventory();
            p.sendMessage("§bUse /party duel <leader> to challenge another party!");
        }));

        ItemStack settings = new ItemStack(Material.REDSTONE);
        ItemMeta settingsMeta = settings.getItemMeta();
        settingsMeta.setDisplayName("§c§lParty Settings");
        settings.setItemMeta(settingsMeta);

        buttons.put(14, new Button(settings, (p, click) -> {
            p.closeInventory();
            p.sendMessage("§cParty settings is not available in community edition.");
        }));

        ItemStack help = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName("§e§lParty Help");
        help.setItemMeta(helpMeta);

        buttons.put(19, new Button(help, (p, click) -> {
            p.closeInventory();
            p.performCommand("party");
        }));

        com.ansh.atlaspractice.profile.Profile profile = partyManager.getPlugin().getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return buttons;

        Party party = partyManager.getParty(profile.getPartyId()).orElse(null);
        if (party != null && party.isLeader(player.getUniqueId())) {
            ItemStack disband = new ItemStack(Material.BARRIER);
            ItemMeta meta = disband.getItemMeta();
            meta.setDisplayName("§c§lDisband Party");
            disband.setItemMeta(meta);

            buttons.put(20, new Button(disband, (p, click) -> {
                p.closeInventory();
                partyManager.disbandParty(p);
            }));
        } else {
            ItemStack leave = new ItemStack(Material.REDSTONE_BLOCK);
            ItemMeta meta = leave.getItemMeta();
            meta.setDisplayName("§c§lLeave Party");
            leave.setItemMeta(meta);

            buttons.put(20, new Button(leave, (p, click) -> {
                p.closeInventory();
                partyManager.leaveParty(p);
            }));
        }

        return buttons;
    }
}