# Noxesium Packet API

Implements a generic packet listening and modification API for Bukkit. Used by Noxesium's server-side implementation but can be used without
it as a standalone packet listener.

Packet listening methods are registered using the @PacketHandler notification and can return null to cancel the packet, a modified packet,
or a list of new packets. Bundle packets are automatically split up into individual packets and if a list of packets are returned they are
sent to the client in a bundle.

When listening to serverbound packets handlers must always return a singular packet.

Packets can only be accessed during the PLAY phase, handler is only registered when the PlayerJoinEvent is called.