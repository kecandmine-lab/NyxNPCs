package com.nyx.nyxnpcs.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nyx.nyxnpcs.NyxNPCs;
import com.nyx.nyxnpcs.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class DataManager {

    private final NyxNPCs plugin;
    private final File dataFile;
    private final Gson gson;

    public DataManager(NyxNPCs plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "npcs.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
    }

    public void saveNPCs() {
        try {
            List<NPCData> npcDataList = new ArrayList<>();
            
            Collection<NPC> allNPCs = plugin.getNpcManager().getAllNPCs();
            plugin.getLogger().info("Saving " + allNPCs.size() + " NPCs...");

            for (NPC npc : allNPCs) {
                NPCData data = new NPCData();
                data.uuid = npc.getUuid().toString();
                data.entityId = npc.getEntityId();
                data.name = npc.getName();
                data.world = npc.getLocation().getWorld().getName();
                data.x = npc.getLocation().getX();
                data.y = npc.getLocation().getY();
                data.z = npc.getLocation().getZ();
                data.yaw = npc.getLocation().getYaw();
                data.pitch = npc.getLocation().getPitch();
                data.skinTexture = npc.getSkinTexture();
                data.skinSignature = npc.getSkinSignature();
                data.entityType = npc.getEntityType().name();
                data.lookAtPlayer = npc.isLookAtPlayer();
                data.displayName = npc.getDisplayName();
                data.pose = npc.getPose().name();

                data.actions = new ArrayList<>();
                for (NPC.NPCAction action : npc.getActions()) {
                    ActionData actionData = new ActionData();
                    actionData.type = action.getType().name();
                    actionData.value = action.getValue();
                    data.actions.add(actionData);
                }

                npcDataList.add(data);
            }

            String json = gson.toJson(npcDataList);
            Files.writeString(dataFile.toPath(), json);
            
            plugin.getLogger().info("Successfully saved " + npcDataList.size() + " NPCs to file.");

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save NPCs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadNPCs() {
        if (!dataFile.exists()) {
            plugin.getLogger().info("No NPC data file found. Starting fresh.");
            return;
        }

        try {
            String json = Files.readString(dataFile.toPath());
            Type listType = new TypeToken<List<NPCData>>(){}.getType();
            List<NPCData> npcDataList = gson.fromJson(json, listType);

            if (npcDataList == null || npcDataList.isEmpty()) {
                plugin.getLogger().info("No NPCs to load.");
                return;
            }

            for (NPCData data : npcDataList) {
                try {
                    Location location = new Location(
                        Bukkit.getWorld(data.world),
                        data.x,
                        data.y,
                        data.z,
                        data.yaw,
                        data.pitch
                    );

                    if (location.getWorld() == null) {
                        plugin.getLogger().warning("World '" + data.world + "' not found for NPC: " + data.name);
                        continue;
                    }

                    UUID uuid = UUID.fromString(data.uuid);
                    NPC npc = plugin.getNpcManager().loadNPC(uuid, data.entityId, data.name, location);

                    if (data.skinTexture != null && data.skinSignature != null) {
                        npc.setSkinTexture(data.skinTexture);
                        npc.setSkinSignature(data.skinSignature);
                    }

                    if (data.entityType != null) {
                        try {
                            npc.setEntityType(org.bukkit.entity.EntityType.valueOf(data.entityType));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid entity type: " + data.entityType);
                        }
                    }

                    npc.setLookAtPlayer(data.lookAtPlayer);

                    if (data.displayName != null) {
                        npc.setDisplayName(data.displayName);
                    }

                    if (data.pose != null) {
                        try {
                            npc.setPose(NPC.Pose.valueOf(data.pose));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid pose: " + data.pose);
                        }
                    }

                    if (data.actions != null) {
                        for (ActionData actionData : data.actions) {
                            try {
                                NPC.ActionType type = NPC.ActionType.valueOf(actionData.type);
                                npc.addAction(new NPC.NPCAction(type, actionData.value));
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Invalid action type: " + actionData.type);
                            }
                        }
                    }

                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load NPC: " + data.name + " - " + e.getMessage());
                }
            }

            plugin.getLogger().info("Loaded " + npcDataList.size() + " NPCs.");
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (NPC npc : plugin.getNpcManager().getAllNPCs()) {
                    plugin.getNpcManager().spawnNPCForAll(npc);
                }
            }, 20L);

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load NPCs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class NPCData {
        @SuppressWarnings("unused")
        String uuid;
        @SuppressWarnings("unused")
        int entityId;
        String name;
        String world;
        double x, y, z;
        float yaw, pitch;
        String skinTexture;
        String skinSignature;
        @SuppressWarnings("unused")
        String entityType;
        @SuppressWarnings("unused")
        boolean lookAtPlayer;
        @SuppressWarnings("unused")
        String displayName;
        @SuppressWarnings("unused")
        String pose;
        List<ActionData> actions;
    }

    private static class ActionData {
        String type;
        String value;
    }
}
