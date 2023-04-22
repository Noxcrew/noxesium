Noxesium
---
A fabric mod with feature additions, bugfixes, and performance improvements. It allows servers to offer a better experience to clients by giving access to additional features beyond vanilla limitations.

Server developers are welcome to submit additional patches they need, however all changes must be fully optional to use.

# Features

### Additions
- Sends information about GUI scale to connected servers to allow better server-side UI design
- Allow servers to disable riptide trident collisions
- Allow servers to define a global CanDestroy and CanPlaceOn tag that applies to all items
- Allow servers to show player heads in chat messages

### Performance
- Optimized Beacon block entity rendering (disabled when using Iris Shaders)

### Bugfixes
- Always display head layer in TAB menu
- Change teleport packets to teleport vehicle passengers to their vehicle
- Fixes lighting issues with moving piston walls
- Fixes custom models sometimes disappearing when not looking at the center

### MCC Island-only Features
- Adds a setting to show player heads in UIs


# For developers
## Message Channels
Noxesium communicates with the server over plugin messaging channels. Currently, there are 3 messaging channels available.

`noxesium:client_information` (client -> server); Contains the Protocol version of the mod version used by the client. This is sent right after a client joins the server. If a proxy server is used it's recommended to cache this value per session on the server-side. Protocol versions are bumped when major new capabilities are introduced so servers can disable features for outdated mod users. E.g. the introduction of player head components caused a protocol bump so MCC Island can disable the setting for users with an older mod version.

|Field Name       |Field Type       |Notes            |
|-----------------|-----------------|-----------------|
|Protocol Version |Integer          |Currently 2      |

`noxesium:client_settings` (client -> server); Contains information about the settings used by the client. Sent when a player joins a server or whenever the player changes their settings.

|Field Name       |Field Type       |
|-----------------|-----------------|
|GUI Scale        |Integer          |
|Enforce Unicode  |Boolean          |

`noxesium:server_rules` (server -> client); Can be used to modify the current values of server rules known to this client. You should not mark rules being changed for reset, the client avoids triggering changes if a rule's value did not change.

| Field Name      |Field Type       | Notes                                                                                                                                                             |
|-----------------|-----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Rules to reset  |VarInt Array     | An array of rule ids to reset to their default value.                                                                                                             |
| Amount of rules |Integer          | Size of the rules array.                                                                                                                                          |
| Rules to change |ServerRule Array | An array of server rules to change, each rule has their own data format. This object always starts with an Integer of the rule index before each rule's own data. |

## Server Rules
Server Rules are a system similar to Game Rules but able to be modified whenever desired. Server Rule settings are cleared whenever a player disconnects from a server. These values allow a server to affect the client's state easily.

**Disable Auto Spin Attack**. Disabled colliding with other entities while riptiding. This also prevents the spin attack from dealing any damage to entities moved through.

|Field Name       |Field Type       |Notes                                            |
|-----------------|-----------------|-------------------------------------------------|
|Index            |Integer          |0                                                |
|Value            |Boolean          |`true` to disable collisions. `false` by default.|

**Global Can Place On**. Makes client in adventure able to build on blocks provided. Similar to the per-item Place On but applies to all items including the empty hand.

|Field Name       |Field Type         |Notes            |
|-----------------|-------------------|-----------------|
|Index            |Integer            |1                |
|List             |List of Blocks     |Example: `minecraft:grass_block` will make the client think it can place blocks on grass blocks in adventure.|

**Global Can Destroy**. Makes client in adventure able to break blocks provided. Similar to the per-item Can Destroy but applies to all items including the empty hand.

|Field Name       |Field Type         |Notes            |
|-----------------|-------------------|-----------------|
|Index            |Integer            |2                |
|Blocks           |List of Blocks     |Example: `minecraft:grass_block` will make the client think it can break grass blocks in adventure.|

**Held Item Name Offset**. Moves the item tooltip text in the action bar vertically. Positive values move it up.

|Field Name       |Field Type         |Notes            |
|-----------------|-------------------|-----------------|
|Index            |Integer            |3                |
|Offset           |Integer            |`0` by default   |

**Camera Lock**. Makes the client unable to move their camera.

|Field Name       |Field Type         |Notes              |
|-----------------|-------------------|-------------------|
|Index            |Integer            |4                  |
|Locked           |Boolean            |`false` by default.|
