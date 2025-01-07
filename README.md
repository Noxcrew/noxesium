![Noxesium Header Image](https://cdn.modrinth.com/data/Kw7Sm3Xf/09bf3e8a2e3e671272e5e8b1e34ca47cf7598e84_96.webp)

Noxesium
---
A Minecraft mod available on Fabric and NeoForge improves your experience when playing on large multiplayer servers. Here's some of the main things Noxesium does:

- Fixes various vanilla bugs such as [MC-263293](https://bugs.mojang.com/browse/MC-263293) (Dying resets toggle sprint) and [MC-259812](https://bugs.mojang.com/browse/MC-259812) (Transparent objects are invisible behind text displays).
- Adds extra systems which lets servers add more content, including a custom sound system, creating custom speed boosters or jump pads, and drawing player heads in text.
- Introduces optional performance settings for limiting the fps at which the user interface is rendered.

Noxesium is directly developed by [Noxcrew](https://noxcrew.com/), creators of MC Championship and MCC Island, as a result most features of Noxesium originate directly from issues encountered by players of those projects.

Noxesium is also automatically included on some versions of [Lunar Client](https://www.lunarclient.com/).

# Usage
Public builds of Noxesium are available on [Modrinth](https://modrinth.com/mod/noxesium) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/noxesium). Upcoming releases can be found on the [Releases](https://github.com/Noxcrew/noxesium/releases) page here on GitHub.  Developers interested in using Noxesium as an API can find more information on the [Wiki page](https://github.com/Noxcrew/noxesium/wiki).

# Features

Noxesium has a lot of different features, so they are split into various small groups below:

<details>
<summary>Vanilla Bugfixes</summary>

- [MC-256850](https://bugs.mojang.com/browse/MC-256850): Moving piston walls don't flicker as much while moving
- [MC-259812](https://bugs.mojang.com/browse/MC-259812): Transparent entity models are now properly visible behind text displays
- [MC-263293](https://bugs.mojang.com/browse/MC-263293): Adds a new accessibility setting to disable toggle sprint resetting on death (enabled by default)
- Passenger entities being teleported no longer jitter
</details>

<details>
<summary>General Features</summary>

- A new accessibility setting that can be used to render maps held in the off-hand as a UI element instead. This makes it easier to read the map especially when using View Bobbing. Servers can also remotely enable this feature if they want to enforce it.
- Extra debug overlays which can be used to see your current fps or show extra information for server and shader developers.
</details>

<details>
<summary>UI Performance</summary>

Noxesium contains an optional feature to limit the framerate at which UI elements are rendered. For most players the fps impact is negligible as rendering the UI does not take that long. However, if you run a highly optimized client with mods such as [Sodium](https://github.com/CaffeineMC/sodium) or [Nvidium](https://github.com/MCRcortex/nvidium) the UI may start to become a bottleneck.

For users with this issue Noxesium adds a new optional performance setting called "Limit UI Framerate", when enabled the user interface is rendered at at most 60 fps (this is also configurable).

There is also an additional setting called "Dynamic UI Optimization" which is only available to Windows/Linux users that have at least OpenGL 4.4. This will attempt to lower the UI Framerate dynamically whenever no animations happen but return to the maximum framerate during animations. With this you can run a higher max UI framerate with good performance, but mileage may vary per user!

</details>

<details>
<summary>Server Features</summary>

Noxesium adds a number of extra features only accessible for server developers. This lets them bypass some vanilla restrictions and make better content.

Here's a list of things Noxesium lets servers do:
- Create custom interactables on the client like speed boosters or jump pads
- Play custom sounds and control various properties, including a start offset, changing the volume over time, resuming playing the sound and starting anchored to a UNIX timestamp
- Draw player heads in text messages
- Tweak the behavior of riptide tridents to make them more usable in multiplayer games
- Prevent moving items in GUIs
- Locking camera movement
</details>

<details>
<summary>MCC Island Integration</summary>

Noxesium has extra features to integrate with MCC Island directly! MCC Island detects any user running Noxesium and sends the client extra information on your current location and game state. This allows other mods like [Island Utils](https://github.com/AsoDesu/IslandUtils) to use this information for its custom features.
</details>