package com.nyx.nyxnpcs.listeners;

import com.nyx.nyxnpcs.NyxNPCs;
import com.nyx.nyxnpcs.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FreelookTask extends BukkitRunnable {

    private final NyxNPCs plugin;

    public FreelookTask(NyxNPCs plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (NPC npc : plugin.getNpcManager().getAllNPCs()) {
            if (!npc.isLookAtPlayer()) continue;

            Location npcLoc = npc.getLocation();
            Player closestPlayer = null;
            double closestDistance = 10.0;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getWorld().equals(npcLoc.getWorld())) continue;

                double distance = player.getLocation().distance(npcLoc);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = player;
                }
            }

            if (closestPlayer != null) {
                Location playerLoc = closestPlayer.getEyeLocation();
                Location npcEyeLoc = npcLoc.clone().add(0, 1.62, 0);
                Vector direction = playerLoc.toVector().subtract(npcEyeLoc.toVector());
                Location lookAt = npcEyeLoc.clone();
                lookAt.setDirection(direction);

                float yaw = lookAt.getYaw();
                float pitch = lookAt.getPitch();
                
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getWorld().equals(npcLoc.getWorld())) {
                        plugin.getNpcManager().getPacketHandler().updateNPCHeadRotation(onlinePlayer, npc, yaw, pitch);
                    }
                }
            }
        }
    }
}
