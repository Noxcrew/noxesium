Noxesium
---
A fabric mod with feature additions, bugfixes and various performance improvements.

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

### For developers
The mod communicates with the server over plugin messaging channels. Currently there are 3 messaging channels involved.

`noxesium:client_information`. Client sends a packet containing the Protocol version of the mod (current: 2) when it joins the server. 
|Field Name       |Field Type       |Notes            |
|-----------------|-----------------|-----------------|
|Protocol Version |Integer          |Currently 2      |

`noxesium:client_settings`. Client sends a packet containing it's settings when it joins the server.
|Field Name       |Field Type       |
|-----------------|-----------------|
|GUI Scale        |Integer          |
|Enforce Unicode  |Boolean          |

`noxesium:server_rules`. Server sends ServerRule packets.
|Field Name       |Field Type       |Notes                                |
|-----------------|-----------------|-------------------------------------|
|Rules Modified   |VarInt Array     |                                     |
|Amount of rules  |Integer          |Amount of rules following this field.|
|Rules            |ServerRule Array |                                     |

### Server Rules
Disable Auto Spin Attack
|Field Name       |Field Type       |Notes                                            |
|-----------------|-----------------|-------------------------------------------------|
|Index            |Integer          |0                                                |
|Value            |Boolean          |`true` to disable collisions. `false` by default.|

Global Can Place On
|Field Name       |Field Type         |Notes            |
|-----------------|-------------------|-----------------|
|Index            |Integer            |1                |
|List             |List of UTF Strings|Not documented   |

Global Can Destroy
|Field Name       |Field Type         |Notes            |
|-----------------|-------------------|-----------------|
|Index            |Integer            |2                |
|List             |List of UTF Strings|Not documented   |

Held Item Name Offset
|Field Name       |Field Type         |Notes            |
|-----------------|-------------------|-----------------|
|Index            |Integer            |3                |
|Offset           |Integer            |`0` by default   |

Camera Lock
|Field Name       |Field Type         |Notes              |
|-----------------|-------------------|-------------------|
|Index            |Integer            |4                  |
|Locked           |Boolean            |`false` by default.|
