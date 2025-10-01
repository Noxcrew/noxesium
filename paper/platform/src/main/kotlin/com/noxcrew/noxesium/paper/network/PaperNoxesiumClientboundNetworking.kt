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
import net.minecraft.resources.ResourceLocation

/** Implements clientbound networking for Paper. */
public class PaperNoxesiumClientboundNetworking : NoxesiumClientboundNetworking() {
    private val payloadTypes = mutableMapOf<String, NoxesiumPayloadType<*>>()
    public var capturedPacket: ClientboundCustomPayloadPacket? = null

    /**
     * Enables packet capture which will set [capturedPacket] to the next
     * packet instead of sending it to the client. This may come in useful if you are
     * doing some extremely cursed bundling of packets on entity initialization.
     * Not saying I am doing exactly that and thus need this feature.
     */
    public var capturePackets: Boolean = false

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

    override fun <T : NoxesiumPacket> send(player: NoxesiumServerPlayer, type: NoxesiumPayloadType<T>, payload: T) {
        // Force serialization of the packet to happen right here so we can set the target player
        // for the stream codecs! Do not defer this to happen later in the netty thread. We do this just so
        // we can apply the ViaVersion codecs defined above, serializing on a separate thread is fine
        // for any other implementations that do not need such a technique.
        val player = (player as PaperNoxesiumServerPlayer).player ?: return
        (NoxesiumPlatform.getInstance() as? PaperPlatform)?.currentTargetPlayer = player

        // We have to do this custom so we can re-use the byte buf otherwise it gets padded with 0's!
        val buffer = RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess())
        if (type.jsonSerialized) {
            val serializer =
                JsonSerializerRegistry
                    .getInstance()
                    .getSerializer(type.clazz.getAnnotation(JsonSerializedPacket::class.java).value)
            buffer.writeUtf(serializer.encode(payload, type.clazz))
        } else {
            // Use the stream codec of this payload type to encode it into the buffer
            val serializer = PacketSerializerRegistry.getSerializers(type.clazz)
            serializer.encode(buffer, payload)
        }
        (NoxesiumPlatform.getInstance() as? PaperPlatform)?.currentTargetPlayer = null

        // Copy only the used bytes otherwise we send lingering empty data which crashes clients
        val out = ByteArray(buffer.readableBytes())
        System.arraycopy(buffer.array(), 0, out, 0, buffer.readableBytes())
        val packet = ClientboundCustomPayloadPacket(DiscardedPayload(ResourceLocation.parse(type.id().asString()), out))
        if (capturePackets) {
            capturedPacket = packet
            capturePackets = false
        } else {
            player.sendPayload(packet)
        }
    }

    override fun markLazyActive(payloadGroup: NoxesiumPayloadGroup) {
        // Inform all authenticated players directly about the newly un-lazy packet
        val packet = ClientboundLazyPacketsPacket(setOf(payloadGroup.groupId()))
        NoxesiumPlayerManager.getInstance().allPlayers.forEach {
            it.sendPacket(packet)
        }
    }
}
