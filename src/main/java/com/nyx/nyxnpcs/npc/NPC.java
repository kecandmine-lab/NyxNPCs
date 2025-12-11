package com.nyx.nyxnpcs.npc;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.*;

public class NPC {

    private final UUID uuid;
    private final int entityId;
    private String name;
    private String displayName; // Nombre visible con colores, null para ocultar
    private Location location;
    private String skinTexture;
    private String skinSignature;
    private List<NPCAction> actions;
    private EntityType entityType;
    private boolean lookAtPlayer;
    private Pose pose;

    public NPC(UUID uuid, int entityId, String name, Location location) {
        this.uuid = uuid;
        this.entityId = entityId;
        this.name = name;
        this.displayName = name; // Por defecto usa el mismo nombre
        this.location = location;
        this.actions = new ArrayList<>();
        this.entityType = EntityType.PLAYER;
        this.lookAtPlayer = false;
        this.pose = Pose.STANDING;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getSkinTexture() {
        return skinTexture;
    }

    public void setSkinTexture(String skinTexture) {
        this.skinTexture = skinTexture;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public void setSkinSignature(String skinSignature) {
        this.skinSignature = skinSignature;
    }

    public List<NPCAction> getActions() {
        return actions;
    }

    public void addAction(NPCAction action) {
        this.actions.add(action);
    }

    public void clearActions() {
        this.actions.clear();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public boolean isLookAtPlayer() {
        return lookAtPlayer;
    }

    public void setLookAtPlayer(boolean lookAtPlayer) {
        this.lookAtPlayer = lookAtPlayer;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public enum Pose {
        STANDING,
        SITTING
    }

    public static class NPCAction {
        private final ActionType type;
        private final String value;

        public NPCAction(ActionType type, String value) {
            this.type = type;
            this.value = value;
        }

        public ActionType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ActionType {
        MESSAGE,
        COMMAND,
        CONSOLE_COMMAND
    }
}
