package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType
import com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry
import com.noxcrew.noxesium.paper.NoxesiumPaper
import io.netty.buffer.Unpooled
import net.minecraft.network.RegistryFriendlyByteBuf
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.logging.Level

/**
 * Listens to incoming plugin messages and triggers packet handlers.
 */
public data class PaperPacketHandler<T : NoxesiumPacket>(private val payloadType: NoxesiumPayloadType<T>) : PluginMessageListener {
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        // Safety check, although Bukkit presumably already filters this!
        if (channel != payloadType.id().toString()) return

        try {
            // Decode the message and let handlers handle it
            val craftPlayer = player as CraftPlayer
            val buffer = RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(message), craftPlayer.handle.registryAccess())
            val codec = PacketSerializerRegistry.getSerializers(payloadType) ?: return
            payloadType.handle(player.uniqueId, codec.decode(buffer))
        } catch (x: Exception) {
            NoxesiumPaper.plugin.logger.log(Level.WARNING, "Failed to decode plugin message on channel '$channel' for ${player.name}", x)
        }
    }
}
