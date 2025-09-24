package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.network.handshake.HandshakePackets
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundLazyPacketsPacket
import com.noxcrew.noxesium.api.network.json.JsonSerializedPacket
import com.noxcrew.noxesium.api.network.json.JsonSerializerRegistry
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType
import com.noxcrew.noxesium.api.nms.NoxesiumPlatform
import com.noxcrew.noxesium.api.nms.serialization.PacketSerializerRegistry
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.paper.NoxesiumPaper
import com.noxcrew.noxesium.paper.PaperPlatform
import com.noxcrew.packet.PacketHandler
import io.netty.buffer.Unpooled
import io.papermc.paper.connection.PlayerCommonConnection
import io.papermc.paper.connection.PlayerConfigurationConnection
import io.papermc.paper.connection.PlayerGameConnection
import net.kyori.adventure.key.Key
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import net.minecraft.resources.ResourceLocation
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftPlayer
import java.util.logging.Level

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

    override fun unregister(payloadType: NoxesiumPayloadType<*>?) {
        super.unregister(payloadType)
        if (payloadType != null) {
            payloadTypes -= payloadType.id().asString()
        }
    }

    override fun <T : NoxesiumPacket> createPayloadType(
        namespace: String,
        id: String,
        clazz: Class<T>,
        clientToServer: Boolean,
    ): NoxesiumPayloadType<T> = NoxesiumPayloadType(Key.key(namespace, id), clazz, clientToServer)

    override fun canReceive(player: NoxesiumServerPlayer, type: NoxesiumPayloadType<*>): Boolean {
        // Prevent sending if the client does not know of this channel!
        val noxesiumPlayer = (player as PaperNoxesiumServerPlayer)
        if (!noxesiumPlayer.isConnected) return false
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
        val player = (player as PaperNoxesiumServerPlayer).player ?: return false
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
            val serializer = PacketSerializerRegistry.getSerializers(type)
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
        return true
    }

    /**
     * Capture payloads even during the configuration phase to prevent Bukkit from seeing anything
     * about any of the Noxesium plugin channels.
     */
    @PacketHandler
    public fun onCustomPayload(
        connection: PlayerCommonConnection,
        packet: ServerboundCustomPayloadPacket,
    ): ServerboundCustomPayloadPacket? {
        (packet.payload as? DiscardedPayload)?.also { payload ->
            // Ignore packets not for Noxesium!
            if (payload.id.namespace != NoxesiumReferences.PACKET_NAMESPACE) return@also

            // Determine the current Noxesium player and if they can send this type of packet
            val playerUUID =
                (connection as? PlayerConfigurationConnection)?.profile?.id ?: (connection as? PlayerGameConnection)?.player?.uniqueId
                    ?: return null
            val playerName =
                (connection as? PlayerConfigurationConnection)?.profile?.name ?: (connection as? PlayerGameConnection)?.player?.name
                    ?: playerUUID.toString()

            val noxesiumPlayer = NoxesiumPlayerManager.getInstance().getPlayer(playerUUID) as? PaperNoxesiumServerPlayer
            val knownChannels = noxesiumPlayer?.registeredPluginChannels ?: HandshakePackets.INSTANCE.pluginChannelIdentifiers
            val channel = payload.id.toString()
            if (channel !in knownChannels) {
                NoxesiumPaper.plugin.logger.log(
                    Level.WARNING,
                    "Received unauthorized plugin message on channel '$channel' for $playerName",
                )
                return null
            }

            // Determine the payload type for this packet if the client knows of it
            val payloadType = getPayloadType(channel) ?: return null

            try {
                // Decode the message and let handlers handle it
                val buffer =
                    ((connection as? PlayerGameConnection)?.player as? CraftPlayer)?.handle?.registryAccess()?.let { registryAccess ->
                        RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(payload.data), registryAccess)
                    } ?: return null

                val payload =
                    if (payloadType.jsonSerialized) {
                        val serializer =
                            JsonSerializerRegistry
                                .getInstance()
                                .getSerializer(payloadType.clazz.getAnnotation(JsonSerializedPacket::class.java).value)
                        serializer.decode(buffer.readUtf(), payloadType.clazz)
                    } else {
                        val codec = PacketSerializerRegistry.getSerializers(payloadType)
                        codec.decode(buffer)
                    }

                // Mark the payload as received
                noxesiumPlayer?.markPacketReceived()

                // Perform packet handling on the main thread
                ensureMain {
                    payloadType.handle(playerUUID, payload)
                }
            } catch (x: Exception) {
                NoxesiumPaper.plugin.logger.log(
                    Level.WARNING,
                    "Failed to decode plugin message on channel '$channel' for $playerName",
                    x,
                )
            }

            // Always hide Noxesium packets from Bukkit!
            return null
        }
        return packet
    }

    override fun markLazyActive(payloadType: NoxesiumPayloadType<*>) {
        // Inform all authenticated players directly about the newly un-lazy packet
        val packet = ClientboundLazyPacketsPacket(setOf(payloadType.id()))
        NoxesiumPlayerManager.getInstance().allPlayers.forEach {
            it.sendPacket(packet)
        }
    }

    /** Runs the given [function] delayed on the main thread. */
    private fun ensureMain(function: () -> Unit) {
        Bukkit.getScheduler().callSyncMethod(NoxesiumPaper.plugin) { function() }
    }
}
