package com.nyx.nyxnpcs.managers;

import com.nyx.nyxnpcs.npc.NPC;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    private final Map<UUID, NPC> selectedNPCs;

    public SelectionManager() {
        this.selectedNPCs = new HashMap<>();
    }

    public void selectNPC(Player player, NPC npc) {
        selectedNPCs.put(player.getUniqueId(), npc);
    }

    public void deselectNPC(Player player) {
        selectedNPCs.remove(player.getUniqueId());
    }

    public NPC getSelectedNPC(Player player) {
        return selectedNPCs.get(player.getUniqueId());
    }

    public boolean hasSelection(Player player) {
        return selectedNPCs.containsKey(player.getUniqueId());
    }
}
