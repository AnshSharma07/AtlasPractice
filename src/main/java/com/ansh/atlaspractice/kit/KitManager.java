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

package com.ansh.atlaspractice.kit;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class KitManager {

    private final AtlasPracticePlugin plugin;

    @Getter
    private final KitRegistry registry = new KitRegistry();

    public KitManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    public void loadKitsFromDisk() {
        File file = new File(plugin.getDataFolder(), "kits.yml");

        if (!file.exists()) {
            plugin.saveResource("kits.yml", false);
        }

        registry.clear();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("kits")) {
            return;
        }
        Set<String> usedArenas = new HashSet<>();
        for (String key : config.getConfigurationSection("kits").getKeys(false)) {

            String path = "kits." + key + ".";

            String displayName =
                    config.getString(path + "display-name", key);

            Kit kit = new Kit(key, displayName);
            kit.setLore(config.getStringList(path + "lore"));
            kit.setKnockbackProfileName(config.getString(path + "kb-profile", "default"));
            kit.setRankedEnabled(config.getBoolean(path + "ranked", true));
            kit.setBuildAllowed(config.getBoolean(path + "build", false));
            kit.setDamageEnabled(config.getBoolean(path + "damage", true));
            kit.setDurabilityEnabled(config.getBoolean(path + "durability", true));
            kit.setBoxingMode(config.getBoolean(path + "boxing", false));
            kit.setHungerLossEnabled(config.getBoolean(path + "hunger-loss", false));
            if (config.contains(path + "icon-material")) {
                try {
                    Material mat = Material.valueOf(config.getString(path + "icon-material"));
                    kit.setIconMaterial(mat);
                    kit.setDisplayMaterial(mat);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid icon material for kit " + key + ". Defaulting to AIR.");
                    kit.setIconMaterial(Material.AIR);
                    kit.setDisplayMaterial(Material.AIR);
                }
            }
            kit.setIconData(config.getInt(path + "icon-data", 0));

            List<String> arenaIds =
                    config.getStringList(path + "arenas");

            for (String arenaId : arenaIds) {

                if (usedArenas.contains(arenaId.toLowerCase())) {

                    plugin.getLogger().warning(
                            "Duplicate arena assignment: " + arenaId
                    );

                    continue;
                }

                usedArenas.add(arenaId.toLowerCase());
                kit.addArena(arenaId);
            }


            if (config.contains(path + "contents.main")) {
                kit.setMainContents(
                        InventoryUtil.deserializeItemStacks(
                                config.getString(path + "contents.main")
                        )
                );
            }

            if (config.contains(path + "contents.armor")) {
                kit.setArmorContents(
                        InventoryUtil.deserializeItemStacks(
                                config.getString(path + "contents.armor")
                        )
                );
            }

            registry.registerKit(kit);
        }

        plugin.getLogger().info(
                "Loaded " + registry.getAllKits().size() + " kits."
        );
    }

    public void saveKitToDisk(Kit kit) {

        File file = new File(plugin.getDataFolder(), "kits.yml");

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);
        String path = "kits." + kit.getId() + ".";
        config.set(path + "display-name", kit.getDisplayName());
        config.set(path + "lore", kit.getLore());
        config.set(path + "kb-profile", kit.getKnockbackProfileName());
        config.set(path + "ranked", kit.isRankedEnabled());
        config.set(path + "build", kit.isBuildAllowed());
        config.set(path + "damage", kit.isDamageEnabled());
        config.set(path + "durability", kit.isDurabilityEnabled());
        config.set(path + "boxing", kit.isBoxingMode());
        config.set(path + "hunger-loss", kit.isHungerLossEnabled());
        config.set(path + "arenas",
                kit.getArenaIds());
        config.set(path + "contents.main",
                InventoryUtil.serializeItemStacks(
                        kit.getMainContents()
                ));
        config.set(path + "contents.armor",
                InventoryUtil.serializeItemStacks(
                        kit.getArmorContents()
                ));
        config.set(path + "icon-material", kit.getIconMaterial() != null ? kit.getIconMaterial().name() : "AIR");
        config.set(path + "icon-data", kit.getIconData()); // Saves potion variant ID

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Failed to save kit " + kit.getId(),
                    exception
            );
        }
    }

    public void deleteKit(String id) {

        File file = new File(plugin.getDataFolder(), "kits.yml");

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        config.set("kits." + id.toLowerCase(), null);

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Failed to delete kit " + id,
                    exception
            );
        }

        registry.clear();
        loadKitsFromDisk();
    }

    public Optional<Kit> getKit(String id) {
        return registry.getKit(id);
    }

    public Collection<Kit> getAllKits() {
        return registry.getAllKits();
    }

    public Map<String, Kit> getKitsMap() {
        return registry.getAllKits()
                .stream()
                .collect(Collectors.toMap(
                        Kit::getId,
                        kit -> kit
                ));
    }

    public boolean exists(String id) {
        return registry.getKit(id).isPresent();
    }

    public void registerKit(Kit kit) {
        registry.registerKit(kit);
    }

    public void reload() {
        loadKitsFromDisk();
    }
}