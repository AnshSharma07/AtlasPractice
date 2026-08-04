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

package com.ansh.atlaspractice.scoreboard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
public final class ScoreboardBoard {

    private final UUID playerUuid;
    private final Scoreboard scoreboard;
    private final Objective objective;
    private final List<Team> linesCache = new ArrayList<>();

    public ScoreboardBoard(Player player) {
        this.playerUuid = player.getUniqueId();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        this.objective = this.scoreboard.registerNewObjective("atlas_sb", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName("§6§lLoading...");
        Objective hp = this.scoreboard.getObjective("atlas_hp");

        if (hp == null) {
            hp = this.scoreboard.registerNewObjective("atlas_hp", "health");
        }

        hp.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        hp.setDisplayName("§c❤");
        // Pre-build structural Team slots to prevent client rendering flicker
        for (int i = 0; i < 15; i++) {
            Team entryTeam = this.scoreboard.registerNewTeam("sb_line_" + i);
            String entryIdentifier = "§" + ChatColorEntries.values()[i].getColorCode() + "§r";
            entryTeam.addEntry(entryIdentifier);
            //     this.objective.getScore(entryIdentifier).setScore(15 - i);
            this.linesCache.add(entryTeam);
        }

        player.setScoreboard(this.scoreboard);
    }


    public void updateContents(String dynamicTitle, List<String> lines) {
        this.objective.setDisplayName(dynamicTitle);

        for (int i = 0; i < 15; i++) {
            Team entryTeam = this.linesCache.get(i);

            String entryIdentifier = "§" + ChatColorEntries.values()[i].getColorCode() + "§r";

            if (i < lines.size()) {
                String targetLine = lines.get(i);

                if (targetLine.length() <= 16) {
                    entryTeam.setPrefix(targetLine);
                    entryTeam.setSuffix("");
                } else {
                    String prefix = targetLine.substring(0, 16);

                    // Avoid cutting a color code in half
                    if (prefix.endsWith("§")) {
                        prefix = prefix.substring(0, 15);
                    }

                    String suffix = targetLine.substring(prefix.length());

                    // Preserve colors
                    suffix = ChatColor.getLastColors(prefix) + suffix;

                    // Trim suffix safely
                    if (suffix.length() > 16) {
                        suffix = suffix.substring(0, 16);

                        // Avoid ending on half a color code
                        if (suffix.endsWith("§")) {
                            suffix = suffix.substring(0, 15);
                        }
                    }

                    entryTeam.setPrefix(prefix);
                    entryTeam.setSuffix(suffix);
                }

                this.objective.getScore(entryIdentifier).setScore(15 - i);

            } else {
                entryTeam.setPrefix("");
                entryTeam.setSuffix("");
                this.scoreboard.resetScores(entryIdentifier);
            }
        }
    }

    @Getter
    private enum ChatColorEntries {
        L0('0'), L1('1'), L2('2'), L3('3'), L4('4'), L5('5'), L6('6'), L7('7'), L8('8'), L9('9'),
        LA('a'), LB('b'), LC('c'), LD('d'), LE('e');

        private final char colorCode;
        ChatColorEntries(char colorCode) { this.colorCode = colorCode; }
    }
}