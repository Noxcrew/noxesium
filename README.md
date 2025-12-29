![Noxesium Header Image](https://cdn.modrinth.com/data/Kw7Sm3Xf/09bf3e8a2e3e671272e5e8b1e34ca47cf7598e84_96.webp)

Noxesium
---
A Minecraft mod available on Fabric which improves your experience when playing on large multiplayer servers. Here's some of the main things Noxesium does:

- Fixes various vanilla bugs such as [MC-263293](https://bugs.mojang.com/browse/MC-263293) (Dying resets toggle sprint) and [MC-259812](https://bugs.mojang.com/browse/MC-259812) (Transparent objects are invisible behind text displays).
- Adds extra systems which lets servers add more content, including a custom sound system, creating custom speed boosters or jump pads, or making tridents, elytra and spears client-side authoritative.

Noxesium is directly developed by [Noxcrew](https://noxcrew.com/), creators of MC Championship and MCC Island, as a result most features of Noxesium originate directly from issues encountered by players of those projects.

Noxesium is also automatically included on some versions of [Lunar Client](https://www.lunarclient.com/).

# Usage
Public builds of Noxesium are available on [Modrinth](https://modrinth.com/mod/noxesium) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/noxesium). Upcoming releases can be found on the [Releases](https://github.com/Noxcrew/noxesium/releases) page here on GitHub. Developers interested in using Noxesium as an API can find more information on the [Wiki page](https://github.com/Noxcrew/noxesium/wiki).

# Features

Noxesium has a lot of different features, so they are split into various small groups below:

<details>
<summary>📜 General Features</summary>

- Settings to rescale different GUI elements, accessible in the Noxesium settings menu openable through Mod Menu or by pressing F3+W.
- A new accessibility setting that can be used to render maps held in the off-hand as a UI element instead. This makes it easier to read the map especially when using View Bobbing. Servers can also remotely enable this feature if they want to enforce it.
- Extra settings for debugging usable by server developers to see entity culling hitboxes, game time (for shaders) or scoreboard team exceptions which can cause protocol errors.
</details>

<details>
<summary>🐛 Vanilla Bugfixes</summary>

- [MC-256850](https://bugs.mojang.com/browse/MC-256850): Moving piston walls don't flicker as much while moving.
- [MC-259812](https://bugs.mojang.com/browse/MC-259812): Transparent entity models are now properly visible behind text displays.
- [MC-263293](https://bugs.mojang.com/browse/MC-263293): Adds a new accessibility setting to disable toggle sprint resetting on death (enabled by default).
- [MC-577](https://bugs.mojang.com/browse/MC-577): Inventory closing and drop item keys now work when bound to mouse buttons.
- [MC-301281](https://bugs.mojang.com/browse/MC-301281): Mouse buttons on toggle mode are now re-enabled when closing a menu.
- Mouse buttons on hold mode now re-trigger when closing a menu.
- Passenger entities being teleported no longer jitter.
</details>

<details>
<summary>💻 Server Features</summary>

Noxesium adds a number of extra features only accessible for server developers. This lets them bypass some vanilla restrictions and make better content.

Here's a list of things Noxesium lets servers do:

- Create custom interactables on the client like speed boosters or jump pads
- Move authority of riptide tridents, elytras, or lunge spears to the client which makes them act identically regardless of ping
- Move authority of arrow behavior or note block/tripwire block updates to the server only
- Play custom sounds and control various properties, including a start offset, changing the volume over time, resuming playing the sound and starting anchored to a UNIX timestamp
- Draw player heads in text messages with an offset to the position or at different scales
- Draw text with any x/y offset to its position
- Prevent moving items in GUIs, adding hover sounds to items, changing slot sprites when hovered
- Locking camera movements, restrict GUI rescaling, restrict using various debug features or hiding the UI
- Tweaking the hitbox size of entities (including non-square hitboxes) or making entities unattackable
- Change the heights of beacon beams
- Detect mouse clicks directly regardless of which interactions occurred after

These features are all available through the publicly available Noxesium Paper plugin which lets you configure and use most with in-game commands and everything with its API.
</details>

<details>
<summary>🏝️ MCC Island Integration</summary>

Noxesium has extra features to integrate with MCC Island directly! MCC Island detects any user running Noxesium and sends the client extra information on your current location and game state. This allows other mods like [Island Utils](https://github.com/AsoDesu/IslandUtils) to use this information for its custom features.
</details>

<details>
<summary>📂 Folder Synchronization</summary>

As an optional feature for server developers, Noxesium comes with an extension called Noxesium Sync. This can be separately installed on both the client and server to let you synchronize folders in the server directory with clients. This can be used to simplify development by modifying configurations from your client instead of having to use an FTP connection. It also speeds up testing significantly if servers are ran inside Docker containers as it allows modifying configs of running servers. This module should **never** be enabled on production servers.

</details>