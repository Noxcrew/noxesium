Noxesium
---
A Fabric and NeoForge mod that improves the user experience when playing on multiplayer servers.

Server developers are welcome to submit additional patches they need, feel free to open an issue to discuss your ideas.

# Usage
Public builds of Noxesium are available on [Modrinth](https://modrinth.com/mod/noxesium) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/noxesium). Upcoming releases can be found on the [Releases](https://github.com/Noxcrew/noxesium/releases) page here on GitHub.

Developers that want to use Noxesium as a dependency can add it can find the artifact on our [public Maven repository](https://maven.noxcrew.com/#/public/com/noxcrew/noxesium/api).

The following artifacts are available:
- api: General code for interacting with Noxesium on any platform.
- common: Client mod specific code shared between Fabric and NeoForge.
- fabric: Fabric-specific implementation.
- neoforge: NeoForge-specific implementation.
- paper: Server-side API implementation for Paper.

Maven:
```xml
<repository>
    <id>noxcrew-maven</id>
    <name>Noxcrew Public Maven Repository</name>
    <url>https://maven.noxcrew.com/public</url>
</repository>

<dependency>
  <groupId>com.noxcrew.noxesium</groupId>
  <artifactId>api</artifactId>
  <version>NOXESIUM_VERSION</version>
</dependency>
```

Gradle (Kotlin):
```kotlin
repositories {
    maven("https://maven.noxcrew.com/public")
}

dependencies {
    api("com.noxcrew.noxesium:api:NOXESIUM_VERSION")
}
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

Noxesium contains optional performance features which limit UI rendering to 60 fps while keeping everything else at your full framerate. This helps free up computation time to work on rendering instead of redrawing static UI elements. It is however only effective if your fps is above the UI framerate limit. There is an additional setting available to enable dynamic UI fps lowering, which lowers the limit down to 20 fps on a per-element basis.

These performance optimizations can be enabled manually in the Noxesium mod settings menu.

### Bugfixes

Noxesium also fixes various small client bugs that relate to multiplayer servers.

- Fixes jittery behavior of teleporting entities on mounts
- Fixes MC-256850 so moving piston walls don't flicker as much while moving
- Fixes MC-259812 so entity models are properly visible behind text displays


### MCC Island-specific Features

There are also a few features specific to MCC Island as Noxesium is developed alongside it:

- Adds a setting to show player heads in UIs
- Provides access to additional information sent by the server regarding the current server and current game state

# For developers

Any developers interested in writing code that interacts with Noxesium should have a look at the `api` module which contains various structures useful for setting up a server-side implementation that interacts with Noxesium.

We also provide the `paper` module which contains our own server-side implementation written in Kotlin which allows you to interface with Noxesium clients. This also implements backwards compatibility for older Noxesium clients.

Further information about specific features of the mod can be found on the [Wiki](https://github.com/Noxcrew/noxesium/wiki) page.
