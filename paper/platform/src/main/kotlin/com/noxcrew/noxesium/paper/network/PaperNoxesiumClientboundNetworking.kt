package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundLazyPacketsPacket
import com.noxcrew.noxesium.api.network.json.JsonSerializedPacket
import com.noxcrew.noxesium.api.network.json.JsonSerializerRegistry
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType
import com.noxcrew.noxesium.api.nms.NoxesiumPlatform
import com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.paper.PaperPlatform
import io.netty.buffer.Unpooled
import net.kyori.adventure.key.Key
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import net.minecraft.resources.Identifier

/** Implements clientbound networking for Paper. */
public class PaperNoxesiumClientboundNetworking : NoxesiumClientboundNetworking() {
    private val payloadTypes = mutableMapOf<String, NoxesiumPayloadType<*>>()

    /** Returns the payload type for the given channel. */
    public fun getPayloadType(channel: String): NoxesiumPayloadType<*>? = payloadTypes[channel]

    override fun register(payloadGroup: NoxesiumPayloadGroup?, entrypoint: NoxesiumEntrypoint?) {
        super.register(payloadGroup, entrypoint)
        payloadGroup?.payloadTypes?.forEach {
            require(it.id().asString() !in payloadTypes) {
                "Payload channel ${
                    it.id().asString()
                } registered multiple times"
            }
            payloadTypes[it.id().asString()] = it
        }
    }

    override fun unregister(payloadGroup: NoxesiumPayloadGroup?) {
        super.unregister(payloadGroup)
        payloadGroup?.payloadTypes?.forEach {
            payloadTypes -= it.id().asString()
        }
    }

    override fun <T : NoxesiumPacket> createPayloadType(
        group: NoxesiumPayloadGroup,
        id: Key,
        clazz: Class<T>,
        clientToServer: Boolean,
    ): NoxesiumPayloadType<T> = NoxesiumPayloadType(group, id, clazz, clientToServer)

    override fun <T : NoxesiumPacket> canReceive(player: NoxesiumServerPlayer, type: NoxesiumPayloadType<T>): Boolean {
        // Prevent sending if the client does not know of this channel!
        val noxesiumPlayer = (player as PaperNoxesiumServerPlayer)
        if (!noxesiumPlayer.isConnected) return false
        if (type.id().toString() !in noxesiumPlayer.clientRegisteredPluginChannels) return false
        return super.canReceive(player, type)
    }

    override fun <T : NoxesiumPacket> create(
        player: NoxesiumServerPlayer,
        type: NoxesiumPayloadType<T>,
        payload: T,
    ): ClientboundCustomPayloadPacket? {
        // Force serialization of the packet to happen right here so we can set the target player
        // for the stream codecs! Do not defer this to happen later in the netty thread. We do this just so
        // we can apply the custom transforming ViaVersion codecs, serializing on a separate thread is fine
        // for any other implementations that do not need such a technique.
        val player = (player as PaperNoxesiumServerPlayer).player ?: return null

        // We have to do this custom so we can re-use the byte buf otherwise it gets padded with 0's!
        val buffer = RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess())
        if (type.jsonSerialized) {
            val serializer =
                JsonSerializerRegistry
                    .getInstance()
                    .getSerializer(type.clazz.getAnnotation(JsonSerializedPacket::class.java).value)
            buffer.writeUtf(serializer.encode(payload, type.clazz))
        } else {
            // Determine which serializer to use!
            val serializer = PacketSerializerRegistry.getSerializers(type.clazz)

            // Ensure we do not share access to the current target player with other threads!
            val platform = (NoxesiumPlatform.getInstance() as? PaperPlatform)
            synchronized(platform?.currentTargetPlayer ?: Any()) {
                try {
                    // Use the stream codec of this payload type to encode it into the buffer
                    platform?.currentTargetPlayer = player
                    serializer.encode(buffer, payload)
                } finally {
                    platform?.currentTargetPlayer = null
                }
            }
        }

        // Copy only the used bytes otherwise we send lingering empty data which crashes clients
        val out = ByteArray(buffer.readableBytes())
        System.arraycopy(buffer.array(), 0, out, 0, buffer.readableBytes())
        return ClientboundCustomPayloadPacket(DiscardedPayload(Identifier.parse(type.id().asString()), out))
    }

    override fun <T : NoxesiumPacket?> send(player: NoxesiumServerPlayer, type: NoxesiumPayloadType<T?>, payload: Any) {
        val player = (player as PaperNoxesiumServerPlayer).player ?: return
        player.sendPayload(payload as? ClientboundCustomPayloadPacket ?: return)
    }

    override fun markLazyActive(payloadGroup: NoxesiumPayloadGroup) {
        // Inform all authenticated players directly about the newly un-lazy packet
        val packet = ClientboundLazyPacketsPacket(setOf(payloadGroup.groupId()))
        NoxesiumPlayerManager.getInstance().allPlayers.forEach {
            it.sendPacket(packet)
        }
    }
}
