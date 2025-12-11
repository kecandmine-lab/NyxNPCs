package com.nyx.nyxnpcs.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.nyx.nyxnpcs.NyxNPCs;
import com.nyx.nyxnpcs.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class InteractionListener extends PacketListenerAbstract implements Listener {

    private final NyxNPCs plugin;

    public InteractionListener(NyxNPCs plugin) {
        super(PacketListenerPriority.NORMAL);
        this.plugin = plugin;
        PacketEvents.getAPI().getEventManager().registerListener(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getNpcManager().showAllNPCsToPlayer(player);
        }, 20L);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            
            int entityId = wrapper.getEntityId();
            NPC npc = plugin.getNpcManager().getNPCByEntityId(entityId);
            
            if (npc != null) {
                Player player = (Player) event.getPlayer();
                
                WrapperPlayClientInteractEntity.InteractAction action = wrapper.getAction();
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    switch (action) {
                        case INTERACT:
                        case INTERACT_AT:
                            handleRightClick(player, npc);
                            break;
                        case ATTACK:
                            handleLeftClick(player, npc);
                            break;
                    }
                });
            }
        }
    }

    private void handleRightClick(Player player, NPC npc) {
        for (NPC.NPCAction action : npc.getActions()) {
            executeAction(player, action);
        }
    }

    private void handleLeftClick(Player player, NPC npc) {
        // No hace nada por defecto, solo si tiene acciones
        for (NPC.NPCAction action : npc.getActions()) {
            executeAction(player, action);
        }
    }

    private void executeAction(Player player, NPC.NPCAction action) {
        switch (action.getType()) {
            case MESSAGE:
                player.sendMessage(action.getValue().replace("&", "§"));
                break;
            case COMMAND:
                player.performCommand(action.getValue());
                break;
            case CONSOLE_COMMAND:
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    action.getValue().replace("%player%", player.getName()));
                break;
        }
    }
}
