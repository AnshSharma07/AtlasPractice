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

package com.ansh.atlaspractice.preset.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.kit.Kit;
import com.ansh.atlaspractice.preset.PresetKit;
import com.ansh.atlaspractice.preset.PresetKitFactory;

public class PresetKitCreateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("atlas.admin")) {
            player.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length == 0) return true;

        String presetId = args[0];
        PresetKit preset = PresetKitFactory.getPreset(presetId);

        if (preset == null) return true;

        var kitManager = AtlasPracticePlugin.getInstance().getKitManager();

        if (kitManager.getKit(preset.getDisplayName()).isPresent()) {
            player.sendMessage("§cA kit named '" + preset.getDisplayName() + "' already exists.");
            return true;
        }

        Kit kit = new Kit(preset.getId(), preset.getDisplayName());

        // Get the full icon configuration from the implementation class
        ItemStack icon = preset.getDisplayIcon();
        if (icon != null) {
            kit.setDisplayMaterial(icon.getType());
            kit.setIconMaterial(icon.getType());
            kit.setIconData(icon.getDurability()); // Captures potion type/durability values!
        }

        // Setup inventory items
        preset.setup(kit);

        try {
            kitManager.registerKit(kit);
            kitManager.saveKitToDisk(kit);
            player.sendMessage("§aSuccessfully created and saved preset kit: §f" + preset.getDisplayName());
        } catch (Exception e) {
            AtlasPracticePlugin.getInstance().getLogger().log(java.util.logging.Level.SEVERE, "Failed to create kit", e);
            player.sendMessage("§cAn error occurred. Check console.");
        }

        return true;
    }
}