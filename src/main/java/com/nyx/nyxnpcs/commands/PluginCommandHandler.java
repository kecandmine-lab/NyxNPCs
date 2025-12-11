package com.nyx.nyxnpcs.commands;

import com.nyx.nyxnpcs.NyxNPCs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PluginCommandHandler implements CommandExecutor, TabCompleter {

    private final NyxNPCs plugin;

    public PluginCommandHandler(NyxNPCs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "save":
                return handleSave(sender);
            case "reload":
                return handleReload(sender);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage("§cUnknown subcommand. Use /nyxnpcs help");
                return true;
        }
    }

    private boolean handleSave(CommandSender sender) {
        if (!sender.hasPermission("nyxnpcs.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        plugin.getDataManager().saveNPCs();
        sender.sendMessage("§7[§bNyxNPCs§7] §aAll NPCs have been saved successfully!");
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("nyxnpcs.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        sender.sendMessage("§7[§bNyxNPCs§7] §eReloading plugin...");
        
        // Guardar antes de recargar
        plugin.getDataManager().saveNPCs();
        
        // Remover todos los NPCs actuales
        plugin.getNpcManager().removeAllNPCs();
        
        // Recargar configuración
        plugin.reloadConfig();
        
        // Cargar NPCs de nuevo
        plugin.getDataManager().loadNPCs();
        
        sender.sendMessage("§7[§bNyxNPCs§7] §aPlugin reloaded successfully!");
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§7§m------------------§r §b§lNyxNPCs Admin §7§m------------------");
        sender.sendMessage("§b/nyxnpcs save §7- Save all NPCs to file");
        sender.sendMessage("§b/nyxnpcs reload §7- Reload the plugin and all NPCs");
        sender.sendMessage("§b/nyxnpcs help §7- Show this help message");
        sender.sendMessage("§7§m------------------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("save", "reload", "help"));
        }

        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}
