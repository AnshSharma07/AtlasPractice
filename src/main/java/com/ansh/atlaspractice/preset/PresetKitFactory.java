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

package com.ansh.atlaspractice.preset;

import java.util.ArrayList;
import java.util.List;

import com.ansh.atlaspractice.preset.impl.*;
public class PresetKitFactory {
    
    private static final List<PresetKit> PRESETS = new ArrayList<>();

    static {
        register(new NoDebuffPreset());
        register(new BoxingPreset());
        register(new SumoPreset());
        register(new BuildUHCPreset());
        register(new ComboPreset());
        register(new GapplePreset());
        register(new ArcherPreset());
        register(new ClassicPreset());
        register(new BridgePreset());
        register(new PearlFightPreset());
        register(new StickFightPreset());
        register(new IronPreset());
        register(new DiamondPreset());
        register(new HCFPreset());
        register(new BedfightPreset());
        register(new FireballFightPreset());
        register(new TopFightPreset());
        register(new FinalUHCPreset());
        register(new DebuffPreset());
        register(new SoupPreset());
        register(new AxePreset());
        register(new BattleRushPreset());
    }

    public static void register(PresetKit preset) {
        PRESETS.add(preset);
    }

    public static List<PresetKit> getPresets() {
        return PRESETS;
    }

    public static PresetKit getPreset(String id) {
        for (PresetKit preset : PRESETS) {
            if (preset.getId().equalsIgnoreCase(id)) {
                return preset;
            }
        }
        return null;
    }
}