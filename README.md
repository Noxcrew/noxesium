Noxesium
---
A fabric mod with feature additions, bugfixes, and performance improvements. It allows servers to offer a better experience to clients through access to additional features beyond vanilla limitations.

Server developers are welcome to submit additional patches they need, however all non-performance changes must be fully optional to use. No client-side configuration is available as behaviour should be decided by the server.

# Usage
Public builds of Noxesium can be found on the [Modrinth](https://modrinth.com/mod/noxesium) page. Upcoming releases can be found on the [Releases](https://github.com/Noxcrew/noxesium/releases) page here on GitHub.

Developers that want to use Noxesium as a dependency can add it can find the artefact on our [public Maven repository](https://maven.noxcrew.com/#/public/com/noxcrew/noxesium/api).

```xml
<repository>
    <id>noxcrew-maven</id>
    <name>Noxcrew Public Maven Repository</name>
    <url>https://maven.noxcrew.com/public</url>
</repository>

<dependency>
  <groupId>com.noxcrew.noxesium</groupId>
  <artifactId>api</artifactId>
  <version>REPLACE_WITH_CURRENT_NOXESIUM_VERSION</version>
</dependency>
```

# Features

### Additions

- Sends information about GUI scale to connected servers to allow better server-side UI design
- Allow servers to disable riptide trident collisions
- Allow servers to define a global CanDestroy and CanPlaceOn tag that applies to all items
- Allow servers to show player heads in chat messages
- Allow servers to prevent picking up items in GUIs
- Allow servers to prevent camera movement temporarily
- Allow servers to disable vanilla Minecraft music and play custom music in custom categories
- Allow servers more control the start offset, and change volume of sounds

### Performance
- Optimizes CustomModelData lookups for item models
- Optimizes color lookups for leather armor
- Caches color provider results in Sodium
- Optimizes Beacon block entity rendering (disabled when using Iris Shaders)

### Bugfixes

- Always display head layer in TAB menu
- Change teleport packets to teleport vehicle passengers to their vehicle
- Fixes lighting issues with moving piston walls
- Music properly resumes playing when setting the volume to 0% and back up
- Fixes the backgrounds of text displays not being transparent

### MCC Island-specific Features

- Adds a setting to show player heads in UIs
- Provides access to additional information sent by the server regarding the current server and current game state

# For developers

Any developers interested in writing code that interacts with Noxesium should have a look at the `api` module which contains various structures useful for setting up a server-side implementation that interacts with Noxesium. This modules
does not have a dependency on fabric and can thus be used as a dependency in server software.

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

**Disable Boat Collisions**. Disables boats colliding with other entities. Similar modification is recommended on the server side to ensure behaviour congruity.

| Field Name | Field Type | Notes               |
|------------|------------|---------------------|
| Index      | VarInt     | 6                   |
| Value      | Boolean    | `false` by default. |
## Sounds
Noxesium allows servers more control over sounds, when they're started, and while they're playing. 
You can start a sound from a specific start time, have the sound automatically loop, or modify the
sound's volume while it's playing.

### Start Sound
Sounds using this system must be started through the `start_sound` custom packet.\
**NOTE:** If a sound with the same location is already playing, that sound will be stopped.

| Field Name   | Field Type  | Notes                                                                                                      |
|--------------|-------------|------------------------------------------------------------------------------------------------------------|
| Sound        | Identifier  | The sound to play                                                                                          |
| Category     | VarInt Enum | The sound category to play in ([current categories](https://gist.github.com/konwboj/7c0c380d3923443e9d55)) |
| X            | VarInt      | The X position of the sound                                                                                |
| Y            | VarInt      | The Y position of the sound                                                                                |
| Z            | VarInt      | The Z position of the sound                                                                                |
| Looping      | Boolean     | Weather the sound should automatically loop                                                                |
| Volume       | Float       | The starting volume of the sound (0 - 1)                                                                   |
| Pitch        | Float       | The pitch of the sound (0.5 - 2)                                                                           |
| Start Offset | Float       | The start offset of the sound (in seconds)                                                                 |

### Modify Volume
The `sound_volume` custom packet allows you to modify the volume of a sound played through the 
`start_sound` packet.\
**NOTE:** Setting/fading the volume to 0 will not stop the sound.

| Field Name    | Field Type | Notes                  |
|---------------|------------|------------------------|
| Sound         | Identifier | The sound to modify    |
| Volume        | Float      | The volume to set to   |
| Interpolation | Float      | The fade time in ticks |