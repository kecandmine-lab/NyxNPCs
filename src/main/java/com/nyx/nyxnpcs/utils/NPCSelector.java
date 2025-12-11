package com.nyx.nyxnpcs.utils;

import com.nyx.nyxnpcs.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public class NPCSelector {

    public static NPC getTargetedNPC(Player player, Collection<NPC> npcs, double maxDistance) {
        Vector direction = player.getEyeLocation().getDirection();
        Vector start = player.getEyeLocation().toVector();
        
        NPC closestNPC = null;
        double closestDistance = maxDistance;
        
        for (NPC npc : npcs) {
            Vector npcPos = npc.getLocation().toVector().add(new Vector(0, 1, 0));
            Vector toNPC = npcPos.clone().subtract(start);
            
            double distance = toNPC.length();
            if (distance > maxDistance) continue;
            
            Vector projection = direction.clone().multiply(toNPC.dot(direction));
            double perpendicularDistance = toNPC.clone().subtract(projection).length();
            
            if (perpendicularDistance < 0.5 && distance < closestDistance) {
                closestDistance = distance;
                closestNPC = npc;
            }
        }
        
        return closestNPC;
    }
}
