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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProfileState {

    LOBBY(true, false, false, true),
    QUEUE(false, false, false, false),
    QUEUING(false, false, false, false), // Added back to fix compilation
    MATCH(false, true, true, false),
    PARTY(true, false, false, true),
    EVENT(false, true, false, false),
    SPECTATING(false, false, true, false),
    SPECTATE(false, false, true, false), // Added back to fix compilation
    EDITOR(false, false, false, false);

    private final boolean lobbyInteractionAllowed;
    private final boolean activelyFighting;
    private final boolean processingMatchWorldPackets;
    private final boolean openToDuelInvites;

    public boolean isInventoryProtected() {
        return switch (this) {
            case LOBBY, QUEUE, QUEUING, PARTY, SPECTATING, SPECTATE, EDITOR -> true;
            case MATCH, EVENT -> false;
        };
    }

    public boolean isLobby() { return this == LOBBY; }
    public boolean isQueuing() { return this == QUEUE || this == QUEUING; }
    public boolean isInMatch() { return this == MATCH; }
    public boolean isInParty() { return this == PARTY; }
    public boolean isInEvent() { return this == EVENT; }
    public boolean isSpectating() { return this == SPECTATING || this == SPECTATE; }
    public boolean isInEditor() { return this == EDITOR; }
}
