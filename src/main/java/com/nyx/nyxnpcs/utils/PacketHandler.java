package com.nyx.nyxnpcs.utils;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.*;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.nyx.nyxnpcs.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;

public class PacketHandler {

    public void spawnNPC(Player player, NPC npc) {
        Location loc = npc.getLocation();
        
        if (npc.getEntityType() == EntityType.PLAYER) {
            spawnPlayerNPC(player, npc, loc);
        } else {
            spawnMobNPC(player, npc, loc);
        }
    }

    private void spawnPlayerNPC(Player player, NPC npc, Location loc) {
        // Usar displayName si existe, sino usar el nombre normal
        String visibleName = npc.getDisplayName() != null ? 
            ChatColor.translateAlternateColorCodes('&', npc.getDisplayName()) : 
            npc.getName();
            
        UserProfile userProfile = new UserProfile(npc.getUuid(), visibleName);
        if (npc.getSkinTexture() != null && npc.getSkinSignature() != null) {
            TextureProperty textureProperty = new TextureProperty("textures", npc.getSkinTexture(), npc.getSkinSignature());
            userProfile.setTextureProperties(List.of(textureProperty));
        }
        
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo = 
            new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                userProfile,
                false,
                0,
                GameMode.CREATIVE,
                null,
                null
            );
        
        WrapperPlayServerPlayerInfoUpdate infoPacket = new WrapperPlayServerPlayerInfoUpdate(
            EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, 
                      WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED),
            playerInfo
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, infoPacket);
        
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
            npc.getEntityId(),
            Optional.of(npc.getUuid()),
            com.github.retrooper.packetevents.protocol.entity.type.EntityTypes.PLAYER,
            new Vector3d(loc.getX(), loc.getY(), loc.getZ()),
            loc.getPitch(),
            loc.getYaw(),
            loc.getYaw(),
            0,
            Optional.empty()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);
        
        List<EntityData> metadata = new ArrayList<>();
        metadata.add(new EntityData(17, EntityDataTypes.BYTE, (byte) 0x7F));
        
        // Añadir pose si está sentado
        if (npc.getPose() == NPC.Pose.SITTING) {
            metadata.add(new EntityData(6, EntityDataTypes.ENTITY_POSE, 
                com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.SITTING));
        }
        
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
            npc.getEntityId(),
            metadata
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
        
        WrapperPlayServerEntityHeadLook headLookPacket = new WrapperPlayServerEntityHeadLook(
            npc.getEntityId(),
            loc.getYaw()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, headLookPacket);
    }

    private void spawnMobNPC(Player player, NPC npc, Location loc) {
        com.github.retrooper.packetevents.protocol.entity.type.EntityType peType = 
            convertBukkitEntityType(npc.getEntityType());
        
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
            npc.getEntityId(),
            Optional.of(npc.getUuid()),
            peType,
            new Vector3d(loc.getX(), loc.getY(), loc.getZ()),
            loc.getPitch(),
            loc.getYaw(),
            loc.getYaw(),
            0,
            Optional.empty()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);
        
        // Añadir metadata del nombre personalizado y pose para mobs
        List<EntityData> metadata = new ArrayList<>();
        
        if (npc.getDisplayName() != null) {
            String coloredName = ChatColor.translateAlternateColorCodes('&', npc.getDisplayName());
            Component nameComponent = LegacyComponentSerializer.legacySection().deserialize(coloredName);
            metadata.add(new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(nameComponent)));
            metadata.add(new EntityData(3, EntityDataTypes.BOOLEAN, true)); // Custom name visible
        }
        
        // Añadir pose si está sentado
        if (npc.getPose() == NPC.Pose.SITTING) {
            metadata.add(new EntityData(6, EntityDataTypes.ENTITY_POSE, 
                com.github.retrooper.packetevents.protocol.entity.pose.EntityPose.SITTING));
        }
        
        if (!metadata.isEmpty()) {
            WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
                npc.getEntityId(),
                metadata
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
        }
        
        WrapperPlayServerEntityHeadLook headLookPacket = new WrapperPlayServerEntityHeadLook(
            npc.getEntityId(),
            loc.getYaw()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, headLookPacket);
    }

    private com.github.retrooper.packetevents.protocol.entity.type.EntityType convertBukkitEntityType(EntityType bukkitType) {
        return com.github.retrooper.packetevents.protocol.entity.type.EntityTypes.getByName(
            "minecraft:" + bukkitType.name().toLowerCase()
        );
    }

    public void updateNPCHeadRotation(Player player, NPC npc, float yaw, float pitch) {
        WrapperPlayServerEntityHeadLook headLookPacket = new WrapperPlayServerEntityHeadLook(
            npc.getEntityId(),
            yaw
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, headLookPacket);
        
        WrapperPlayServerEntityRotation rotationPacket = new WrapperPlayServerEntityRotation(
            npc.getEntityId(),
            yaw,
            pitch,
            false
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, rotationPacket);
    }

    public void updateNPCDisplayName(Player player, NPC npc) {
        List<EntityData> metadata = new ArrayList<>();
        
        if (npc.getDisplayName() != null) {
            String coloredName = ChatColor.translateAlternateColorCodes('&', npc.getDisplayName());
            Component nameComponent = LegacyComponentSerializer.legacySection().deserialize(coloredName);
            metadata.add(new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(nameComponent)));
            metadata.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
        } else {
            metadata.add(new EntityData(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.empty()));
            metadata.add(new EntityData(3, EntityDataTypes.BOOLEAN, false));
        }
        
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(
            npc.getEntityId(),
            metadata
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);
    }

    public void despawnNPC(Player player, NPC npc) {
        if (npc.getEntityType() == EntityType.PLAYER) {
            WrapperPlayServerPlayerInfoRemove removeInfoPacket = new WrapperPlayServerPlayerInfoRemove(
                List.of(npc.getUuid())
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, removeInfoPacket);
        }
        
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(
            npc.getEntityId()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);
    }
}
