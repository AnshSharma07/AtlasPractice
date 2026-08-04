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

package com.ansh.atlaspractice.profile;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public final class ProfileManager {

    private final AtlasPracticePlugin plugin; // Added plugin reference hook
    private final Map<UUID, Profile> profiles = new ConcurrentHashMap<>();

    public Profile createProfile(UUID uuid, String name) {
        return this.profiles.compute(uuid, (ignored, existing) -> {
            if (existing != null) {
                return existing;
            } return this.plugin.getDatabaseService().loadProfileData(uuid, name);
        });
    }

    public Profile getProfile(UUID uuid) {
        return this.profiles.get(uuid);
    }

    public int getSelectedLayout(UUID uuid, String kitId) {
        Profile profile = profiles.get(uuid);
        if (profile == null) {
            return 1;
        }
        return profile.getSelectedLayout(kitId);
    }

    public void setSelectedLayout(UUID uuid, String kitId, int layout) {
        Profile profile = profiles.get(uuid);
        if (profile != null) {
            profile.setSelectedLayout(kitId, layout);
        }
    }

    public int getProfilesCount() {
        return this.profiles.size();
    }

    public Collection<Profile> getProfiles() {
        return Collections.unmodifiableCollection(this.profiles.values());
    }

    public void saveAndUnloadProfile(UUID uuid) {
        Profile profile = this.profiles.remove(uuid);
        if (profile != null) {
            this.plugin.getDatabaseService().saveProfileDataAsync(profile);
        }
    }
}
