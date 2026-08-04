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

package com.ansh.atlaspractice.arena;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.kit.Kit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages per-mode shared arena pools.
 *
 * <p>Every enabled shared mode (BOXING, NODEBUFF, COMBO, …) owns an
 * independent {@link SharedArenaGroup}. Assigning an arena to Boxing never
 * automatically assigns it to Nodebuff, and vice versa.</p>
 *
 * <h3>Storage format (shared-arenas.yml)</h3>
 * <pre>
 * groups:
 *   boxing:
 *     display-name: Boxing
 *     arenas:
 *       - arena1
 *       - arena3
 *   nodebuff:
 *     display-name: Nodebuff
 *     arenas:
 *       - arena2
 * </pre>
 *
 * <h3>Legacy migration</h3>
 * If the file contains the old single {@code groups.shared} key, every arena
 * in that pool is copied into every currently enabled mode automatically.
 * The migration runs once and the file is saved in the new format.
 */
public final class SharedArenaService {

    /** Mode IDs that are recognised by the Shared-Arena system. */
    private static final List<String> DEFAULT_MODES = Arrays.asList(
            "NODEBUFF",
            "BOXING",
            "BUILDUHC",
            "COMBO",
            "GAPPLE",
            "ARCHER",
            "CLASSIC",
            "IRON",
            "DIAMOND",
            "HCF",
            "FINALUHC",
            "DEBUFF",
            "SOUP",
            "AXE"
    );

    /** Human-readable names used for new groups and in the GUI. */
    private static final Map<String, String> MODE_DISPLAY_NAMES;
    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("nodebuff",  "Nodebuff");
        m.put("boxing",    "Boxing");
        m.put("builduhc",  "Build UHC");
        m.put("combo",     "Combo");
        m.put("gapple",    "Gapple");
        m.put("archer",    "Archer");
        m.put("classic",   "Classic");
        m.put("iron",      "Iron");
        m.put("diamond",   "Diamond");
        m.put("hcf",       "HCF");
        m.put("finaluhc",  "Final UHC");
        m.put("debuff",    "Debuff");
        m.put("soup",      "Soup");
        m.put("axe",       "Axe");
        MODE_DISPLAY_NAMES = Collections.unmodifiableMap(m);
    }

    private final AtlasPracticePlugin plugin;

    /** One group per enabled mode, keyed by lowercase mode ID. */
    private final Map<String, SharedArenaGroup> groups = new LinkedHashMap<>();

    /** Lowercase mode IDs that are currently enabled in config. */
    private final Set<String> enabledModes = new LinkedHashSet<>();

    private File storageFile;
    private boolean enabled;

    public SharedArenaService(AtlasPracticePlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    public void load() {
        addConfigDefaults();

        this.enabled = plugin.getConfig().getBoolean("Shared-Arena.Enabled", true);
        this.enabledModes.clear();
        this.groups.clear();

        for (String mode : DEFAULT_MODES) {
            if (plugin.getConfig().getBoolean("Shared-Arena.Modes." + mode, true)) {
                String key = mode.toLowerCase();
                enabledModes.add(key);
                String displayName = MODE_DISPLAY_NAMES.getOrDefault(key, mode);
                groups.put(key, new SharedArenaGroup(key, displayName));
            }
        }

        this.storageFile = new File(plugin.getDataFolder(), "shared-arenas.yml");
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(storageFile);

        if (isLegacyFormat(storage)) {
            migrateLegacyFormat(storage);
        } else {
            loadNewFormat(storage);
        }

        save();
    }

    private void addConfigDefaults() {
        plugin.getConfig().addDefault("Shared-Arena.Enabled", true);
        for (String mode : DEFAULT_MODES) {
            plugin.getConfig().addDefault("Shared-Arena.Modes." + mode, true);
        }
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    // -----------------------------------------------------------------------
    // Legacy migration
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} when the YAML file contains the legacy single
     * {@code groups.shared} pool and does NOT already have any per-mode group.
     */
    private boolean isLegacyFormat(YamlConfiguration storage) {
        ConfigurationSection gs = storage.getConfigurationSection("groups");
        if (gs == null) {
            return false;
        }
        // New format: has keys that match known modes (not "shared")
        for (String key : gs.getKeys(false)) {
            if (key.equalsIgnoreCase("shared")) {
                return true;
            }
        }
        return false;
    }

    private void migrateLegacyFormat(YamlConfiguration storage) {
        List<String> legacyArenas = storage.getStringList("groups.shared.arenas");

        plugin.getLogger().info("[SharedArena] Detected legacy format — migrating to per-mode storage.");
        plugin.getLogger().info("[SharedArena] Legacy pool contained " + legacyArenas.size() + " arena(s): "
                + String.join(", ", legacyArenas));

        for (SharedArenaGroup group : groups.values()) {
            group.setArenaIds(legacyArenas);
        }

        plugin.getLogger().info("[SharedArena] Migration complete. Each enabled mode now has its own copy of the legacy pool.");
        // save() will be called by load() immediately after.
    }

    private void loadNewFormat(YamlConfiguration storage) {
        ConfigurationSection groupsSection = storage.getConfigurationSection("groups");
        if (groupsSection == null) {
            return;
        }
        for (String modeKey : groupsSection.getKeys(false)) {
            SharedArenaGroup group = groups.get(modeKey.toLowerCase());
            if (group == null) {
                // Mode exists in file but is disabled in config — skip.
                continue;
            }
            List<String> arenas = groupsSection.getStringList(modeKey + ".arenas");
            group.setArenaIds(arenas);
        }
    }

    // -----------------------------------------------------------------------
    // Persistence
    // -----------------------------------------------------------------------

    public void save() {
        if (storageFile == null) {
            return;
        }
        YamlConfiguration storage = new YamlConfiguration();
        for (SharedArenaGroup group : groups.values()) {
            String path = "groups." + group.getId();
            storage.set(path + ".display-name", group.getDisplayName());
            storage.set(path + ".arenas", group.getArenaIds());
        }
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "[SharedArena] Failed to save shared-arenas.yml.", e);
        }
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public boolean isEnabled() {
        return enabled;
    }

    public boolean usesSharedArenas(Kit kit) {
        return enabled && kit != null && enabledModes.contains(kit.getId().toLowerCase());
    }

    // --- Mode-keyed operations ---

    /**
     * Assigns {@code arenaId} to the shared pool for {@code mode}.
     *
     * @param mode    lowercase or mixed-case mode ID (e.g. "boxing")
     * @param arenaId arena identifier
     * @return {@code true} if the arena was newly added; {@code false} if it was already present
     */
    public boolean assignArena(String mode, String arenaId) {
        SharedArenaGroup group = groups.get(mode.toLowerCase());
        if (group == null) {
            return false;
        }
        boolean added = group.addArena(arenaId);
        save();
        return added;
    }

    /**
     * Removes {@code arenaId} from the shared pool for {@code mode}.
     *
     * @return {@code true} if the arena was present and removed
     */
    public boolean removeArena(String mode, String arenaId) {
        SharedArenaGroup group = groups.get(mode.toLowerCase());
        if (group == null) {
            return false;
        }
        boolean removed = group.removeArena(arenaId);
        if (removed) {
            save();
        }
        return removed;
    }

    /**
     * Removes {@code arenaId} from every mode's pool.
     * Called when an arena is deleted entirely.
     */
    public void removeArenaFromAllModes(String arenaId) {
        boolean anyRemoved = false;
        for (SharedArenaGroup group : groups.values()) {
            if (group.removeArena(arenaId)) {
                anyRemoved = true;
            }
        }
        if (anyRemoved) {
            save();
        }
    }

    /**
     * Returns {@code true} when {@code arenaId} is in the pool for {@code mode}.
     */
    public boolean isAssigned(String mode, String arenaId) {
        SharedArenaGroup group = groups.get(mode.toLowerCase());
        return group != null && group.getArenaIds().stream().anyMatch(id -> id.equalsIgnoreCase(arenaId));
    }

    // --- Kit-based overloads (convenience wrappers) ---

    public boolean assignArena(Kit kit, String arenaId) {
        return kit != null && assignArena(kit.getId(), arenaId);
    }

    public boolean removeArena(Kit kit, String arenaId) {
        return kit != null && removeArena(kit.getId(), arenaId);
    }

    public boolean isAssigned(Kit kit, String arenaId) {
        return kit != null && isAssigned(kit.getId(), arenaId);
    }

    // --- Getters ---

    /**
     * Returns the arena IDs assigned to the shared pool for the given mode.
     * Returns an empty list if the mode is not found or not enabled.
     */
    public List<String> getArenaIds(String mode) {
        SharedArenaGroup group = groups.get(mode.toLowerCase());
        return group == null ? Collections.emptyList() : group.getArenaIds();
    }

    /**
     * Returns the arena IDs for the kit's shared pool, or the kit's own arena
     * IDs if this kit does not use shared arenas.
     */
    public List<String> getArenaIds(Kit kit) {
        if (usesSharedArenas(kit)) {
            return getArenaIds(kit.getId());
        }
        return kit == null ? Collections.emptyList() : kit.getArenaIds();
    }

    /**
     * Returns the {@link SharedArenaGroup} for the given mode, or {@code null}
     * if the mode is unknown / disabled.
     */
    public SharedArenaGroup getGroup(String mode) {
        return groups.get(mode.toLowerCase());
    }

    /**
     * Returns the {@link SharedArenaGroup} for the given kit's mode.
     * Returns {@code null} if the kit does not use shared arenas.
     */
    public SharedArenaGroup getGroup(Kit kit) {
        return kit == null ? null : getGroup(kit.getId());
    }

    /**
     * Returns an immutable view of all per-mode groups.
     */
    public Map<String, SharedArenaGroup> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    /**
     * Returns the set of currently enabled mode IDs (lowercase).
     */
    public Collection<String> getEnabledModes() {
        return new ArrayList<>(enabledModes);
    }

    /**
     * Returns the {@link Arena} objects that belong to the shared pool for
     * {@code kit}'s mode and are currently registered in the arena manager.
     */
    public List<Arena> getAvailableArenaPool(Kit kit) {
        return getArenaIds(kit).stream()
                .map(id -> plugin.getArenaManager().getArena(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
