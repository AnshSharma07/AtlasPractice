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

package com.ansh.atlaspractice.cosmetics;

import com.ansh.atlaspractice.cosmetics.aura.NoneAura;
import com.ansh.atlaspractice.cosmetics.aura.SimpleAuraEffect;
import com.ansh.atlaspractice.cosmetics.chat.SimpleChatColor;
import com.ansh.atlaspractice.cosmetics.chat.WhiteChatColor;
import com.ansh.atlaspractice.cosmetics.kill.NoneKillEffect;
import com.ansh.atlaspractice.cosmetics.kill.SimpleKillEffect;
import com.ansh.atlaspractice.cosmetics.projectile.NoneProjectileTrail;
import com.ansh.atlaspractice.cosmetics.projectile.SimpleProjectileTrail;
import com.ansh.atlaspractice.cosmetics.victory.NoneVictoryEffect;
import com.ansh.atlaspractice.cosmetics.victory.SimpleVictoryEffect;
import com.ansh.atlaspractice.cosmetics.message.KillMessageLoader;
import com.ansh.atlaspractice.cosmetics.walking.NoneWalkingTrail;
import com.ansh.atlaspractice.cosmetics.walking.SimpleWalkingTrail;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;

public final class Cosmetics {

    private Cosmetics() {
    }

    public static void registerAll() {
        CosmeticRegistry.clear();

        CosmeticRegistry.register(new NoneKillEffect());
        CosmeticRegistry.register(new SimpleKillEffect("lightning", "Lightning", Material.NETHER_STAR, Effect.SMOKE, Sound.ANVIL_LAND, 6, true));
        CosmeticRegistry.register(new SimpleKillEffect("explosion", "Explosion", Material.TNT, Effect.EXPLOSION_LARGE, Sound.EXPLODE, 2, false));
        CosmeticRegistry.register(new SimpleKillEffect("flame_burst", "Flame Burst", Material.BLAZE_POWDER, Effect.MOBSPAWNER_FLAMES, Sound.FIRE, 10, false));
        CosmeticRegistry.register(new SimpleKillEffect("heart_burst", "Heart Burst", Material.RED_ROSE, Effect.HEART, Sound.NOTE_PLING, 8, false));
        CosmeticRegistry.register(new SimpleKillEffect("cloud_burst", "Cloud Burst", Material.WEB, Effect.CLOUD, Sound.FIZZ, 8, false));
        CosmeticRegistry.register(new SimpleKillEffect("crit_explosion", "Crit Explosion", Material.IRON_SWORD, Effect.CRIT, Sound.ANVIL_LAND, 12, false));
        CosmeticRegistry.register(new SimpleKillEffect("magic", "Magic", Material.POTION, Effect.WITCH_MAGIC, Sound.LEVEL_UP, 10, false));
        CosmeticRegistry.register(new SimpleKillEffect("portal", "Portal", Material.ENDER_PEARL, Effect.PORTAL, Sound.ENDERMAN_TELEPORT, 12, false));
        CosmeticRegistry.register(new SimpleKillEffect("smoke", "Smoke", Material.COAL, Effect.SMOKE, Sound.FIZZ, 10, false));
        CosmeticRegistry.register(new SimpleKillEffect("happy_villager", "Happy Villager", Material.EMERALD, Effect.HAPPY_VILLAGER, Sound.VILLAGER_YES, 8, false));

        CosmeticRegistry.register(new NoneVictoryEffect());
        CosmeticRegistry.register(new SimpleVictoryEffect("firework", "Firework", Material.FIREWORK, Effect.FIREWORKS_SPARK, Sound.FIREWORK_LAUNCH, true, false, false));
        CosmeticRegistry.register(new SimpleVictoryEffect("firework_rain", "Firework Rain", Material.FIREWORK_CHARGE, Effect.FIREWORKS_SPARK, Sound.FIREWORK_BLAST, true, false, true));
        CosmeticRegistry.register(new SimpleVictoryEffect("lightning_celebration", "Lightning Celebration", Material.NETHER_STAR, Effect.SMOKE, Sound.AMBIENCE_THUNDER, false, true, false));
        CosmeticRegistry.register(new SimpleVictoryEffect("flame_circle", "Flame Circle", Material.BLAZE_POWDER, Effect.MOBSPAWNER_FLAMES, Sound.FIRE, false, false, false));
        CosmeticRegistry.register(new SimpleVictoryEffect("cloud_circle", "Cloud Circle", Material.WEB, Effect.CLOUD, Sound.FIZZ, false, false, false));
        CosmeticRegistry.register(new SimpleVictoryEffect("hearts", "Hearts", Material.RED_ROSE, Effect.HEART, Sound.NOTE_PLING, false, false, false));
        CosmeticRegistry.register(new SimpleVictoryEffect("portal_spiral", "Portal Spiral", Material.ENDER_PEARL, Effect.PORTAL, Sound.ENDERMAN_TELEPORT, false, false, true));
        CosmeticRegistry.register(new SimpleVictoryEffect("villager_celebration", "Villager Celebration", Material.EMERALD, Effect.HAPPY_VILLAGER, Sound.VILLAGER_YES, false, false, false));
        CosmeticRegistry.register(new SimpleVictoryEffect("magic", "Magic", Material.POTION, Effect.WITCH_MAGIC, Sound.LEVEL_UP, false, false, true));
        CosmeticRegistry.register(new SimpleVictoryEffect("smoke", "Smoke", Material.COAL, Effect.SMOKE, Sound.FIZZ, false, false, false));
        CosmeticRegistry.register(new NoneProjectileTrail());
        registerProjectile("flame", "Flame", Material.BLAZE_POWDER, Effect.MOBSPAWNER_FLAMES);
        registerProjectile("smoke", "Smoke", Material.COAL, Effect.SMOKE);
        registerProjectile("heart", "Heart", Material.RED_ROSE, Effect.HEART);
        registerProjectile("portal", "Portal", Material.ENDER_PEARL, Effect.PORTAL);
        registerProjectile("crit", "Crit", Material.IRON_SWORD, Effect.CRIT);
        registerProjectile("magic_crit", "Magic Crit", Material.POTION, Effect.MAGIC_CRIT);
        registerProjectile("cloud", "Cloud", Material.WEB, Effect.CLOUD);
        registerProjectile("happy_villager", "Happy Villager", Material.EMERALD, Effect.HAPPY_VILLAGER);
        registerProjectile("redstone", "Redstone", Material.REDSTONE, Effect.COLOURED_DUST);
        registerProjectile("lava", "Lava", Material.LAVA_BUCKET, Effect.LAVADRIP);
        CosmeticRegistry.register(new NoneWalkingTrail());
        registerWalking("heart_footsteps", "Heart Footsteps", Material.RED_ROSE, Effect.HEART);
        registerWalking("cloud_footsteps", "Cloud Footsteps", Material.WEB, Effect.CLOUD);
        registerWalking("smoke_footsteps", "Smoke Footsteps", Material.COAL, Effect.SMOKE);
        registerWalking("flame_footsteps", "Flame Footsteps", Material.BLAZE_POWDER, Effect.MOBSPAWNER_FLAMES);
        registerWalking("crit_footsteps", "Crit Footsteps", Material.IRON_SWORD, Effect.CRIT);
        registerWalking("portal_footsteps", "Portal Footsteps", Material.ENDER_PEARL, Effect.PORTAL);
        registerWalking("villager_footsteps", "Villager Footsteps", Material.EMERALD, Effect.HAPPY_VILLAGER);
        registerWalking("magic_footsteps", "Magic Footsteps", Material.POTION, Effect.WITCH_MAGIC);
        registerWalking("lava_footsteps", "Lava Footsteps", Material.LAVA_BUCKET, Effect.LAVADRIP);
        registerWalking("snow_footsteps", "Snow Footsteps", Material.SNOW_BALL, Effect.SNOWBALL_BREAK);
        CosmeticRegistry.register(new NoneAura());
        registerAura("flame", "Flame", Material.BLAZE_POWDER, Effect.MOBSPAWNER_FLAMES);
        registerAura("heart", "Heart", Material.RED_ROSE, Effect.HEART);
        registerAura("magic", "Magic", Material.POTION, Effect.WITCH_MAGIC);
        registerAura("portal", "Portal", Material.ENDER_PEARL, Effect.PORTAL);
        registerAura("cloud", "Cloud", Material.WEB, Effect.CLOUD);
        registerAura("smoke", "Smoke", Material.COAL, Effect.SMOKE);
        registerAura("crit", "Crit", Material.IRON_SWORD, Effect.CRIT);
        registerAura("happy_villager", "Happy Villager", Material.EMERALD, Effect.HAPPY_VILLAGER);
        registerAura("lava", "Lava", Material.LAVA_BUCKET, Effect.LAVADRIP);
        registerAura("snow", "Snow", Material.SNOW_BALL, Effect.SNOWBALL_BREAK);

        CosmeticRegistry.register(new WhiteChatColor());
        registerChat("gray", "Gray", Material.INK_SACK, ChatColor.GRAY);
        registerChat("dark_gray", "Dark Gray", Material.COAL, ChatColor.DARK_GRAY);
        registerChat("yellow", "Yellow", Material.YELLOW_FLOWER, ChatColor.YELLOW);
        registerChat("gold", "Gold", Material.GOLD_INGOT, ChatColor.GOLD);
        registerChat("red", "Red", Material.REDSTONE, ChatColor.RED);
        registerChat("dark_red", "Dark Red", Material.REDSTONE_BLOCK, ChatColor.DARK_RED);
        registerChat("green", "Green", Material.EMERALD, ChatColor.GREEN);
        registerChat("dark_green", "Dark Green", Material.CACTUS, ChatColor.DARK_GREEN);
        registerChat("blue", "Blue", Material.LAPIS_BLOCK, ChatColor.BLUE);
        registerChat("aqua", "Aqua", Material.DIAMOND, ChatColor.AQUA);
        registerChat("light_purple", "Light Purple", Material.INK_SACK, ChatColor.LIGHT_PURPLE);
        KillMessageLoader.load();
    }

    private static void registerProjectile(String id, String name, Material icon, Effect effect) {
        CosmeticRegistry.register(new SimpleProjectileTrail(id, name, icon, effect, 1));
    }

    private static void registerWalking(String id, String name, Material icon, Effect effect) {
        CosmeticRegistry.register(new SimpleWalkingTrail(id, name, icon, effect));
    }

    private static void registerAura(String id, String name, Material icon, Effect effect) {
        CosmeticRegistry.register(new SimpleAuraEffect(id, name, icon, effect));
    }

    private static void registerChat(String id, String name, Material icon, ChatColor color) {
        CosmeticRegistry.register(new SimpleChatColor(id, name, icon, color));
    }
}
