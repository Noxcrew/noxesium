Noxesium
---
A fabric mod centered around improving the user experience when playing on multiplayer servers.
This mod brings no notable performance improvements in single player.

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

### Performance

Optimizes client performance when playing on multiplayer servers such as MCC Island or Origin Realms. Optimizations are focussed on areas that other mods normally don't optimize as they usually focus on regular survival mode gameplay.

- Recent versions contain experimental UI optimizations which can be enabled in the Video Settings. These optimizations currently don't support 3rd party mods yet but will in a future release. After these are stable they will be automatically enabled and should improve client fps on servers with custom UI elements.
- Various small optimizations to rendering custom entity models
- Speeds up beacon beam rendering (only when not using Iris Shaders)

### Additions

Noxesium provides various additional features focussed around giving servers more control over how the client behaves. Some examples of additional things servers can do:

- Draw player heads in text messages
- Play custom sounds and control various properties, including a start offset, changing the volume over time, resuming playing the sound and starting anchored to a UNIX timestamp
- Use information about the GUI scale of clients
- Disable various minor mechanics such as trident or boat collisions
- Define an override for the empty hand item
- Prevent moving items in GUIs
- Locking camera movement

### Bugfixes

Noxesium also fixes various small client bugs that relate to multiplayer servers.

- Fixes jittery behavior of teleporting entities on mounts
- Fixes lighting issues with moving piston walls
- Music properly resumes playing when setting the volume to 0% and back up
- Fixes the backgrounds of text displays not being transparent

### MCC Island-specific Features

There are also a few features specific to MCC Island as Noxesium is developed alongside it:

- Adds a setting to show player heads in UIs
- Provides access to additional information sent by the server regarding the current server and current game state

# For developers

Any developers interested in writing code that interacts with Noxesium should have a look at the `api` module which contains various structures useful for setting up a server-side implementation that interacts with Noxesium. This modules
does not have a dependency on fabric and can thus be used as a dependency in server software.

Further information about specific features of the mod can be found on the [Wiki](https://github.com/Noxcrew/noxesium/wiki) page.
