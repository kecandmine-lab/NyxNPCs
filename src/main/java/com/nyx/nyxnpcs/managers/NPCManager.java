package com.nyx.nyxnpcs.managers;

import com.nyx.nyxnpcs.NyxNPCs;
import com.nyx.nyxnpcs.npc.NPC;
import com.nyx.nyxnpcs.utils.PacketHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NPCManager {

    private final NyxNPCs plugin;
    private final Map<UUID, NPC> npcs;
    private final Map<Integer, UUID> entityIdToUUID;
    private final AtomicInteger entityIdCounter;
    private final PacketHandler packetHandler;

    public NPCManager(NyxNPCs plugin) {
        this.plugin = plugin;
        this.npcs = new ConcurrentHashMap<>();
        this.entityIdToUUID = new ConcurrentHashMap<>();
        this.entityIdCounter = new AtomicInteger(100000);
        this.packetHandler = new PacketHandler();
    }

    public NPC createNPC(String name, Location location) {
        UUID uuid = UUID.randomUUID();
        int entityId = entityIdCounter.incrementAndGet();
        
        NPC npc = new NPC(uuid, entityId, name, location);
        npcs.put(uuid, npc);
        entityIdToUUID.put(entityId, uuid);
        
        spawnNPCForAll(npc);
        
        return npc;
    }

    public NPC loadNPC(UUID uuid, int entityId, String name, Location location) {
        NPC npc = new NPC(uuid, entityId, name, location);
        npcs.put(uuid, npc);
        entityIdToUUID.put(entityId, uuid);
        
        if (entityId >= entityIdCounter.get()) {
            entityIdCounter.set(entityId + 1);
        }
        
        return npc;
    }

    public void removeNPC(UUID uuid) {
        NPC npc = npcs.remove(uuid);
        if (npc != null) {
            entityIdToUUID.remove(npc.getEntityId());
            despawnNPCForAll(npc);
        }
    }

    public void removeNPC(int entityId) {
        UUID uuid = entityIdToUUID.get(entityId);
        if (uuid != null) {
            removeNPC(uuid);
        }
    }

    public NPC getNPC(UUID uuid) {
        return npcs.get(uuid);
    }

    public NPC getNPCByEntityId(int entityId) {
        UUID uuid = entityIdToUUID.get(entityId);
        return uuid != null ? npcs.get(uuid) : null;
    }

    public Collection<NPC> getAllNPCs() {
        return npcs.values();
    }

    public void spawnNPCForAll(NPC npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            spawnNPCForPlayer(npc, player);
        }
    }

    public void spawnNPCForPlayer(NPC npc, Player player) {
        packetHandler.spawnNPC(player, npc);
    }

    public void despawnNPCForAll(NPC npc) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            despawnNPCForPlayer(npc, player);
        }
    }

    public void despawnNPCForPlayer(NPC npc, Player player) {
        packetHandler.despawnNPC(player, npc);
    }

    public void removeAllNPCs() {
        for (NPC npc : new ArrayList<>(npcs.values())) {
            despawnNPCForAll(npc);
        }
        npcs.clear();
        entityIdToUUID.clear();
    }

    public void updateNPCSkin(NPC npc, String skinTexture, String skinSignature) {
        npc.setSkinTexture(skinTexture);
        npc.setSkinSignature(skinSignature);
        
        despawnNPCForAll(npc);
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            spawnNPCForAll(npc);
        }, 5L);
    }

    public void showAllNPCsToPlayer(Player player) {
        for (NPC npc : npcs.values()) {
            spawnNPCForPlayer(npc, player);
        }
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }
}
