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

import com.ansh.atlaspractice.bots.ai.CombatController;
import com.ansh.atlaspractice.bots.ai.EatingController;
import com.ansh.atlaspractice.bots.ai.MovementController;
import com.ansh.atlaspractice.bots.ai.RotationController;
import com.ansh.atlaspractice.bots.ai.TargetSelector;
import com.ansh.atlaspractice.bots.npc.CitizensBotNPC;
import com.ansh.atlaspractice.match.Match;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
public final class PracticeBot {

    private final BotDifficulty difficulty;
    private final Player targetPlayer;

    private final CitizensBotNPC botNPC;

    private boolean frozen;
    private final TargetSelector targetSelector;
    private final RotationController rotationController;
    private final MovementController movementController;
    private final CombatController combatController;
    private final EatingController eatingController;

    private BotState state = BotState.IDLE;
    private int equipmentRefreshTicks;

    private Match match;

    public PracticeBot(
            UUID uuid,
            String name,
            Location spawnLocation,
            BotDifficulty difficulty,
            Player targetPlayer,
            String skinTexture,
            String skinSignature,
            double eatThreshold
    ) {
        this.difficulty   = difficulty;
        this.targetPlayer = targetPlayer;

        this.botNPC = new CitizensBotNPC(
                uuid,
                name,
                spawnLocation,
                skinTexture,
                skinSignature
        );

        this.targetSelector    = new TargetSelector(targetPlayer);
        this.rotationController = new RotationController(botNPC, difficulty);
        this.movementController = new MovementController(botNPC, difficulty);

        this.combatController = new CombatController(
                this,
                botNPC,
                difficulty,
                movementController
        );

        this.eatingController = new EatingController(
                botNPC,
                eatThreshold,
                difficulty
        );
    }

    public void spawn() {
        if (state != BotState.IDLE) return;

        state = BotState.SPAWNING;
        botNPC.spawn();
        equipmentRefreshTicks = 0;
        state = BotState.COUNTDOWN;
    }

    public void startFighting() {
        if (!state.isAlive()) return;
        state = BotState.SEARCHING;
    }

    public void despawn() {
        if (state == BotState.DESPAWN) return;

        state = BotState.DESPAWN;

        movementController.reset();
        eatingController.reset();
        combatController.reset();

        botNPC.despawn();
    }

    public void tick() {
        if (frozen) {

            botNPC.tick();
            botNPC.refreshViewers();
            return;
        }

        if (!state.isAlive()) return;

        Player botPlayer = botNPC.getBukkitEntity();

        if (botPlayer == null) {
            state = BotState.DEAD;
            return;
        }

        if (botNPC.isDead() || botNPC.getHealth() <= 0.0F) {
            state = BotState.DEAD;
            return;
        }

        Player target = targetSelector.acquireTarget();

        if (target == null) {
            state = BotState.SEARCHING;
            movementController.stop();
            botNPC.refreshViewers();
            botNPC.tick();
            return;
        }

        eatingController.updateEating();

        boolean eating = eatingController.isEating();
        if (!eating) {
            selectKitAwareHotbarItem(target);
        }

        rotationController.facePlayer(target);
        movementController.update(target);

        if (!eating) {
            combatController.updateCombat(target);
        }

        botNPC.tick();

        if (++equipmentRefreshTicks >= 20) {
            equipmentRefreshTicks = 0;
            botNPC.updateEquipment();
        }

        if (eating) {
            state = BotState.HEALING;
        } else if (combatController.isInCombo()) {
            state = BotState.COMBO;
        } else {
            state = BotState.STRAFING;
        }

        botNPC.refreshViewers();
    }

    private void selectKitAwareHotbarItem(Player target) {
        Player botPlayer = botNPC.getBukkitEntity();
        if (botPlayer == null || match == null || match.getKit() == null) return;

        double distance = botPlayer.getLocation().distance(target.getLocation());
        ItemStack preferred = findPreferredItem(distance);
        if (preferred != null) {
            botNPC.setHeldItem(preferred);
        }
    }

    private ItemStack findPreferredItem(double distance) {
        ItemStack[] contents = botNPC.getBukkitEntity().getInventory().getContents();
        Material wanted;

        if (distance > 6.0D) {
            wanted = firstPresent(contents, Material.BOW, Material.FISHING_ROD);
        } else if (distance > 3.2D) {
            wanted = firstPresent(contents, Material.FISHING_ROD, Material.BOW,
                    Material.SNOW_BALL, Material.EGG);
        } else {
            wanted = firstPresent(contents,
                    Material.DIAMOND_SWORD, Material.IRON_SWORD,
                    Material.STONE_SWORD,   Material.WOOD_SWORD,
                    Material.DIAMOND_AXE,   Material.IRON_AXE,
                    Material.STONE_AXE,     Material.WOOD_AXE,
                    Material.STICK);
        }

        if (wanted == null && match.getKit().isBuildAllowed()) {
            wanted = firstBlock(contents);
        }

        if (wanted == null) {
            wanted = firstPresent(contents,
                    Material.DIAMOND_SWORD, Material.IRON_SWORD,
                    Material.STONE_SWORD,   Material.WOOD_SWORD,
                    Material.STICK);
        }

        if (wanted == null) return null;

        for (ItemStack item : contents) {
            if (item != null && item.getType() == wanted) return item.clone();
        }
        return null;
    }

    private Material firstPresent(ItemStack[] contents, Material... materials) {
        for (Material material : materials) {
            for (ItemStack item : contents) {
                if (item != null && item.getType() == material) return material;
            }
        }
        return null;
    }

    private Material firstBlock(ItemStack[] contents) {
        for (ItemStack item : contents) {
            if (item != null && item.getType().isBlock()) return item.getType();
        }
        return null;
    }
    public void updateEquipment() {
        botNPC.updateEquipment();
    }
    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public CitizensBotNPC getBotNPC() {
        return botNPC;
    }

    public Player getBukkitEntity() {
        return botNPC.getBukkitEntity();
    }

    public UUID getUniqueId() {
        return botNPC.getUuid();
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public BotDifficulty getDifficulty() {
        return difficulty;
    }

    public BotState getState() {
        return state;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }
}
