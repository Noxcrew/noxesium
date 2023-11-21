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
- Allow servers to play custom sounds and control various properties, including a start offset, changing the volume over time, resuming playing the sound and starting anchored to a UNIX timestamp

### Performance
- (Experimental, disabled by default) Massively optimizes UI rendering by buffering components and optimizing text rendering
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

Further information about specific features of the mod can be found on the [Wiki](https://github.com/Noxcrew/noxesium/wiki) page.