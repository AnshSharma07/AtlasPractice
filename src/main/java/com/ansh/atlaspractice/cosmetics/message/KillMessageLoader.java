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

package com.ansh.atlaspractice.cosmetics.message;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.cosmetics.CosmeticRarity;
import com.ansh.atlaspractice.cosmetics.CosmeticRegistry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.EnumMap;
import java.util.List;

public final class KillMessageLoader {

    private KillMessageLoader() {
    }

    public static void load() {
        AtlasPracticePlugin plugin = AtlasPracticePlugin.getInstance();

        File file = new File(plugin.getDataFolder(), "kill-messages.yml");

        if (!file.exists()) {
            CosmeticRegistry.register(DefaultKillMessage.INSTANCE);
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection root = config.getConfigurationSection("kill-message");

        if (root == null) {
            CosmeticRegistry.register(DefaultKillMessage.INSTANCE);
            return;
        }

        boolean defaultRegistered = false;

        for (String id : root.getKeys(false)) {

            ConfigurationSection section = root.getConfigurationSection(id);

            if (section == null) {
                continue;
            }

            String displayName = section.getString("display-name", capitalize(id));
            String permission = section.getString("permission", "");
            int price = section.getInt("price", 0);

            CosmeticRarity rarity;
            try {
                rarity = CosmeticRarity.valueOf(
                        section.getString("rarity", "COMMON").toUpperCase()
                );
            } catch (Exception ignored) {
                rarity = CosmeticRarity.COMMON;
            }

            String item = section.getString("item", "PAPER");

            Material material;
            try {
                material = Material.valueOf(item.split(":")[0].toUpperCase());
            } catch (Exception ignored) {
                material = Material.PAPER;
            }

            EnumMap<KillMessageType, List<String>> messages =
                    new EnumMap<>(KillMessageType.class);

            load(messages, section, KillMessageType.PVP, "PvP-Kill");
            load(messages, section, KillMessageType.VOID, "Void-Kill");
            load(messages, section, KillMessageType.EXPLOSION, "Explosion-Kill");
            load(messages, section, KillMessageType.SHOOT, "Shoot-Kill");

            CosmeticRegistry.register(
                    new SimpleKillMessage(
                            id,
                            displayName,
                            material,
                            permission,
                            price,
                            rarity,
                            messages
                    )
            );

            if (id.equalsIgnoreCase("default")) {
                defaultRegistered = true;
            }
        }

        if (!defaultRegistered) {
            CosmeticRegistry.register(DefaultKillMessage.INSTANCE);
        }
    }

    private static void load(EnumMap<KillMessageType, List<String>> map,
                             ConfigurationSection section,
                             KillMessageType type,
                             String path) {

        List<String> list = section.getStringList(path);

        if (!list.isEmpty()) {
            map.put(type, list);
        }
    }

    private static String capitalize(String id) {
        String[] split = id.replace('_', ' ').split(" ");

        StringBuilder builder = new StringBuilder();

        for (String s : split) {
            if (s.isEmpty()) {
                continue;
            }

            builder.append(Character.toUpperCase(s.charAt(0)))
                    .append(s.substring(1))
                    .append(' ');
        }

        return builder.toString().trim();
    }
}