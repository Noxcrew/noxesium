![Banner](https://assets.mcchampionship.com/launchy/images-03e3556d-0460-49eb-8ed1-fdf72929da50/noxesium_banner.png)

Noxesium is mod for Fabric which improves your experience when playing on multiplayer servers by giving the server more control over the client and fixing bugs that affect multiplayer gameplay.

# Usage 👤
Public builds of Noxesium are available on [Modrinth](https://modrinth.com/mod/noxesium) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/noxesium). Upcoming releases can be found on the [Releases](https://github.com/Noxcrew/noxesium/releases) page here on GitHub. Developers interested in using Noxesium as an API can find more information on the [Wiki page](https://github.com/Noxcrew/noxesium/wiki).

# Features 💎
Noxesium contains a variety of different features for different users, most are configurable or controlled by the server.

## General 📜
- Modify the scale and position of various UI elements.
![Image showing Noxesium's settings](https://cdn.modrinth.com/data/Kw7Sm3Xf/images/d0b126378896a1eb71c0b9e915344693bb4caf09.png)
- Render off-hand maps in the HUD instead in your hand, so it doesn't move around while walking and can act like a mini-map (optional setting).
![Image showing an off-hand map rendered in the HUD](https://cdn.modrinth.com/data/Kw7Sm3Xf/images/8ca930777c541a564b33617fe00c1c26581c17e4.png)


## Bugfixes 🐛
- [MC-263293](https://bugs.mojang.com/browse/MC-263293): Adds a new accessibility setting to disable toggle sprint resetting on death (enabled by default).
- [MC-259812](https://bugs.mojang.com/browse/MC-259812): Transparent entity models are now properly visible behind text displays.
- [MC-577](https://bugs.mojang.com/browse/MC-577): Inventory closing and drop item keys now work when bound to mouse buttons.
- [MC-301281](https://bugs.mojang.com/browse/MC-301281): Mouse buttons on toggle mode are now re-enabled when closing a menu.
- [MC-256850](https://bugs.mojang.com/browse/MC-256850): Moving piston walls don't flicker as much while moving.
- Passenger entities being teleported no longer jitter.
- Mouse buttons on hold mode now re-trigger when closing a menu.

## Server Features 💻
Noxesium adds a number of extra features to the client which server developers can use in their experiences. This lets them bypass some vanilla restrictions and make better content.

Here's a list of things Noxesium currently lets servers do:
- Create custom interactables on the client like speed boosters or jump pads
- Move authority of riptide tridents, elytras, or lunge spears to the client which makes them act identically regardless of ping
- Move authority of arrow behavior or note block/tripwire block updates to the server only
- Play custom sounds and control various properties, including a start offset, changing the volume over time, and resuming playing the sound
- Draw player heads in text messages with an offset to the position or at different scales
- Draw text with any x/y offset to its position
- Prevent moving items in GUIs, adding hover sounds to items, changing slot sprites when hovered
- Locking camera movements, restrict GUI rescaling, restrict using various debug features such as hitboxes or debug renderers, or hiding the UI through F1
- Tweaking the hitbox size of entities (including non-square hitboxes) or making entities unattackable
- Change the heights of beacon beams
- Detect mouse clicks directly regardless of which interactions occurred after
- Receive additional information on the client's GUI scale and window size to better align custom UIs
- Change the FOV or current zoom of the client
- Open a pop-up to visit a URL in their browser
- Add a custom creative item tab with custom items/blocks from your server

Server developers can use the server API plugin for Paper to interact with these features either through code or through in-game commands!

### Extensions 🧩
Noxesium can be easily extended by other mods and plugins. This lets developers implement their own features for their projects, and contribute these back to the main mod so it works on all clients. For server developers, there's also a lightweight bundled packet injection API.

## MCC Island Integration 🏝️ 

Noxesium has extra features to integrate with MCC Island directly! MCC Island detects any user running Noxesium and sends the client extra information on your current location and game state. This allows other mods like [Island Utils](https://github.com/AsoDesu/IslandUtils) and  [Trident](https://github.com/pe3ep/Trident) to use this information for its custom features.


# Development 🖥️
Noxesium is directly developed by [Noxcrew](https://noxcrew.com/), creators of MC Championship and MCC Island, as a result Noxesium focusses on improving the experience for users of those projects.

Noxesium is also automatically included on some versions of [Lunar Client](https://www.lunarclient.com/).
