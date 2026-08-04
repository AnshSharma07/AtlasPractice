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

package com.ansh.atlaspractice.cosmetics.message;

import com.ansh.atlaspractice.cosmetics.CosmeticRarity;
import com.ansh.atlaspractice.cosmetics.CosmeticType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class SimpleKillMessage implements KillMessage {

    private static final Random RANDOM = new Random();

    private final String id;
    private final String displayName;
    private final String permission;
    private final Material icon;
    private final int price;
    private final CosmeticRarity rarity;
    private final EnumMap<KillMessageType, List<String>> messages;

    public SimpleKillMessage(String id,
                             String displayName,
                             Material icon,
                             String permission,
                             int price,
                             CosmeticRarity rarity,
                             EnumMap<KillMessageType, List<String>> messages) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.permission = permission;
        this.price = price;
        this.rarity = rarity;
        this.messages = messages;
    }

    public SimpleKillMessage(String id, String displayName, Material icon, String format) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.permission = "";
        this.price = 0;
        this.rarity = CosmeticRarity.COMMON;
        this.messages = new EnumMap<>(KillMessageType.class);
        this.messages.put(KillMessageType.PVP, Collections.singletonList(format));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public CosmeticType getType() {
        return CosmeticType.KILL_MESSAGE;
    }

    @Override
    public Material getIcon() {
        return icon;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public CosmeticRarity getRarity() {
        return rarity;
    }

    @Override
    public String format(Player killer, Player victim, KillMessageType type) {
        List<String> validMessages = messages.get(type);

        if (validMessages == null || validMessages.isEmpty()) {
            validMessages = messages.get(KillMessageType.PVP);
        }

        if (validMessages == null || validMessages.isEmpty()) {
            return DefaultKillMessage.INSTANCE.format(killer, victim, type);
        }

        String message = validMessages.get(RANDOM.nextInt(validMessages.size()));
        message = message.replace("{killer}", killer.getName())
                .replace("{victim}", victim.getName());

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}