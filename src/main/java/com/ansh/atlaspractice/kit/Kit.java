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

import com.ansh.atlaspractice.team.KitColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.ansh.atlaspractice.team.TeamColor;

import java.util.ArrayList;
import java.util.List;

public final class Kit {

    private final String id;
    private final String displayName;
    private boolean hungerLossEnabled = true;
    private Material displayMaterial;
    private boolean damageEnabled = true;
    private ItemStack[] mainContents;
    private ItemStack[] armorContents;
    private boolean boxingMode = false;
    private Material iconMaterial;
    private int iconData;
    private boolean durabilityEnabled = true;
    private String knockbackProfileName;
    private final List<String> arenaIds = new ArrayList<>();
    private String arenaId;
    private boolean comboMode = false;
    private boolean rankedEnabled;
    private boolean buildAllowed;

    public Kit createClone() {
        Kit clone = new Kit(this.id, this.displayName);
        clone.setMainContents(this.mainContents.clone());
        clone.setArmorContents(this.armorContents.clone());
        clone.setIconMaterial(this.iconMaterial);
        clone.setDurabilityEnabled(this.durabilityEnabled);
        return clone;
    }
    public boolean isHungerLossEnabled() {
        return hungerLossEnabled;
    }
    public boolean isComboMode() {return comboMode;}
    public void setComboMode(boolean comboMode) {this.comboMode = comboMode;}
    public boolean isBoxingMode() {
        return boxingMode;
    }
    public void setDurabilityEnabled(boolean durabilityEnabled) {
        this.durabilityEnabled = durabilityEnabled;
    }
    public void setBoxingMode(boolean boxingMode) {
        this.boxingMode = boxingMode;
    }
    public void setHungerLossEnabled(boolean hungerLossEnabled) {
        this.hungerLossEnabled = hungerLossEnabled;
    }
    public boolean isDamageEnabled() {
        return damageEnabled;
    }
    public boolean isDurabilityEnabled() {
        return durabilityEnabled;
    }
    public void setDamageEnabled(boolean damageEnabled) {
        this.damageEnabled = damageEnabled;
    }
    public Kit(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        this.displayMaterial = Material.AIR;
        this.mainContents = new ItemStack[36];
        this.armorContents = new ItemStack[4];
        this.iconMaterial = Material.AIR;
        this.iconData = 0;

        this.knockbackProfileName = "default";

        this.rankedEnabled = true;
        this.buildAllowed = false;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public void setDisplayMaterial(Material displayMaterial) {
        this.displayMaterial = displayMaterial;
    }

    public ItemStack[] getMainContents() {
        return mainContents;
    }

    public void setMainContents(ItemStack[] mainContents) {
        if (mainContents == null) {
            this.mainContents = new ItemStack[36];
            return;
        }

        this.mainContents = new ItemStack[36];

        for (int i = 0; i < Math.min(36, mainContents.length); i++) {
            this.mainContents[i] = mainContents[i];
        }
    }

    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    public void setArmorContents(ItemStack[] armorContents) {
        if (armorContents == null) {
            this.armorContents = new ItemStack[4];
            return;
        }

        this.armorContents = new ItemStack[4];

        for (int i = 0; i < Math.min(4, armorContents.length); i++) {
            this.armorContents[i] = armorContents[i];
        }
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public void setIconMaterial(Material iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    public int getIconData() {
        return iconData;
    }

    public void applyToPlayer(Player player, com.ansh.atlaspractice.profile.Profile profile) {
        player.getInventory().clear();

        ItemStack[] contents;
        ItemStack[] customLayout = profile.getCustomLayout(this.id);

        if (customLayout != null) {
            contents = customLayout.clone();
        } else {
            contents = mainContents.clone();
        }

        player.getInventory().setContents(contents);
        player.getInventory().setArmorContents(armorContents.clone());

        player.updateInventory();
    }

    public void setIconData(int iconData) {
        this.iconData = iconData;
    }

    public String getKnockbackProfileName() {
        return knockbackProfileName;
    }

    public List<String> getArenaIds() {
        return arenaIds;
    }

    public void addArena(String arenaId) {
        if (!arenaIds.contains(arenaId.toLowerCase())) {
            arenaIds.add(arenaId.toLowerCase());
        }
    }

    public void removeArena(String arenaId) {
        arenaIds.remove(arenaId.toLowerCase());
    }

    public void setKnockbackProfileName(String knockbackProfileName) {
        this.knockbackProfileName = knockbackProfileName;
    }

    public boolean isRankedEnabled() {
        return rankedEnabled;
    }

    public void setRankedEnabled(boolean rankedEnabled) {
        this.rankedEnabled = rankedEnabled;
    }

    public boolean isBuildAllowed() {
        return buildAllowed;
    }

    public void setBuildAllowed(boolean buildAllowed) {
        this.buildAllowed = buildAllowed;
    }

    public void applyToPlayer(Player player) {
        player.getInventory().clear();

        player.getInventory().setContents(mainContents.clone());
        player.getInventory().setArmorContents(armorContents.clone());

        player.updateInventory();
    }

    public void applyToPlayer(Player player, TeamColor teamColor) {
        player.getInventory().clear();

        player.getInventory().setContents(
                KitColorUtil.colorInventory(
                        mainContents.clone(),
                        teamColor
                )
        );

        player.getInventory().setArmorContents(
                KitColorUtil.colorArmor(
                        armorContents.clone(),
                        teamColor
                )
        );

        player.updateInventory();
    }

    public void applyToPlayer(Player player, com.ansh.atlaspractice.profile.Profile profile, TeamColor teamColor) {
        player.getInventory().clear();

        ItemStack[] contents;
        ItemStack[] customLayout = profile.getCustomLayout(this.id);

        if (customLayout != null) {
            contents = customLayout.clone();
        } else {
            contents = mainContents.clone();
        }

        player.getInventory().setContents(
                KitColorUtil.colorInventory(
                        contents,
                        teamColor
                )
        );

        player.getInventory().setArmorContents(
                KitColorUtil.colorArmor(
                        armorContents.clone(),
                        teamColor
                )
        );

        player.updateInventory();
    }

    public Kit copy() {
        Kit copy = new Kit(this.id, this.displayName);

        copy.setDisplayMaterial(this.displayMaterial);

        copy.setMainContents(this.mainContents.clone());
        copy.setArmorContents(this.armorContents.clone());

        copy.setIconMaterial(this.iconMaterial);
        copy.setIconData(this.iconData);
        copy.setDurabilityEnabled(this.durabilityEnabled);
        copy.setKnockbackProfileName(this.knockbackProfileName);

        copy.setRankedEnabled(this.rankedEnabled);
        copy.setBuildAllowed(this.buildAllowed);
        copy.setDamageEnabled(this.damageEnabled);
        copy.setBoxingMode(this.boxingMode);
        copy.setHungerLossEnabled(this.hungerLossEnabled);

        return copy;
    }
}