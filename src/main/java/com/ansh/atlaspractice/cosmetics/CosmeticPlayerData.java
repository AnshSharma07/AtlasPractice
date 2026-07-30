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

public class CosmeticPlayerData {

    private final java.util.Set<String> ownedCosmetics = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private String killEffect = "none";
    private String victoryEffect = "none";
    private String projectileTrail = "none";
    private String walkingTrail = "none";
    private String aura = "none";
    private String chatColor = "white";
    private String killMessage = "default";

    public CosmeticPlayerData() {
        ownedCosmetics.add("KILL_EFFECT:none"); ownedCosmetics.add("VICTORY_EFFECT:none"); ownedCosmetics.add("AURA:none");
        ownedCosmetics.add("WALKING_TRAIL:none"); ownedCosmetics.add("PROJECTILE_TRAIL:none"); ownedCosmetics.add("CHAT_COLOR:white"); ownedCosmetics.add("KILL_MESSAGE:default");
    }
    public boolean owns(com.ansh.atlaspractice.cosmetics.CosmeticType type, String id) { return ownedCosmetics.contains(type.name() + ":" + (id == null ? "" : id.toLowerCase())); }
    public void addOwned(com.ansh.atlaspractice.cosmetics.CosmeticType type, String id) { if (id != null) ownedCosmetics.add(type.name() + ":" + id.toLowerCase()); }
    public java.util.Set<String> getOwnedCosmetics() { return ownedCosmetics; }
    public void loadOwnedCosmetics(String serialized) { if (serialized != null && !serialized.isEmpty()) for (String key : serialized.split(",")) if (!key.isBlank()) ownedCosmetics.add(key); }
    public String serializeOwnedCosmetics() { return String.join(",", ownedCosmetics); }

    public String getKillEffect() {
        return killEffect;
    }

    public void setKillEffect(String killEffect) {
        this.killEffect = killEffect == null ? "none" : killEffect;
    }

    public String getVictoryEffect() {
        return victoryEffect;
    }

    public void setVictoryEffect(String victoryEffect) {
        this.victoryEffect = victoryEffect == null ? "none" : victoryEffect;
    }

    public String getProjectileTrail() {
        return projectileTrail;
    }

    public void setProjectileTrail(String projectileTrail) {
        this.projectileTrail = projectileTrail == null ? "none" : projectileTrail;
    }

    public String getWalkingTrail() {
        return walkingTrail;
    }

    public void setWalkingTrail(String walkingTrail) {
        this.walkingTrail = walkingTrail == null ? "none" : walkingTrail;
    }

    public String getAura() {
        return aura;
    }

    public void setAura(String aura) {
        this.aura = aura == null ? "none" : aura;
    }

    public String getChatColor() {
        return chatColor;
    }

    public void setChatColor(String chatColor) {
        this.chatColor = chatColor == null ? "white" : chatColor;
    }

    public String getKillMessage() {
        return killMessage;
    }

    public void setKillMessage(String killMessage) {
        this.killMessage = killMessage == null ? "default" : killMessage;
    }
}