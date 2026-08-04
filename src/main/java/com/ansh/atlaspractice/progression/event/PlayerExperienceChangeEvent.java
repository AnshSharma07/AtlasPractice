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

package com.ansh.atlaspractice.progression.event;
import com.ansh.atlaspractice.profile.Profile;import org.bukkit.event.*;
public class PlayerExperienceChangeEvent extends Event { private static final HandlerList HANDLERS=new HandlerList(); private final Profile profile; private final long oldExperience,newExperience; public PlayerExperienceChangeEvent(Profile p,long o,long n){profile=p;oldExperience=o;newExperience=n;} public Profile getProfile(){return profile;} public long getOldExperience(){return oldExperience;} public long getNewExperience(){return newExperience;} public HandlerList getHandlers(){return HANDLERS;} public static HandlerList getHandlerList(){return HANDLERS;} }
