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

package com.ansh.atlaspractice.match;

import com.ansh.atlaspractice.arena.Arena;
import com.ansh.atlaspractice.bots.BotDifficulty;
import com.ansh.atlaspractice.kit.Kit;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;


@Getter
@Builder
public final class MatchContext {

    private final Kit kit;
    private final Arena arena;
    private final List<MatchTeam> teams;
    private final boolean ranked;
    private UUID botUuid;
    // Optional properties used exclusively under special category parameters
    private final boolean botMatch;
    private final BotDifficulty botDifficulty;
    private final UUID singlePlayerOwner;
    public UUID getBotUuid() {
        return botUuid;
    }

    public void setBotUuid(UUID botUuid) {
        this.botUuid = botUuid;
    }
}
