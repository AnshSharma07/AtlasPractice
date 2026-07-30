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
// UNDER DEVELOPEMENT, disabled rn
package com.ansh.atlaspractice.bots;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.bots.npc.CitizensBotNPC;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BotManager â€” unchanged public API.
 *
 * <p>Added: initialises the anonymous Citizens NPC registry on construction,
 * and destroys it on {@link #despawnAll()} so the NPCs never leak into
 * Citizens' saves.yml.</p>
 */
public final class BotManager {

    private final AtlasPracticePlugin plugin;

    private final Map<UUID, PracticeBot>      activeBots        = new ConcurrentHashMap<>();
    private final Map<UUID, BotAIController>  activeControllers = new ConcurrentHashMap<>();

    public BotManager(AtlasPracticePlugin plugin) {
        this.plugin = plugin;

        // Initialise the anonymous NPC registry used for all bots.
        // Citizens must already be enabled at this point (plugin.yml depend).
        CitizensBotNPC.initRegistry();

        reloadConfig();
    }


    public void reloadConfig() {
        FileConfiguration config = plugin.getConfig();
        for (BotDifficulty difficulty : BotDifficulty.values()) {
            difficulty.loadFromConfig(config);
        }
    }


    public PracticeBot createBot(
            Player owner,
            UUID botUuid,
            Location spawnLocation,
            BotDifficulty difficulty
    ) {
        removeBot(owner.getUniqueId());

        FileConfiguration config = plugin.getConfig();
        String botName       = config.getString("bot.name",           "AtlasBot");
        String skinTexture   = config.getString("bot.skin",           "");
        String skinSignature = config.getString("bot.skin-signature", "");
        double eatThreshold  = config.getDouble("bot.eat-health",     10.0D);

        PracticeBot bot = new PracticeBot(
                botUuid,
                botName,
                spawnLocation,
                difficulty,
                owner,
                skinTexture,
                skinSignature,
                eatThreshold
        );

        BotAIController controller = new BotAIController(bot);

        bot.spawn();
        controller.start();

        activeBots.put(owner.getUniqueId(), bot);
        activeControllers.put(owner.getUniqueId(), controller);

        return bot;
    }

    public void removeBot(Player player) {
        if (player != null) removeBot(player.getUniqueId());
    }

    public void removeBot(UUID ownerUuid) {
        BotAIController controller = activeControllers.remove(ownerUuid);
        if (controller != null) controller.stop();

        PracticeBot bot = activeBots.remove(ownerUuid);
        if (bot != null) bot.despawn();
    }

    public void despawnAll() {
        for (BotAIController controller : activeControllers.values()) {
            controller.stop();
        }
        activeControllers.clear();

        for (PracticeBot bot : activeBots.values()) {
            bot.despawn();
        }
        activeBots.clear();

        CitizensBotNPC.destroyRegistry();
    }

    public PracticeBot getBot(Player player) {
        return player == null ? null : activeBots.get(player.getUniqueId());
    }

    public PracticeBot getBot(UUID ownerUuid) {
        return activeBots.get(ownerUuid);
    }

    public PracticeBot getBotByUuid(UUID botUuid) {
        for (PracticeBot bot : activeBots.values()) {
            if (bot.getUniqueId().equals(botUuid)) return bot;
        }
        return null;
    }

    public boolean isBot(UUID uuid) {
        return getBotByUuid(uuid) != null;
    }

    public boolean hasBot(Player player) {
        return player != null && activeBots.containsKey(player.getUniqueId());
    }

    public int getActiveBotCount() {
        return activeBots.size();
    }

    public Map<UUID, PracticeBot> getActiveBots() {
        return activeBots;
    }
}
