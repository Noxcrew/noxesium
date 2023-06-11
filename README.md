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

Noxesium communicates with the server over plugin messaging channels. The namespace of these channels matches the current API version of Noxesium, which is currently `v1`. Currently, there are 4 messaging channels available.

`noxesium-v1:client_information` (client -> server); Contains the Protocol version of the mod version used by the client. This is sent right after a client joins the server. If a proxy server is used it's recommended to cache this value per
session on the server-side. Protocol versions are bumped when major new capabilities are introduced so servers can disable features for outdated mod users. E.g. the introduction of player head components caused a protocol bump so MCC Island
can disable the setting for users with an older mod version.

| Field Name       | Field Type | Notes       |
|------------------|------------|-------------|
| Protocol Version | VarInt     | Currently 2 |

`noxesium-v1:client_settings` (client -> server); Contains information about the settings used by the client. Sent when a player joins a server or whenever the player changes their settings.

| Field Name                | Field Type | Notes                                               |
|---------------------------|------------|-----------------------------------------------------|
| GUI Scale                 | VarInt     | The value set in the video settings screen.         |
| Internal GUI Scale        | Double     | The internal GUI scale value.                       |
| Scaled Width              | VarInt     | The scaled width of the window.                     |
| Scaled Height             | VarInt     | The scaled height of the window.                    |
| Enforce Unicode           | Boolean    | Whether the enforce unicode setting is on.          |
| Touchscreen Mode          | Boolean    | Whether touchscreen mode is on.                     |
| Notification Display Time | Double     | The value of the notification display time setting. |

`noxesium-v1:server_rules` (server -> client); Can be used to modify the current values of server rules known to this client. You should not mark rules being changed for reset, the client avoids triggering changes if a rule's value did not
change.

| Field Name      | Field Type       | Notes                                                                                                                                                           |
|-----------------|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Rules to reset  | VarInt Array     | An array of rule ids to reset to their default value.                                                                                                           |
| Amount of rules | VarInt           | Size of the rules array.                                                                                                                                        |
| Rules to change | ServerRule Array | An array of server rules to change, each rule has their own data format. This object always starts with a VarInt of the rule index before each rule's own data. |

`noxesium-v1:reset` (server -> client); Allows the server to reset specific parts of client data.

| Field Name | Field Type | Notes                                                                                                      |
|------------|------------|------------------------------------------------------------------------------------------------------------|
| Command    | Byte       | Bitmasked command byte. 0x01 clears all server rule settings. 0x02 clears all cached custom player skulls. |

## Server Rules

Server Rules are a system similar to Game Rules but able to be modified whenever desired. Server Rule settings are cleared whenever a player disconnects from a server. These values allow a server to affect the client's state easily. Below
is a list of every server rule currently available and their data format in the plugin message:

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