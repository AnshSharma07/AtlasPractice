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

package com.ansh.atlaspractice.duel;

import com.ansh.atlaspractice.kit.Kit;
import lombok.Getter;

import java.util.UUID;

@Getter
public final class DuelChallenge {

    private final UUID id;
    private final UUID challengerUuid;
    private final UUID targetUuid;
    private final Kit kit;
    private final int totalRounds;
    private final String selectedArenaId;
    private final long expirationTimestamp;

    public DuelChallenge(UUID challengerUuid, UUID targetUuid, Kit kit, int totalRounds) {
        this(challengerUuid, targetUuid, kit, totalRounds, null);
    }

    public DuelChallenge(
            UUID challengerUuid,
            UUID targetUuid,
            Kit kit,
            int totalRounds,
            String selectedArenaId
    ) {
        this.id = UUID.randomUUID();
        this.challengerUuid = challengerUuid;
        this.targetUuid = targetUuid;
        this.kit = kit;
        this.totalRounds = totalRounds;
        this.selectedArenaId = selectedArenaId;
        this.expirationTimestamp = System.currentTimeMillis() + 60_000L;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTimestamp;
    }
}