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

package com.ansh.atlaspractice.listeners;

import com.ansh.atlaspractice.AtlasPracticePlugin;
import com.ansh.atlaspractice.match.DuelMatch;
import com.ansh.atlaspractice.match.Match;
import com.ansh.atlaspractice.match.MatchManager;
import com.ansh.atlaspractice.match.MatchTeam;
import com.ansh.atlaspractice.profile.Profile;
import com.ansh.atlaspractice.profile.ProfileManager;
import com.ansh.atlaspractice.profile.ProfileState;
import com.ansh.atlaspractice.util.CombatActionBar;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

@RequiredArgsConstructor
public final class PlayerCombatListener implements Listener {

    private final ProfileManager profileManager;
    private final MatchManager matchManager;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player)
                || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        boolean attackerBot = AtlasPracticePlugin.getInstance().getBotManager().isBot(attacker.getUniqueId());
        boolean victimBot = AtlasPracticePlugin.getInstance().getBotManager().isBot(victim.getUniqueId());

        Profile attackerProfile =
                this.profileManager.getProfile(attacker.getUniqueId());

        Profile victimProfile =
                this.profileManager.getProfile(victim.getUniqueId());

        if ((!attackerBot && attackerProfile == null) || (!victimBot && victimProfile == null)) {
            event.setCancelled(true);
            return;
        }

        if ((!attackerBot && attackerProfile.getState() != ProfileState.MATCH)
                || (!victimBot && victimProfile.getState() != ProfileState.MATCH)) {

            event.setCancelled(true);
            return;
        }

        UUID attackerMatchId =
                this.matchManager.getPlayerMatchId(attacker.getUniqueId());

        UUID victimMatchId =
                this.matchManager.getPlayerMatchId(victim.getUniqueId());

        if (attackerMatchId == null
                || victimMatchId == null
                || !attackerMatchId.equals(victimMatchId)) {

            event.setCancelled(true);
            return;
        }

        Match match = this.matchManager.getLiveMatch(attackerMatchId);
        if (match == null || match.getState() != Match.MatchState.FIGHTING) {
            event.setCancelled(true);
            return;
        }
        if (match.getKit().isComboMode()) {
            victim.setNoDamageTicks(0);
        }
        if (match == null
                || match.getState() != Match.MatchState.FIGHTING) {
            event.setCancelled(true);
            return;
        }
        if (match.getKit().isComboMode()) {
            victim.setMaximumNoDamageTicks(5);
            victim.setNoDamageTicks(0);
        } else if (victim.getMaximumNoDamageTicks() != 20) {
            victim.setMaximumNoDamageTicks(20);
        }
        boolean noDamage = !match.getKit().isDamageEnabled();
        if (match.getTeams().size() > 1) {

            MatchTeam attackerTeam = match.getTeams().stream()
                    .filter(team -> team.getPlayers().stream()
                            .anyMatch(mp -> mp.getUuid().equals(attacker.getUniqueId())))
                    .findFirst()
                    .orElse(null);

            MatchTeam victimTeam = match.getTeams().stream()
                    .filter(team -> team.getPlayers().stream()
                            .anyMatch(mp -> mp.getUuid().equals(victim.getUniqueId())))
                    .findFirst()
                    .orElse(null);

            if (attackerTeam != null && attackerTeam == victimTeam) {
                event.setCancelled(true);
                return;
            }
        }
        this.matchManager.setLastAttacker(victim.getUniqueId(), attacker.getUniqueId());
        // Action bar hp and kill display
        if (match.getKit().isDamageEnabled()) {

            double remainingHealth =
                    Math.max(0.0D, victim.getHealth() - event.getFinalDamage());

            if (remainingHealth > 0.0D) {
                CombatActionBar.showHealth(
                        attacker,
                        victim,
                        remainingHealth
                );
            }
        }
        if (noDamage) {

            event.setDamage(0.0);
            victim.setHealth(20.0);
            if (!match.getKit().isBoxingMode()) {
                return;
            }

            int hits = match.addBoxingHit(attacker.getUniqueId());
// Combo
            match.getComboTracker().resetCombo(victim.getUniqueId());

            int combo =
                    match.getComboTracker()
                            .addCombo(attacker.getUniqueId());

            if (hits >= 100 && match.getState() == Match.MatchState.FIGHTING) {

                MatchTeam winningTeam = match.getTeams().stream()
                        .filter(team -> team.getPlayers().stream()
                                .anyMatch(mp -> mp.getUuid().equals(attacker.getUniqueId())))
                        .findFirst()
                        .orElse(null);

                if (match instanceof DuelMatch) {
                    ((DuelMatch) match).handleRoundWin(winningTeam);
                } else {
                    matchManager.triggerMatchEndSequence(match, winningTeam);
                }
            }

            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onGeneralEnvironmentalDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        Profile profile =
                this.profileManager.getProfile(player.getUniqueId());

        if (profile == null) {
            return;
        }
        if (profile.getState() != ProfileState.MATCH) {

            event.setCancelled(true);

            if (event.getCause()
                    == EntityDamageEvent.DamageCause.VOID) {

                player.teleport(
                        player.getWorld().getSpawnLocation()
                );
            }

            return;
        }

        UUID matchId =
                this.matchManager.getPlayerMatchId(player.getUniqueId());

        Match match =
                this.matchManager.getLiveMatch(matchId);

        if (match == null) {
            return;
        }

        if (!match.getKit().isDamageEnabled()) {

            switch (event.getCause()) {

                case FIRE:
                case FIRE_TICK:
                case LAVA:
                case FALL:
                case VOID:
                case SUFFOCATION:
                case DROWNING:
                case POISON:
                case WITHER:
                    event.setCancelled(true);
                    return;
            }
        }
        if (match.getState() != Match.MatchState.FIGHTING) {

            event.setCancelled(true);
        }
    }
}