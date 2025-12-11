package com.nyx.nyxnpcs.commands;

import com.nyx.nyxnpcs.NyxNPCs;
import com.nyx.nyxnpcs.npc.NPC;
import com.nyx.nyxnpcs.utils.NPCSelector;
import com.nyx.nyxnpcs.utils.SkinFetcher;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final NyxNPCs plugin;

    public CommandHandler(NyxNPCs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreate(player, args);
            case "remove":
            case "delete":
                return handleRemove(player, args);
            case "list":
                return handleList(player);
            case "select":
                return handleSelect(player);
            case "deselect":
                return handleDeselect(player);
            case "rename":
                return handleRename(player, args);
            case "displayname":
            case "name":
                return handleDisplayName(player, args);
            case "skin":
                return handleSkin(player, args);
            case "type":
                return handleType(player, args);
            case "pose":
                return handlePose(player, args);
            case "move_here":
            case "movehere":
                return handleMoveHere(player, args);
            case "center":
                return handleCenter(player, args);
            case "teleport":
            case "tp":
                return handleTeleport(player, args);
            case "freelook":
            case "look":
                return handleFreelook(player, args);
            case "action":
                return handleAction(player, args);
            case "save":
                return handleSave(player);
            case "help":
                sendHelp(player);
                return true;
            default:
                player.sendMessage("§cUnknown subcommand. Use /npc help");
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.create")) {
            player.sendMessage("§cYou don't have permission to create NPCs.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc create <name>");
            return true;
        }

        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Location location = player.getLocation();

        NPC npc = plugin.getNpcManager().createNPC(name, location);
        player.sendMessage("§7[§bNPC§7] §aNPC created: §f" + name + " §7(ID: " + npc.getEntityId() + ")");

        plugin.getDataManager().saveNPCs();
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.remove")) {
            player.sendMessage("§cYou don't have permission to remove NPCs.");
            return true;
        }

        NPC npc = null;
        
        if (args.length >= 2) {
            try {
                int entityId = Integer.parseInt(args[1]);
                npc = plugin.getNpcManager().getNPCByEntityId(entityId);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid entity ID.");
                return true;
            }
        } else {
            npc = plugin.getSelectionManager().getSelectedNPC(player);
        }

        if (npc == null) {
            player.sendMessage("§cNo NPC selected or found. Use /npc select or provide an ID.");
            return true;
        }

        String npcName = npc.getName();
        plugin.getNpcManager().removeNPC(npc.getEntityId());
        plugin.getSelectionManager().deselectNPC(player);
        player.sendMessage("§7[§bNPC§7] §cRemoved NPC: §f" + npcName);
        plugin.getDataManager().saveNPCs();

        return true;
    }

    private boolean handleList(Player player) {
        if (!player.hasPermission("nyxnpcs.list")) {
            player.sendMessage("§cYou don't have permission to list NPCs.");
            return true;
        }

        Collection<NPC> npcs = plugin.getNpcManager().getAllNPCs();

        if (npcs.isEmpty()) {
            player.sendMessage("§7[§bNPC§7] §eThere are no NPCs.");
            return true;
        }

        player.sendMessage("§7§m--------------------§r §b§lNPCs §7§m--------------------");
        for (NPC npc : npcs) {
            Location loc = npc.getLocation();
            player.sendMessage("§7ID: §f" + npc.getEntityId() + 
                             " §7| §fName: §b" + npc.getName() + 
                             " §7| §fWorld: §e" + loc.getWorld().getName() +
                             " §7| §fPos: §e" + (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ());
        }
        player.sendMessage("§7§m------------------------------------------------");

        return true;
    }

    private boolean handleSelect(Player player) {
        if (!player.hasPermission("nyxnpcs.use")) {
            player.sendMessage("§cYou don't have permission.");
            return true;
        }

        NPC npc = NPCSelector.getTargetedNPC(player, plugin.getNpcManager().getAllNPCs(), 10.0);
        
        if (npc == null) {
            player.sendMessage("§7[§bNPC§7] §cNo NPC found. Look at an NPC and try again.");
            return true;
        }

        plugin.getSelectionManager().selectNPC(player, npc);
        player.sendMessage("§7[§bNPC§7] §aSelected NPC: §f" + npc.getName() + " §7(ID: " + npc.getEntityId() + ")");
        return true;
    }

    private boolean handleDeselect(Player player) {
        if (!player.hasPermission("nyxnpcs.use")) {
            player.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (!plugin.getSelectionManager().hasSelection(player)) {
            player.sendMessage("§7[§bNPC§7] §cYou don't have an NPC selected.");
            return true;
        }

        plugin.getSelectionManager().deselectNPC(player);
        player.sendMessage("§7[§bNPC§7] §eNPC deselected.");
        return true;
    }

    private boolean handleSkin(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.skin")) {
            player.sendMessage("§cYou don't have permission to change NPC skins.");
            return true;
        }

        NPC npc = null;
        String skinName;
        
        if (args.length >= 3) {
            try {
                int entityId = Integer.parseInt(args[1]);
                npc = plugin.getNpcManager().getNPCByEntityId(entityId);
                skinName = args[2];
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid entity ID.");
                return true;
            }
        } else if (args.length >= 2) {
            npc = plugin.getSelectionManager().getSelectedNPC(player);
            skinName = args[1];
        } else {
            player.sendMessage("§cUsage: /npc skin [id] <playerName>");
            return true;
        }

        if (npc == null) {
            player.sendMessage("§cNo NPC selected or found. Use /npc select first.");
            return true;
        }

        if (npc.getEntityType() != EntityType.PLAYER) {
            player.sendMessage("§cSkins can only be applied to player NPCs.");
            return true;
        }

        player.sendMessage("§7[§bNPC§7] §eFetching skin for: §f" + skinName + "§e...");

        NPC finalNpc = npc;
        SkinFetcher.fetchSkin(skinName, (texture, signature) -> {
            if (texture != null && signature != null) {
                plugin.getNpcManager().updateNPCSkin(finalNpc, texture, signature);
                player.sendMessage("§7[§bNPC§7] §aSkin updated for NPC: §f" + finalNpc.getName());
                plugin.getDataManager().saveNPCs();
            } else {
                player.sendMessage("§7[§bNPC§7] §cFailed to fetch skin for: §f" + skinName);
            }
        });

        return true;
    }

    private boolean handleType(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.use")) {
            player.sendMessage("§cYou don't have permission.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc type <entityType>");
            player.sendMessage("§eExamples: PLAYER, ZOMBIE, SKELETON, COW, PIG, VILLAGER");
            return true;
        }

        NPC npc = plugin.getSelectionManager().getSelectedNPC(player);
        if (npc == null) {
            player.sendMessage("§cNo NPC selected. Use /npc select first.");
            return true;
        }

        try {
            EntityType type = EntityType.valueOf(args[1].toUpperCase());
            
            if (type.isAlive()) {
                npc.setEntityType(type);
                plugin.getNpcManager().despawnNPCForAll(npc);
                plugin.getNpcManager().spawnNPCForAll(npc);
                player.sendMessage("§7[§bNPC§7] §aNPC type changed to: §f" + type.name());
                plugin.getDataManager().saveNPCs();
            } else {
                player.sendMessage("§cInvalid entity type. Must be a living entity.");
                player.sendMessage("§eExamples: PLAYER, ZOMBIE, SKELETON, COW, PIG, VILLAGER");
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid entity type. Use /npc type for examples.");
        }

        return true;
    }

    private boolean handleFreelook(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.use")) {
            player.sendMessage("§cYou don't have permission.");
            return true;
        }

        NPC npc = plugin.getSelectionManager().getSelectedNPC(player);
        if (npc == null) {
            player.sendMessage("§cNo NPC selected. Use /npc select first.");
            return true;
        }

        if (args.length < 2) {
            boolean currentState = npc.isLookAtPlayer();
            npc.setLookAtPlayer(!currentState);
            player.sendMessage("§7[§bNPC§7] §eFreelook " + (npc.isLookAtPlayer() ? "§aenabled" : "§cdisabled") + "§e for: §f" + npc.getName());
        } else {
            boolean state = args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("on");
            npc.setLookAtPlayer(state);
            player.sendMessage("§7[§bNPC§7] §eFreelook " + (state ? "§aenabled" : "§cdisabled") + "§e for: §f" + npc.getName());
        }

        plugin.getDataManager().saveNPCs();
        return true;
    }

    private boolean handleAction(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.action")) {
            player.sendMessage("§cYou don't have permission to set NPC actions.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc action <add|clear|list> [type] [value]");
            player.sendMessage("§eOr: /npc action [id] <add|clear|list> [type] [value]");
            player.sendMessage("§eTypes: message, command, console");
            return true;
        }

        NPC npc = null;
        String subAction;
        int argOffset;

        try {
            int entityId = Integer.parseInt(args[1]);
            npc = plugin.getNpcManager().getNPCByEntityId(entityId);
            if (args.length < 3) {
                player.sendMessage("§cUsage: /npc action <id> <add|clear|list> [type] [value]");
                return true;
            }
            subAction = args[2].toLowerCase();
            argOffset = 3;
        } catch (NumberFormatException e) {
            npc = plugin.getSelectionManager().getSelectedNPC(player);
            subAction = args[1].toLowerCase();
            argOffset = 2;
        }

        if (npc == null) {
            player.sendMessage("§cNo NPC selected or found. Use /npc select first.");
            return true;
        }

        switch (subAction) {
            case "add":
                if (args.length < argOffset + 2) {
                    player.sendMessage("§cUsage: /npc action add <type> <value>");
                    return true;
                }

                String type = args[argOffset].toLowerCase();
                String value = String.join(" ", Arrays.copyOfRange(args, argOffset + 1, args.length));

                    NPC.ActionType actionType;
                    switch (type) {
                        case "message":
                            actionType = NPC.ActionType.MESSAGE;
                            break;
                        case "command":
                            actionType = NPC.ActionType.COMMAND;
                            break;
                        case "console":
                            actionType = NPC.ActionType.CONSOLE_COMMAND;
                            break;
                        default:
                            player.sendMessage("§cInvalid action type. Use: message, command, console");
                            return true;
                    }

                    npc.addAction(new NPC.NPCAction(actionType, value));
                    player.sendMessage("§7[§bNPC§7] §aAction added to NPC: §f" + npc.getName());
                    plugin.getDataManager().saveNPCs();
                    break;

                case "clear":
                    npc.clearActions();
                    player.sendMessage("§7[§bNPC§7] §aActions cleared for NPC: §f" + npc.getName());
                    plugin.getDataManager().saveNPCs();
                    break;

                case "list":
                    player.sendMessage("§7[§bNPC§7] §eActions for: §f" + npc.getName());
                    if (npc.getActions().isEmpty()) {
                        player.sendMessage("§7  No actions set.");
                    } else {
                        int i = 1;
                        for (NPC.NPCAction action : npc.getActions()) {
                            player.sendMessage("§7  " + i + ". §e" + action.getType() + "§7: §f" + action.getValue());
                            i++;
                        }
                    }
                    break;

                default:
                    player.sendMessage("§cUnknown sub-action. Use: add, clear, list");
                    break;
            }

        return true;
    }
    private boolean handleRename(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.rename")) {
            player.sendMessage("§cYou don't have permission to rename NPCs.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc rename <new_name>");
            return true;
        }

        NPC npc = plugin.getSelectionManager().getSelectedNPC(player);
        
        if (npc == null) {
            player.sendMessage("§cNo NPC selected. Use /npc select first.");
            return true;
        }

        String oldName = npc.getName();
        String newName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        npc.setName(newName);
        
        // Recargar NPC para aplicar cambios (solo para NPCs tipo PLAYER)
        if (npc.getEntityType() == org.bukkit.entity.EntityType.PLAYER) {
            plugin.getNpcManager().despawnNPCForAll(npc);
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getNpcManager().spawnNPCForAll(npc);
            }, 5L);
        }
        
        player.sendMessage("§7[§bNPC§7] §aNPC renamed from §f" + oldName + " §ato §f" + newName);
        plugin.getDataManager().saveNPCs();
        return true;
    }

    private boolean handleDisplayName(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.displayname")) {
            player.sendMessage("§cYou don't have permission to change NPC display names.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc displayname <text|none>");
            player.sendMessage("§7Use & for color codes. Example: /npc displayname &aGreen &bName");
            return true;
        }

        NPC npc = plugin.getSelectionManager().getSelectedNPC(player);
        
        if (npc == null) {
            player.sendMessage("§cNo NPC selected. Use /npc select first.");
            return true;
        }

        String displayName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        if (displayName.equalsIgnoreCase("none")) {
            npc.setDisplayName(null);
            player.sendMessage("§7[§bNPC§7] §aDisplay name removed from NPC: §f" + npc.getName());
        } else {
            npc.setDisplayName(displayName);
            player.sendMessage("§7[§bNPC§7] §aDisplay name set for NPC: §f" + npc.getName());
        }
        
        // Recargar NPC para aplicar cambios en el nombre
        plugin.getNpcManager().despawnNPCForAll(npc);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getNpcManager().spawnNPCForAll(npc);
        }, 5L);
        
        plugin.getDataManager().saveNPCs();
        return true;
    }

    private boolean handlePose(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.pose")) {
            player.sendMessage("§cYou don't have permission to change NPC poses.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /npc pose <sitting|standing>");
            return true;
        }

        NPC npc = plugin.getSelectionManager().getSelectedNPC(player);
        
        if (npc == null) {
            player.sendMessage("§cNo NPC selected. Use /npc select first.");
            return true;
        }

        String poseStr = args[1].toUpperCase();
        NPC.Pose pose;
        
        try {
            pose = NPC.Pose.valueOf(poseStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cInvalid pose. Use: sitting, standing");
            return true;
        }
        
        npc.setPose(pose);
        player.sendMessage("§7[§bNPC§7] §aPose set to: §f" + pose.name().toLowerCase());
        
        // Recargar NPC para aplicar cambios
        plugin.getNpcManager().despawnNPCForAll(npc);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getNpcManager().spawnNPCForAll(npc);
        }, 5L);
        
        plugin.getDataManager().saveNPCs();
        return true;
    }

    private boolean handleMoveHere(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.movehere")) {
            player.sendMessage("§cYou don't have permission to move NPCs.");
            return true;
        }

        NPC npc = plugin.getSelectionManager().getSelectedNPC(player);
        
        if (npc == null) {
            player.sendMessage("§cNo NPC selected. Use /npc select first.");
            return true;
        }

        Location newLoc = player.getLocation();
        npc.setLocation(newLoc);
        
        player.sendMessage("§7[§bNPC§7] §aNPC moved to your location!");
        
        // Recargar NPC para aplicar cambios
        plugin.getNpcManager().despawnNPCForAll(npc);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getNpcManager().spawnNPCForAll(npc);
        }, 5L);
        
        plugin.getDataManager().saveNPCs();
        return true;
    }

    private boolean handleCenter(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.center")) {
            player.sendMessage("§cYou don't have permission to center NPCs.");
            return true;
        }

        NPC npc = plugin.getSelectionManager().getSelectedNPC(player);
        
        if (npc == null) {
            player.sendMessage("§cNo NPC selected. Use /npc select first.");
            return true;
        }

        Location loc = npc.getLocation();
        Location centered = new Location(
            loc.getWorld(),
            loc.getBlockX() + 0.5,
            loc.getBlockY(),
            loc.getBlockZ() + 0.5,
            loc.getYaw(),
            loc.getPitch()
        );
        
        npc.setLocation(centered);
        player.sendMessage("§7[§bNPC§7] §aNPC centered at block!");
        
        // Recargar NPC para aplicar cambios
        plugin.getNpcManager().despawnNPCForAll(npc);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getNpcManager().spawnNPCForAll(npc);
        }, 5L);
        
        plugin.getDataManager().saveNPCs();
        return true;
    }

    private boolean handleTeleport(Player player, String[] args) {
        if (!player.hasPermission("nyxnpcs.teleport")) {
            player.sendMessage("§cYou don't have permission to teleport to NPCs.");
            return true;
        }

        NPC npc = plugin.getSelectionManager().getSelectedNPC(player);
        
        if (npc == null) {
            player.sendMessage("§cNo NPC selected. Use /npc select first.");
            return true;
        }

        player.teleport(npc.getLocation());
        player.sendMessage("§7[§bNPC§7] §aTeleported to NPC: §f" + npc.getName());
        return true;
    }

    private boolean handleSave(Player player) {
        if (!player.hasPermission("nyxnpcs.save")) {
            player.sendMessage("§cYou don't have permission to save NPCs.");
            return true;
        }

        plugin.getDataManager().saveNPCs();
        player.sendMessage("§7[§bNPC§7] §aAll NPCs have been saved!");
        return true;
    }
    private void sendHelp(Player player) {
        player.sendMessage("§7§m--------------------§r §b§lNyxNPCs §7§m--------------------");
        player.sendMessage("§b/npc create <name> §7- Create an NPC");
        player.sendMessage("§b/npc select §7- Select NPC you're looking at");
        player.sendMessage("§b/npc deselect §7- Deselect current NPC");
        player.sendMessage("§b/npc rename <name> §7- Rename selected NPC");
        player.sendMessage("§b/npc displayname <text|none> §7- Set display name with colors");
        player.sendMessage("§b/npc remove [id] §7- Remove selected or specified NPC");
        player.sendMessage("§b/npc list §7- List all NPCs");
        player.sendMessage("§b/npc type <entity> §7- Change NPC entity type");
        player.sendMessage("§b/npc pose <sitting|standing> §7- Change NPC pose");
        player.sendMessage("§b/npc move_here §7- Move NPC to your location");
        player.sendMessage("§b/npc center §7- Center NPC on block");
        player.sendMessage("§b/npc teleport §7- Teleport to NPC");
        player.sendMessage("§b/npc skin [id] <name> §7- Change NPC skin (player only)");
        player.sendMessage("§b/npc freelook [on|off] §7- Toggle NPC looking at players");
        player.sendMessage("§b/npc action add <type> <value> §7- Add action");
        player.sendMessage("§b/npc action clear §7- Clear all actions");
        player.sendMessage("§b/npc action list §7- List actions");
        player.sendMessage("§b/npc save §7- Save all NPCs to file");
        player.sendMessage("§7§m------------------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "select", "deselect", "rename", "displayname", "remove", "list", "type", "pose", "move_here", "center", "teleport", "skin", "freelook", "action", "save", "help"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("type")) {
                completions.addAll(Arrays.asList("PLAYER", "ZOMBIE", "SKELETON", "CREEPER", "VILLAGER", "COW", "PIG", "SHEEP"));
            } else if (args[0].equalsIgnoreCase("pose")) {
                completions.addAll(Arrays.asList("sitting", "standing"));
            } else if (args[0].equalsIgnoreCase("freelook")) {
                completions.addAll(Arrays.asList("on", "off", "true", "false"));
            } else if (args[0].equalsIgnoreCase("action")) {
                completions.addAll(Arrays.asList("add", "clear", "list"));
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("action") && args[1].equalsIgnoreCase("add")) {
            completions.addAll(Arrays.asList("message", "command", "console"));
        }

        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
}
