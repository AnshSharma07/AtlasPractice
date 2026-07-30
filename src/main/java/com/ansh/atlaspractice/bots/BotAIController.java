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
// UNDER DEVELOPEMENT, disabled rn
package com.ansh.atlaspractice.bots;

import org.bukkit.scheduler.BukkitRunnable;

public class BotAIController extends BukkitRunnable {

    private final PracticeBot bot;
    private boolean running;

    public BotAIController(PracticeBot bot) {
        this.bot = bot;
    }

    public void start() {
        if (running) {
            return;
        }

        running = true;
        runTaskTimer(
                com.ansh.atlaspractice.AtlasPracticePlugin.getInstance(),
                1L,
                1L
        );
    }

    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        try {
            cancel();
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void run() {
        if (!running) {
            cancel();
            return;
        }

        if (bot == null) {
            cancel();
            return;
        }

        try {
            bot.tick();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            running = false;
            cancel();
        }
    }
}