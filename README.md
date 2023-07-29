Noxesium
---
A fabric mod with feature additions, bugfixes, and performance improvements. It allows servers to offer a better experience to clients through access to additional features beyond vanilla limitations.

Server developers are welcome to submit additional patches they need, however all non-performance changes must be fully optional to use. No client-side configuration is available as behaviour should be decided by the server.

# Features

### Additions

- Sends information about GUI scale to connected servers to allow better server-side UI design
- Allow servers to disable riptide trident collisions
- Allow servers to define a global CanDestroy and CanPlaceOn tag that applies to all items
- Allow servers to show player heads in chat messages
- Allow servers to prevent picking up items in GUIs
- Allow servers to prevent camera movement temporarily
- Allow servers to disable vanilla Minecraft music

### Performance

- Optimized Beacon block entity rendering (disabled when using Iris Shaders)

### Bugfixes

- Always display head layer in TAB menu
- Change teleport packets to teleport vehicle passengers to their vehicle
- Fixes lighting issues with moving piston walls
- Music properly resumes playing when setting the volume to 0% and back up

### MCC Island-specific Features

- Adds a setting to show player heads in UIs

# For developers

## Player Heads

Noxesium supports rendering player heads into text through a custom skull component. The details of which can be found by looking into the contents
of [the ComponentSerializerMixin file](https://github.com/Noxcrew/noxesium/blob/main/src/main/java/com/noxcrew/noxesium/mixin/client/component/ComponentSerializerMixin.java). However, since servers are often not able to introduce a new
component type the recommended syntax to use is an optional syntax based on the [translated text component](https://minecraft.fandom.com/wiki/Raw_JSON_text_format#Translated_Text). With this syntax, if a player doesn't have Noxesium
installed, the provided fallback will render instead. This allows servers to easily support Noxesium.

### Format

You have to place a string into the translate section of the JSON object prefixed with `%nox_uuid%` and followed by a comma-seperated list of properties:

| Field Name  | Field Type | Notes                                                |
|-------------|------------|------------------------------------------------------|
| Player UUID | UUID       | UUID of a player, whose skull is displayed.          |
| Grayscale   | Boolean    | If true, the skull will be gray.                     |
| Advance     | Integer    | Moves the skull horizontally (doesn't work in chat). |
| Ascent      | Integer    | Moves the skull vertically.                          |
| Scale       | Float      | Scales the skull. The anchor is top-left.            |

Alternatively, if you cannot provide the uuid and wish to provide a texture directly you can use the prefix `%nox_raw%` and replace the UUID with the following field:

| Field Name   | Field Type | Notes                                   |
|--------------|------------|-----------------------------------------|
| Texture Data | String     | Raw texture data for a skin to display. |

### Example

```
%nox_uuid%3e7a89ee-c4e2-4392-a317-444b861b0794,false,0,0,1.0
%nox_raw%ewogICJ0aW1lc3RhbXAiIDogMTYxMjA1MTQxNDA4NywKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTkyNjZiYThiY2Q4ZmE2NGE0NjgyOGY1NjEwZDk5MGE1MzEzMzVmNjQzZWYzOWYzZDA1ZDdmZTFjMWVkYjg4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=,false,0,0,1.0
```

### Usages

**Minecraft Commands**

```
/tellraw @a {"translate":"%nox_uuid%3e7a89ee-c4e2-4392-a317-444b861b0794,false,0,0,1.0","fallback":"This is shown for non-Noxesium clients"}
/tellraw @a {"translate":"%nox_raw%ewogICJ0aW1lc3RhbXAiIDogMTYxMjA1MTQxNDA4NywKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTkyNjZiYThiY2Q4ZmE2NGE0NjgyOGY1NjEwZDk5MGE1MzEzMzVmNjQzZWYzOWYzZDA1ZDdmZTFjMWVkYjg4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=,false,0,0,1.0","fallback":"This is shown for non-Noxesium clients"}
```

**Paper Plugin**

```java
player.sendMessage(Component.translatable("%nox_uuid%3e7a89ee-c4e2-4392-a317-444b861b0794,false,0,0,1.0","This is shown for non-Noxesium clients"));
```

or

```java
player.sendMessage(Component.translatable("%nox_raw%ewogICJ0aW1lc3RhbXAiIDogMTYxMjA1MTQxNDA4NywKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTkyNjZiYThiY2Q4ZmE2NGE0NjgyOGY1NjEwZDk5MGE1MzEzMzVmNjQzZWYzOWYzZDA1ZDdmZTFjMWVkYjg4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=,false,0,0,1.0","This is shown for non-Noxesium clients"));
```

## Message Channels

Noxesium communicates with the server over the plugin messaging channel, but through the use of Fabric Packets. This implementation means packets are defined in Noxesium's code as they would be in Minecrafts source code. Clientbound and
serverbound packets are implemented separately since both only need to be handled on the client-side. Noxesium sends and receives all packets on the `noxesium-v1` namespace. This version identifier is not intended to change, it has changed
once for Noxesium 1.0.0 but should not change again unless the entire packet system is redone. Instead, each packet starts with its own version integer. This version integer can be used to change individual packets. As Noxesium is intended
to be used between clients on servers on both different Minecraft versions and different Noxesium versions it is necessary to strongly support backwards compatibility. For this purpose the version integer in each packet can be
individually tweaked. Packets will both attempt to send in an older format and parse from an older format if the server is outdated. It is left up to the developer to build similar compatibility on the server-side.

If you are interested in understanding what packets Noxesium can receive and which it can send, feel free to look through the `network.clientbound` and `network.serverbound` packages. Packets are documented in their respective classes and
should offer insights into their capabilities through the variable names and constructor arguments.

## Server Rules

Server Rules are a special system similar to Game Rules but able to be modified whenever desired. Server Rule settings are cleared whenever a player disconnects from a server. These values allow a server to affect the client's state easily.
Below is a list of every server rule currently available and their data format. While server rules have packets for setting each individual server rule can decide how the packet is decoded. As such, a detailed breakdown is provided for each
individual rule and how it interprets the incoming packet.

**Disable Auto Spin Attack**. Disables colliding with other entities while riptiding. This also prevents the spin attack from dealing any damage to entities moved through.

| Field Name | Field Type | Notes               |
|------------|------------|---------------------|
| Index      | VarInt     | 0                   |
| Value      | Boolean    | `false` by default. |

**Global Can Place On**. Makes client in adventure able to build on blocks provided. Similar to the per-item Place On but applies to all items including the empty hand.

| Field Name | Field Type     | Notes                                                                                                         |
|------------|----------------|---------------------------------------------------------------------------------------------------------------|
| Index      | VarInt         | 1                                                                                                             |
| List       | List of Blocks | Example: `minecraft:grass_block` will make the client think it can place blocks on grass blocks in adventure. |

**Global Can Destroy**. Makes client in adventure able to break blocks provided. Similar to the per-item Can Destroy but applies to all items including the empty hand.

| Field Name | Field Type     | Notes                                                                                               |
|------------|----------------|-----------------------------------------------------------------------------------------------------|
| Index      | VarInt         | 2                                                                                                   |
| Blocks     | List of Blocks | Example: `minecraft:grass_block` will make the client think it can break grass blocks in adventure. |

**Held Item Name Offset**. Moves the item tooltip text in the action bar vertically. Positive values move it up.

| Field Name | Field Type | Notes           |
|------------|------------|-----------------|
| Index      | VarInt     | 3               |
| Offset     | VarInt     | `0` by default. |

**Camera Lock**. Makes the client unable to move their camera.

| Field Name | Field Type | Notes               |
|------------|------------|---------------------|
| Index      | VarInt     | 4                   |
| Locked     | Boolean    | `false` by default. |

**Custom Music**. Disables vanilla music and adds two new sound categories: Core Music and Game Music.

| Field Name | Field Type | Notes               |
|------------|------------|---------------------|
| Index      | VarInt     | 5                   |
| Enabled    | Boolean    | `false` by default. |

## v0.2.0 Pending Changelog
- Noxesium's API has been completely rewritten. Its features should now be more stable for others to make use of and depend on.
- New feature: Background music now continues playing when briefly setting the music slider to 0% and back.
- New feature: Servers can now easily disable all vanilla music from playing on the client and add extra music sliders.
- New feature: Performance improvements to rendering colored custom models (+~5 fps)
- New feature: Performance improvements to rendering all custom models (+~15 fps)
- Player heads now support showing any skin texture instead of requiring a player uuid. This fixes issues with showing the skins of disguised players.

TODOs:
- Add a system to determine when a server is running an outdated protocol version