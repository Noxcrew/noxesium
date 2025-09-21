package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundLazyPacketsPacket
import com.noxcrew.noxesium.api.network.json.JsonSerializedPacket
import com.noxcrew.noxesium.api.network.json.JsonSerializerRegistry
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

    override fun getRegisteredChannels(player: NoxesiumServerPlayer): Collection<String> =
        (player as PaperNoxesiumServerPlayer).registeredPluginChannels

    override fun register(payloadType: NoxesiumPayloadType<*>?, entrypoint: NoxesiumEntrypoint?) {
        super.register(payloadType, entrypoint)
        if (payloadType != null) {
            require(payloadType.id().asString() !in payloadTypes) {
                "Payload channel ${
                    payloadType.id().asString()
                } registered multiple times"
            }
            payloadTypes[payloadType.id().asString()] = payloadType
        }
    }

    override fun <T : NoxesiumPacket> createPayloadType(
        namespace: String,
        id: String,
        clazz: Class<T>,
        clientToServer: Boolean,
        configPhaseCompatible: Boolean,
    ): NoxesiumPayloadType<T> = NoxesiumPayloadType(Key.key(namespace, id), clazz, clientToServer, configPhaseCompatible)

    override fun canReceive(player: NoxesiumServerPlayer, type: NoxesiumPayloadType<*>): Boolean {
        // Prevent sending if the client does not know of this channel!
        val noxesiumPlayer = (player as PaperNoxesiumServerPlayer)
        val serverPlayer = noxesiumPlayer.player
        if (serverPlayer.connection == null) return false
        if (type.id().toString() !in noxesiumPlayer.registeredPluginChannels) return false
        return super.canReceive(player, type)
    }

    override fun <T : NoxesiumPacket> send(player: NoxesiumServerPlayer, type: NoxesiumPayloadType<T>, payload: T): Boolean {
        // Check if we're allowed to send it!
        if (!canReceive(player, type)) return false

        // Force serialization of the packet to happen right here so we can set the target player
        // for the stream codecs! Do not defer this to happen later in the netty thread. We do this just so
        // we can apply the ViaVersion codecs defined above, serializing on a separate thread is fine
        // for any other implementations that do not need such a technique.
        val serverPlayer = (player as PaperNoxesiumServerPlayer).player
        (NoxesiumPlatform.getInstance() as? PaperPlatform)?.currentTargetPlayer = serverPlayer
        val packet =
            ClientboundCustomPayloadPacket(
                DiscardedPayload(
                    ResourceLocation.parse(type.id().asString()),
                    // We have to do this custom so we can re-use the byte buf otherwise it gets padded with 0's!
                    RegistryFriendlyByteBuf(
                        Unpooled.buffer(),
                        serverPlayer.registryAccess(),
                    ).also {
                        if (type.jsonSerialized) {
                            val serializer =
                                JsonSerializerRegistry
                                    .getInstance()
                                    .getSerializer(type.clazz.getAnnotation(JsonSerializedPacket::class.java).value)
                            it.writeUtf(serializer.encode(payload, type.clazz))
                        } else {
                            // Use the stream codec of this payload type to encode it into the buffer
                            val serializer = PacketSerializerRegistry.getSerializers(type)
                            serializer.encode(it, payload)
                        }
                    }.let {
                        // Copy only the used bytes otherwise we send lingering empty data which crashes clients
                        val out = ByteArray(it.readableBytes())
                        System.arraycopy(it.array(), 0, out, 0, it.readableBytes())
                        out
                    },
                ),
            )
        (NoxesiumPlatform.getInstance() as? PaperPlatform)?.currentTargetPlayer = null
        if (capturePackets) {
            capturedPacket = packet
            capturePackets = false
        } else {
            serverPlayer.connection.send(packet)
        }
        return true
    }

    override fun markLazyActive(payloadType: NoxesiumPayloadType<*>) {
        // Inform all authenticated players directly about the newly un-lazy packet
        val packet = ClientboundLazyPacketsPacket(setOf(payloadType.id()))
        NoxesiumPlayerManager.getInstance().allPlayers.forEach {
            it.sendPacket(packet)
        }
    }
}
