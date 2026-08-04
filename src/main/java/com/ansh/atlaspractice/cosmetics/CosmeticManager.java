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

package com.ansh.atlaspractice.cosmetics;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.cosmetics.aura.AuraEffect;
import com.ansh.atlaspractice.cosmetics.aura.NoneAura;
import com.ansh.atlaspractice.cosmetics.chat.ChatColorEffect;
import com.ansh.atlaspractice.cosmetics.chat.WhiteChatColor;
import com.ansh.atlaspractice.cosmetics.kill.KillEffect;
import com.ansh.atlaspractice.cosmetics.kill.NoneKillEffect;
import com.ansh.atlaspractice.cosmetics.message.DefaultKillMessage;
import com.ansh.atlaspractice.cosmetics.message.KillMessage;
import com.ansh.atlaspractice.cosmetics.message.KillMessageType;
import com.ansh.atlaspractice.cosmetics.projectile.NoneProjectileTrail;
import com.ansh.atlaspractice.cosmetics.projectile.ProjectileTrail;
import com.ansh.atlaspractice.cosmetics.victory.NoneVictoryEffect;
import com.ansh.atlaspractice.cosmetics.victory.VictoryEffect;
import com.ansh.atlaspractice.cosmetics.walking.NoneWalkingTrail;
import com.ansh.atlaspractice.cosmetics.walking.WalkingTrail;
import com.ansh.atlaspractice.profile.Profile;
import org.bukkit.entity.Player;

public class CosmeticManager {

    public CosmeticPlayerData getData(Player player) {

        Profile profile = AtlasPracticePlugin.getInstance()
                .getProfileManager()
                .getProfile(player.getUniqueId());

        return profile == null ? null : profile.getCosmetics();
    }

    public KillEffect getKillEffect(Player player) {

        CosmeticPlayerData data = getData(player);

        if (data == null) {
            return new NoneKillEffect();
        }

        Cosmetic cosmetic = CosmeticRegistry.getById(CosmeticType.KILL_EFFECT, data.getKillEffect());

        return cosmetic instanceof KillEffect
                ? (KillEffect) cosmetic
                : new NoneKillEffect();
    }

    public VictoryEffect getVictoryEffect(Player player) {

        CosmeticPlayerData data = getData(player);

        if (data == null) {
            return new NoneVictoryEffect();
        }

        Cosmetic cosmetic = CosmeticRegistry.getById(CosmeticType.VICTORY_EFFECT, data.getVictoryEffect());

        return cosmetic instanceof VictoryEffect
                ? (VictoryEffect) cosmetic
                : new NoneVictoryEffect();
    }

    public ProjectileTrail getProjectileTrail(Player player) {

        CosmeticPlayerData data = getData(player);

        if (data == null) {
            return new NoneProjectileTrail();
        }

        Cosmetic cosmetic = CosmeticRegistry.getById(CosmeticType.PROJECTILE_TRAIL, data.getProjectileTrail());

        return cosmetic instanceof ProjectileTrail
                ? (ProjectileTrail) cosmetic
                : new NoneProjectileTrail();
    }

    public WalkingTrail getWalkingTrail(Player player) {

        CosmeticPlayerData data = getData(player);

        if (data == null) {
            return new NoneWalkingTrail();
        }

        Cosmetic cosmetic = CosmeticRegistry.getById(CosmeticType.WALKING_TRAIL, data.getWalkingTrail());

        return cosmetic instanceof WalkingTrail
                ? (WalkingTrail) cosmetic
                : new NoneWalkingTrail();
    }

    public AuraEffect getAura(Player player) {

        CosmeticPlayerData data = getData(player);

        if (data == null) {
            return new NoneAura();
        }

        Cosmetic cosmetic = CosmeticRegistry.getById(CosmeticType.AURA, data.getAura());

        return cosmetic instanceof AuraEffect
                ? (AuraEffect) cosmetic
                : new NoneAura();
    }

    public ChatColorEffect getChatColor(Player player) {

        CosmeticPlayerData data = getData(player);

        if (data == null) {
            return new WhiteChatColor();
        }

        Cosmetic cosmetic = CosmeticRegistry.getById(CosmeticType.CHAT_COLOR, data.getChatColor());

        return cosmetic instanceof ChatColorEffect
                ? (ChatColorEffect) cosmetic
                : new WhiteChatColor();
    }

    public KillMessage getKillMessage(Player player) {

        CosmeticPlayerData data = getData(player);

        if (data == null) {
            return new DefaultKillMessage();
        }

        Cosmetic cosmetic = CosmeticRegistry.getById(CosmeticType.KILL_MESSAGE, data.getKillMessage());

        return cosmetic instanceof KillMessage
                ? (KillMessage) cosmetic
                : new DefaultKillMessage();
    }

    public void playKillEffect(Player killer, Player victim) {
        getKillEffect(killer).play(killer, victim.getLocation());
    }

    public void playVictoryEffect(Player winner) {
        getVictoryEffect(winner).play(winner);
    }

    public String formatChat(Player player, String message) {
        return getChatColor(player).getColor() + message;
    }

    public String getKillMessage(Player killer, Player victim, KillMessageType type) {
        return getKillMessage(killer).format(killer, victim, type);
    }
}