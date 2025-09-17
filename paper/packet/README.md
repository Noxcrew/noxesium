# Noxesium Packet API

Implements a generic packet listening and modification API for Bukkit. Used by Noxesium's server-side implementation but can be used without
it as a standalone packet listener.

Packet listening methods are registered using the @PacketHandler notification and can return null to cancel the packet, a modified packet,
or a list of new packets. Bundle packets are automatically split up into individual packets and if a list of packets are returned they are
sent to the client in a bundle.

When listening to serverbound packets handlers must always return a singular packet.

Packets can be accessed during both the configuration and play phases, but it might not always have the required object requested. The receiver type can either be:
- org.bukkit.entity.Player
- net.minecraft.world.entity.player.Player
- io.papermc.paper.connection.PlayerCommonConnection

Only the last type is available during the configuration phase, and can be cast to PlayerConfigurationConnection.