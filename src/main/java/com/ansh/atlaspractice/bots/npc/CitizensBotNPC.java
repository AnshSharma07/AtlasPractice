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
package com.ansh.atlaspractice.bots.npc;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.trait.SkinTrait;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class CitizensBotNPC {
    private static NPCRegistry BOT_REGISTRY;

    private static final AtomicInteger NPC_ID_COUNTER = new AtomicInteger(10_000);

    public static void initRegistry() {
        if (BOT_REGISTRY == null) {
            BOT_REGISTRY = CitizensAPI.getNPCRegistry();
        }
    }

    public static void destroyRegistry() {
        if (BOT_REGISTRY != null) {
            BOT_REGISTRY.deregisterAll();
        }
    }

    private final UUID   uuid;
    private final String name;
    private final NPC    npc;
    private boolean      spawned;
    private final Location pendingSpawnLoc;

    public CitizensBotNPC(
            UUID uuid,
            String name,
            Location spawn,
            String skinTexture,
            String skinSignature
    ) {
        this.uuid           = uuid;
        this.name           = name.length() > 16 ? name.substring(0, 16) : name;
        this.pendingSpawnLoc = spawn;

        int npcIntId = NPC_ID_COUNTER.getAndIncrement();
        this.npc = BOT_REGISTRY.createNPC(EntityType.PLAYER, uuid, npcIntId, this.name);

        // Skin
        if (skinTexture != null && !skinTexture.isEmpty()
                && skinSignature != null && !skinSignature.isEmpty()) {
            npc.getOrAddTrait(SkinTrait.class)
               .setSkinPersistent(uuid.toString(), skinSignature, skinTexture);
        }

        npc.setProtected(false);

        npc.data().setPersistent(NPC.Metadata.REMOVE_FROM_PLAYERLIST, true);

        npc.data().setPersistent(NPC.Metadata.COLLIDABLE, true);

        npc.getNavigator().getDefaultParameters()
           .stuckAction(null)
           .speedModifier(1.0F)
           .range(64F);
    }

    public void spawn() {
        if (spawned) return;
        npc.spawn(pendingSpawnLoc);
        spawned = true;
        Player p = getBukkitEntity();
        if (p != null) p.setRemoveWhenFarAway(false);
    }

    public void despawn() {
        if (!spawned) return;
        stopNavigation();
        if (npc.isSpawned()) npc.despawn(DespawnReason.PLUGIN);
        BOT_REGISTRY.deregister(npc);
        spawned = false;
    }

    public void remove() { despawn(); }

    public boolean isSpawned() { return spawned && npc.isSpawned(); }
    public Location getLocation() {
        if (!isSpawned()) return pendingSpawnLoc;
        return npc.getEntity().getLocation();
    }

    public void teleport(Location location) {
        if (!isSpawned()) return;
        npc.getEntity().teleport(location);
    }

    public void navigateTo(Player target, float speedModifier) {
        if (!isSpawned() || target == null) return;
        npc.getNavigator().setTarget(target, false);
        npc.getNavigator().getLocalParameters()
           .speedModifier(speedModifier)
           .stuckAction(null);
    }

    public void navigateTo(Player target) { navigateTo(target, 1.0F); }

    public void stopNavigation() {
        if (!isSpawned()) return;
        npc.getNavigator().cancelNavigation();
    }

    public boolean isNavigating() {
        return isSpawned() && npc.getNavigator().isNavigating();
    }

    public void setSprinting(boolean sprint) {
        Player p = getBukkitEntity();
        if (p != null) p.setSprinting(sprint);
    }

    public void look(float yaw, float pitch) {
        if (!isSpawned()) return;
        EntityPlayer handle = handle();
        if (handle == null) return;

        handle.yaw   = yaw;
        handle.pitch = pitch;
        handle.aI    = yaw;
        handle.aK    = yaw;
        handle.aJ    = pitch;

        PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket =
                new PacketPlayOutEntity.PacketPlayOutEntityLook(
                        handle.getId(),
                        (byte) (yaw   * 256.0F / 360.0F),
                        (byte) (pitch * 256.0F / 360.0F),
                        handle.onGround
                );
        PacketPlayOutEntityHeadRotation headPacket =
                new PacketPlayOutEntityHeadRotation(
                        handle,
                        (byte) (yaw * 256.0F / 360.0F)
                );
        broadcastPackets(lookPacket, headPacket);
    }

    public void rotate(float yaw, float pitch) { look(yaw, pitch); }

    @Deprecated
    public void setMovementInputs(float forward, float strafe) { }

    @Deprecated
    public void jump() { }

    public void tick() { }

    /** NO-OP. Citizens manages viewer tracking internally. */
    public void refreshViewers() { }

    public void setHeldItem(ItemStack item) {
        npc.getOrAddTrait(Equipment.class).set(EquipmentSlot.HAND, item);
        Player p = getBukkitEntity();
        if (p != null) p.setItemInHand(item);
    }

    public void setArmor(ItemStack[] armor) {
        Equipment equip = npc.getOrAddTrait(Equipment.class);
        if (armor.length > 0 && armor[0] != null) equip.set(EquipmentSlot.BOOTS,      armor[0]);
        if (armor.length > 1 && armor[1] != null) equip.set(EquipmentSlot.LEGGINGS,   armor[1]);
        if (armor.length > 2 && armor[2] != null) equip.set(EquipmentSlot.CHESTPLATE, armor[2]);
        if (armor.length > 3 && armor[3] != null) equip.set(EquipmentSlot.HELMET,     armor[3]);
    }

    public void updateEquipment() { }
    public void swingArm() {
        if (!isSpawned()) return;
        EntityPlayer handle = handle();
        if (handle == null) return;

        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(handle, 0);
        Player botPlayer = getBukkitEntity();
        if (botPlayer == null) return;

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.getWorld().equals(botPlayer.getWorld())) continue;
            if (viewer.getLocation().distanceSquared(botPlayer.getLocation()) > 96.0D * 96.0D) continue;
            ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
        }
    }
    public boolean isDead() {
        EntityPlayer handle = handle();
        return handle == null || handle.dead;
    }

    public float getHealth() {
        EntityPlayer handle = handle();
        return handle == null ? 0f : handle.getHealth();
    }

    public void setHealth(float health) {
        EntityPlayer handle = handle();
        if (handle != null) handle.setHealth(health);
    }

    public boolean isOnGround() {
        EntityPlayer handle = handle();
        return handle != null && handle.onGround;
    }

    public Player getBukkitEntity() {
        if (!isSpawned()) return null;
        org.bukkit.entity.Entity entity = npc.getEntity();
        return (entity instanceof Player) ? (Player) entity : null;
    }

    public EntityPlayer getEntityPlayer() { return handle(); }

    public NPC getCitizensNPC() { return npc; }

    public UUID getUniqueId() { return uuid; }

    public UUID getUuid() { return uuid; }

    public String getName() { return name; }
    private EntityPlayer handle() {
        Player p = getBukkitEntity();
        if (p == null) return null;
        return ((CraftPlayer) p).getHandle();
    }

    private void broadcastPackets(net.minecraft.server.v1_8_R3.Packet<?>... packets) {
        Player botPlayer = getBukkitEntity();
        if (botPlayer == null) return;
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.getWorld().equals(botPlayer.getWorld())) continue;
            if (viewer.getLocation().distanceSquared(botPlayer.getLocation()) > 96.0D * 96.0D) continue;
            PlayerConnection conn = ((CraftPlayer) viewer).getHandle().playerConnection;
            for (net.minecraft.server.v1_8_R3.Packet<?> pkt : packets) {
                conn.sendPacket(pkt);
            }
        }
    }
}
