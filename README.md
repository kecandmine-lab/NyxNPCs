# NyxNPCs - Advanced NPC Plugin

A powerful and feature-rich NPC plugin for Paper/Spigot 1.21+ using PacketEvents for optimal performance.

## Features

- 🎭 **Multiple Entity Types** - Create NPCs as players, zombies, villagers, animals, and more
- 👀 **Freelook System** - NPCs can look at nearby players with full 3D rotation
- 🎨 **Custom Display Names** - Set colored names above NPCs or hide them completely
- 💺 **Pose System** - Make NPCs sit or stand
- 🔄 **Selection System** - Select NPCs by looking at them (no IDs needed)
- 🎯 **Click Actions** - Execute commands, messages, or console commands on NPC interaction
- 🌐 **Persistent Storage** - NPCs save automatically and reload on server restart
- 🎨 **Custom Skins** - Apply any Minecraft player skin to NPCs
- 📍 **Teleportation** - Move NPCs or teleport to them easily

## Build Instructions

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Paper/Spigot 1.21+

### Building
```bash
mvnw.cmd clean package
```

The compiled JAR will be in `target/NyxNPCs-1.0.0.jar`

## Installation
1. Place the JAR in your server's `plugins` folder
2. Start/restart the server
3. NPCs are stored in `plugins/NyxNPCs/npcs.json`

## Commands

### Basic Commands
- `/npc create <name>` - Create an NPC at your location
- `/npc select` - Select the NPC you're looking at (raycast selection)
- `/npc deselect` - Deselect the current NPC
- `/npc remove` - Remove selected NPC
- `/npc list` - List all NPCs with their details

### NPC Appearance
- `/npc rename <name>` - Change the NPC's internal name
- `/npc displayname <text>` - Set colored display name above NPC (use & for colors)
- `/npc displayname none` - Hide the display name completely
- `/npc type <entity>` - Change NPC entity type (PLAYER, ZOMBIE, SKELETON, COW, PIG, VILLAGER, etc.)
- `/npc skin <playerName>` - Change NPC skin (only for PLAYER type NPCs)
- `/npc pose <sitting|standing>` - Change NPC pose

### NPC Position
- `/npc move_here` - Move selected NPC to your location
- `/npc center` - Center NPC on the current block (X.5, Y, Z.5)
- `/npc teleport` (or `/npc tp`) - Teleport to selected NPC

### NPC Behavior
- `/npc freelook <on|off>` - Toggle NPC looking at nearby players (10 block radius)

### Actions (Click Interactions)
- `/npc action add message <text>` - Send message to player (use & for colors)
- `/npc action add command <cmd>` - Execute command as player
- `/npc action add console <cmd>` - Execute command from console (use %player% for player name)
- `/npc action clear` - Clear all actions from selected NPC
- `/npc action list` - List all actions of selected NPC

### Data Management
- `/npc save` - Manually save all NPCs to file
- `/nyxnpcs save` - Admin command to save plugin data
- `/nyxnpcs reload` - Reload plugin and all NPCs
- `/nyxnpcs help` - Show admin help

## Permissions

### Basic Permissions
- `nyxnpcs.*` - All permissions (default: op)
- `nyxnpcs.use` - Basic NPC usage (default: op)
- `nyxnpcs.create` - Create NPCs (default: op)
- `nyxnpcs.remove` - Remove NPCs (default: op)
- `nyxnpcs.list` - List NPCs (default: op)

### Advanced Permissions
- `nyxnpcs.rename` - Rename NPCs (default: op)
- `nyxnpcs.displayname` - Change display names (default: op)
- `nyxnpcs.skin` - Change skins (default: op)
- `nyxnpcs.action` - Manage actions (default: op)
- `nyxnpcs.pose` - Change poses (default: op)
- `nyxnpcs.movehere` - Move NPCs (default: op)
- `nyxnpcs.center` - Center NPCs (default: op)
- `nyxnpcs.teleport` - Teleport to NPCs (default: op)
- `nyxnpcs.save` - Save NPCs manually (default: op)
- `nyxnpcs.admin` - Admin commands (save/reload) (default: op)

## Detailed Features

### NPC Selection System
Instead of using IDs, select NPCs by looking at them:
1. Look at the NPC you want to edit
2. Use `/npc select`
3. All subsequent commands will apply to this NPC
4. Use `/npc deselect` when done

### Entity Types
NPCs can be any living Minecraft entity:
- **PLAYER** - Player with custom skins and names
- **ZOMBIE, SKELETON, CREEPER, SPIDER** - Hostile mobs
- **COW, PIG, SHEEP, CHICKEN, HORSE** - Passive animals
- **VILLAGER, WANDERING_TRADER, IRON_GOLEM** - Village NPCs
- **WOLF, CAT, PARROT** - Tameable animals
- And all other living entities!

### Display Names with Colors
Use Minecraft color codes with `&`:
- `&0-9, a-f` - Colors (e.g., `&a` = green, `&c` = red)
- `&l` - Bold
- `&o` - Italic
- `&n` - Underline
- `&m` - Strikethrough
- `&r` - Reset

Example: `/npc displayname &6&l[&e⭐&6&l] &aShop &7(Click)`

### Freelook System
When enabled:
- NPCs track the nearest player within 10 blocks
- Full 3D head rotation (yaw and pitch)
- Updates every 0.25 seconds (5 ticks)
- Works with all entity types

### Pose System
Available poses:
- **SITTING** - NPC sits down (works with all entity types)
- **STANDING** - NPC stands normally (default)

### Actions System
NPCs can execute multiple actions when clicked (right-click):
- **MESSAGE**: Send colored messages to the player
- **COMMAND**: Execute command as the player (without /)
- **CONSOLE_COMMAND**: Execute command from console with `%player%` placeholder

Left-click does nothing by default (no spam messages).

### Data Persistence
- NPCs auto-save when server stops
- Manual save with `/npc save` or `/nyxnpcs save`
- All data stored in `plugins/NyxNPCs/npcs.json`
- Includes: location, type, skin, actions, pose, display name, freelook state

## Examples

### Create a Shop NPC
```
/npc create Shop
/npc select
/npc displayname &6&l[&e⭐&6&l] &aShop &7(Click)
/npc skin Notch
/npc freelook on
/npc action add message &aWelcome to the shop!
/npc action add console eco give %player% 100
```

### Create a Sitting Guard NPC
```
/npc create Guard
/npc select
/npc type ZOMBIE
/npc displayname &c&lGuard
/npc pose sitting
/npc freelook on
```

### Create an Information NPC
```
/npc create InfoBot
/npc select
/npc displayname &b&lInformation
/npc center
/npc action add message &e&lServer Rules:
/npc action add message &71. Be respectful
/npc action add message &72. No griefing
/npc action add message &73. Have fun!
```

### Create a Pet Animal NPC
```
/npc create Pet
/npc select
/npc type PIG
/npc displayname &d&lOinky
/npc freelook on
/npc action add message &d*Oink oink*
```

### Create a Quest Giver
```
/npc create Questmaster
/npc select
/npc skin Steve
/npc displayname &6&lQuest Master
/npc pose sitting
/npc action add message &eYou have been given a quest!
/npc action add console give %player% diamond_sword 1
/npc action add command spawn
```

## Technical Details

- **API**: Paper API 1.21.3
- **Packet Library**: PacketEvents 2.7.0
- **Serialization**: Gson 2.10.1
- **Java Version**: 21+
- **Selection Range**: 5 blocks (raycast)
- **Freelook Range**: 10 blocks
- **Update Rate**: 5 ticks (0.25s)

## Support

For issues, feature requests, or contributions, please visit the GitHub repository.

## License

This plugin is provided as-is for use on Minecraft servers.

