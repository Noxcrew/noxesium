package com.noxcrew.packet

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket

/**
 * A custom packet handler that modifies incoming and outgoing packets.
 */
internal class ChannelPacketHandler(
    private val packetApi: PacketApi,
    private val connection: Connection,
) : ChannelDuplexHandler() {
    @Suppress("UNCHECKED_CAST")
    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        // Packet Events supports sending hidden packets which are sent as direct byte buffers instead
        // of packets. To avoid breaking them we need to ignore non-packet objects.
        if (msg !is Packet<*>) {
            super.write(ctx, msg, promise)
            return
        }

        val packets = packetApi.handlePacket(connection, msg)
        if (packets.isEmpty()) {
            return
        } else if (packets.size == 1) {
            super.write(ctx, packets[0], promise)
        } else {
            // Create a new bundle packet containing all packets, we are always in the play
            // phase when this packet API is running anyway!
            super.write(ctx, ClientboundBundlePacket(packets as Iterable<Packet<in ClientGamePacketListener>>), promise)
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, packet: Any) {
        // Ignore non packet objects
        if (packet !is Packet<*>) {
            super.channelRead(ctx, packet)
            return
        }

        val newPackets = packetApi.handlePacket(connection, packet)
        if (newPackets.isEmpty()) return
        require(newPackets.size == 1) { "Cannot read multiple packets at once" }
        super.channelRead(ctx, newPackets[0])
    }
}
