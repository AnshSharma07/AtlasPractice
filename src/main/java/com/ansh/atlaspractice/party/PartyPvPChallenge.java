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

package com.ansh.atlaspractice.party;

import com.ansh.atlaspractice.kit.Kit;
import lombok.Getter;
import java.util.UUID;

@Getter
public final class PartyPvPChallenge {

    private final UUID challengerPartyId;
    private final UUID targetPartyId;
    private final UUID challengerLeader;
    private final UUID targetLeader;
    private final Kit kit;
    private final int requiredWins;
    private final long expirationTimestamp;

    public PartyPvPChallenge(UUID challengerPartyId, UUID targetPartyId, UUID challengerLeader, UUID targetLeader, Kit kit, int requiredWins) {
        this.challengerPartyId = challengerPartyId;
        this.targetPartyId = targetPartyId;
        this.challengerLeader = challengerLeader;
        this.targetLeader = targetLeader;
        this.kit = kit;
        this.requiredWins = requiredWins;
        this.expirationTimestamp = System.currentTimeMillis() + 60_000L;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTimestamp;
    }
}