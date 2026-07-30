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

package com.ansh.atlaspractice.cosmetics;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.cosmetics.message.SimpleKillMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public final class CosmeticRegistry {

    private static final Map<CosmeticType, List<Cosmetic>> COSMETICS = new EnumMap<>(CosmeticType.class);
    private static final Map<String, Cosmetic> BY_ID = new HashMap<>();

    static {
        for (CosmeticType type : CosmeticType.values()) {
            COSMETICS.put(type, new ArrayList<Cosmetic>());
        }
    }

    private CosmeticRegistry() {
    }

    public static void register(Cosmetic cosmetic) {
        if (cosmetic == null) {
            return;
        }

        String key = key(cosmetic.getType(), cosmetic.getId());

        if (BY_ID.containsKey(key)) {
            return;
        }

        COSMETICS.get(cosmetic.getType()).add(cosmetic);
        BY_ID.put(key, cosmetic);
    }

    public static Cosmetic getById(CosmeticType type, String id) {
        return id == null ? null : BY_ID.get(key(type, id));
    }

    public static Cosmetic getById(String id) {
        if (id == null) {
            return null;
        }

        for (Cosmetic cosmetic : BY_ID.values()) {
            if (cosmetic.getId().equalsIgnoreCase(id)) {
                return cosmetic;
            }
        }

        return null;
    }

    public static void clear() {
        BY_ID.clear();

        for (List<Cosmetic> cosmetics : COSMETICS.values()) {
            cosmetics.clear();
        }
    }

    private static String key(CosmeticType type, String id) {
        return type.name() + ":" + (id == null ? "" : id.toLowerCase());
    }

    public static List<Cosmetic> getCosmetics(CosmeticType type) {
        return Collections.unmodifiableList(COSMETICS.get(type));
    }

    public static Collection<Cosmetic> getAllCosmetics() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static int getPrice(Cosmetic cosmetic) {
        if (isDefault(cosmetic)) {
            return 0;
        }

        if (cosmetic instanceof SimpleKillMessage) {
            return ((SimpleKillMessage) cosmetic).getPrice();
        }

        YamlConfiguration cfg = shop();

        return cfg.getInt(
                "cosmetics." + cosmetic.getType().name() + "." + cosmetic.getId() + ".price",
                cfg.getInt("default-price", 500)
        );
    }

    public static CosmeticRarity getRarity(Cosmetic cosmetic) {
        if (isDefault(cosmetic)) {
            return CosmeticRarity.COMMON;
        }

        if (cosmetic instanceof SimpleKillMessage) {
            return ((SimpleKillMessage) cosmetic).getRarity();
        }

        String value = shop().getString(
                "cosmetics." + cosmetic.getType().name() + "." + cosmetic.getId() + ".rarity",
                shop().getString("default-rarity", "COMMON")
        );

        try {
            return CosmeticRarity.valueOf(value.toUpperCase());
        } catch (Exception ignored) {
            return CosmeticRarity.COMMON;
        }
    }

    public static boolean isDefault(Cosmetic cosmetic) {
        if (cosmetic == null) {
            return false;
        }

        String id = cosmetic.getId();

        return (cosmetic.getType() == CosmeticType.KILL_EFFECT && id.equalsIgnoreCase("none"))
                || (cosmetic.getType() == CosmeticType.VICTORY_EFFECT && id.equalsIgnoreCase("none"))
                || (cosmetic.getType() == CosmeticType.AURA && id.equalsIgnoreCase("none"))
                || (cosmetic.getType() == CosmeticType.WALKING_TRAIL && id.equalsIgnoreCase("none"))
                || (cosmetic.getType() == CosmeticType.PROJECTILE_TRAIL && id.equalsIgnoreCase("none"))
                || (cosmetic.getType() == CosmeticType.CHAT_COLOR && id.equalsIgnoreCase("white"))
                || (cosmetic.getType() == CosmeticType.KILL_MESSAGE && id.equalsIgnoreCase("default"));
    }

    private static YamlConfiguration shop() {
        AtlasPracticePlugin plugin = AtlasPracticePlugin.getInstance();
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "shop.yml"));
    }
}