Noxesium
---
A fabric mod centered around improving the user experience when playing on multiplayer servers.

Server developers are welcome to submit additional patches they need, feel free to open an Issue to discuss your ideas.

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

Noxesium provides various additional features focussed around giving servers more control over how the client behaves. Some examples of additional things servers can do:

- Create custom interactables on the client like speed boosters or jump pads
- Draw player heads in text messages
- Play custom sounds and control various properties, including a start offset, changing the volume over time, resuming playing the sound and starting anchored to a UNIX timestamp
- Tweak the behavior of riptide tridents to make them more usable in multiplayer games
- Prevent moving items in GUIs
- Locking camera movement

There are also a few improvements that do not require a server:
- Prevents toggle sprint/sneak from resetting on death (can be configured)
- Allow rendering off-hand maps as a UI element
- Adds toggleable debug features for server and shader developers

### Performance

Noxesium also contains some minor performance patches, although these will not significantly improve your fps. Instead, Noxesium focusses on areas that are not covered by other mods like UI rendering or custom models. These don't occur in vanilla gameplay so they get little focus.

Greater performance improvements have been included in the past, but as it's not the main focus they have been eclipsed by improvements in vanilla Minecraft and other mods. The following improvements are still present:

- Optimizations to custom item model overrides with large amounts of CustomModelData variants
- Work is being done on a set of UI performance optimizations that warp around vanilla's UI rendering and slow down the frame rate at which they are being rendered when few changes are occurring. These optimizations have been tested in the past but are currently still being developed.

### Bugfixes

Noxesium also fixes various small client bugs that relate to multiplayer servers.

- Fixes jittery behavior of teleporting entities on mounts
- Fixes lighting issues with moving piston walls
- Music properly resumes playing when setting the volume to 0% and back up
- Fixes the backgrounds of text displays not being transparent
- Fixes the tab menu not showing secondary skin layers

### MCC Island-specific Features

There are also a few features specific to MCC Island as Noxesium is developed alongside it:

- Adds a setting to show player heads in UIs
- Provides access to additional information sent by the server regarding the current server and current game state

# For developers

Any developers interested in writing code that interacts with Noxesium should have a look at the `api` module which contains various structures useful for setting up a server-side implementation that interacts with Noxesium. This modules
does not have a dependency on fabric and can thus be used as a dependency in server software.

We also provide the `paper` module which contains our own server-side implementation written in Kotlin which allows you to interface with Noxesium clients. This also implements backwards compatibility for older Noxesium clients.

Further information about specific features of the mod can be found on the [Wiki](https://github.com/Noxcrew/noxesium/wiki) page.
