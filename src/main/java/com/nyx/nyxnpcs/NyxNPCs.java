package com.nyx.nyxnpcs;

import com.github.retrooper.packetevents.PacketEvents;
import com.nyx.nyxnpcs.commands.CommandHandler;
import com.nyx.nyxnpcs.listeners.InteractionListener;
import com.nyx.nyxnpcs.listeners.FreelookTask;
import com.nyx.nyxnpcs.managers.DataManager;
import com.nyx.nyxnpcs.managers.NPCManager;
import com.nyx.nyxnpcs.managers.SelectionManager;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class NyxNPCs extends JavaPlugin {

    private static NyxNPCs instance;
    private NPCManager npcManager;
    private DataManager dataManager;
    private SelectionManager selectionManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        PacketEvents.getAPI().init();
        
        this.dataManager = new DataManager(this);
        this.npcManager = new NPCManager(this);
        this.selectionManager = new SelectionManager();
        
        dataManager.loadNPCs();
        
        getCommand("npc").setExecutor(new CommandHandler(this));
        getCommand("nyxnpcs").setExecutor(new com.nyx.nyxnpcs.commands.PluginCommandHandler(this));
        
        getServer().getPluginManager().registerEvents(new InteractionListener(this), this);
        
        new FreelookTask(this).runTaskTimer(this, 20L, 5L);
        
        getLogger().info("NyxNPCs has been enabled!");
    }

    @Override
    public void onDisable() {
        // IMPORTANTE: Guardar ANTES de remover los NPCs
        if (dataManager != null) {
            dataManager.saveNPCs();
        }
        
        if (npcManager != null) {
            npcManager.removeAllNPCs();
        }
        
        PacketEvents.getAPI().terminate();
        
        getLogger().info("NyxNPCs has been disabled!");
    }

    public static NyxNPCs getInstance() {
        return instance;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
}
