package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.nms.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.api.nms.network.NoxesiumPlayerManager
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.type.Types
import io.netty.buffer.Unpooled
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.DiscardedPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

/** Implements clientbound networking for Paper. */
public class PaperNoxesiumClientboundNetworking : NoxesiumClientboundNetworking() {
    /**
     * The player that the current packet is being serialized for.
     * This has to be set to properly rewrite packets to this player's protocol version.
     */
    private var currentTargetPlayer: Player? = null

    override fun getComponentStreamCodec(): StreamCodec<RegistryFriendlyByteBuf, Component> =
        object : StreamCodec<RegistryFriendlyByteBuf, Component> {
            override fun decode(buffer: RegistryFriendlyByteBuf): Component =
                PaperAdventure.WRAPPER_AWARE_SERIALIZER.deserialize(ComponentSerialization.STREAM_CODEC.decode(buffer))

            override fun encode(buffer: RegistryFriendlyByteBuf, component: Component) {
                val target = currentTargetPlayer
                if (target == null) {
                    ComponentSerialization.STREAM_CODEC.encode(buffer, PaperAdventure.WRAPPER_AWARE_SERIALIZER.serialize(component))
                } else {
                    // Write the ItemStack to a temporary buffer
                    val tempBuffer = Unpooled.buffer()
                    ComponentSerialization.STREAM_CODEC.encode(
                        RegistryFriendlyByteBuf(tempBuffer, buffer.registryAccess()),
                        PaperAdventure.WRAPPER_AWARE_SERIALIZER.serialize(component),
                    )

                    // Process the temporary buffer using ViaVersion
                    val text = Types.COMPONENT.read(tempBuffer)

                    // Parse the text to the intended destination format
                    val connection = Via.getAPI().getConnection(target.uuid)
                    Via
                        .getManager()
                        .protocolManager
                        .getProtocolPath(
                            Via.getAPI().getPlayerProtocolVersion(target.uuid),
                            Via
                                .getManager()
                                .protocolManager.serverProtocolVersion
                                .highestSupportedProtocolVersion(),
                        )?.forEach { protocol ->
                            protocol.protocol().componentRewriter?.processText(connection, text)
                        }

                    // Write the destination item to the buffer
                    Types.COMPONENT.write(buffer, text)
                }
            }
        }

    override fun getItemStackStreamCodec(): StreamCodec<RegistryFriendlyByteBuf, ItemStack> =
        object : StreamCodec<RegistryFriendlyByteBuf, ItemStack> {
            override fun decode(buffer: RegistryFriendlyByteBuf): ItemStack = ItemStack.STREAM_CODEC.decode(buffer)

            override fun encode(buffer: RegistryFriendlyByteBuf, itemStack: ItemStack) {
                val target = currentTargetPlayer
                if (target == null) {
                    ItemStack.STREAM_CODEC.encode(buffer, itemStack)
                } else {
                    // Write the ItemStack to a temporary buffer
                    val tempBuffer = Unpooled.buffer()
                    ItemStack.STREAM_CODEC.encode(
                        RegistryFriendlyByteBuf(tempBuffer, buffer.registryAccess()),
                        itemStack,
                    )

                    // Process the temporary buffer using ViaVersion
                    var item = Types.ITEM1_20_2.read(tempBuffer)

                    // Parse the item to the intended destination format
                    val connection = Via.getAPI().getConnection(target.uuid)
                    Via
                        .getManager()
                        .protocolManager
                        .getProtocolPath(
                            Via.getAPI().getPlayerProtocolVersion(target.uuid),
                            Via
                                .getManager()
                                .protocolManager.serverProtocolVersion
                                .highestSupportedProtocolVersion(),
                        )?.forEach { protocol ->
                            protocol.protocol().itemRewriter?.handleItemToClient(connection, item)?.let {
                                item = it
                            }
                        }

                    // Write the destination item to the buffer
                    Types.ITEM1_20_2.write(buffer, item)
                }
            }
        }

    override fun getRegisteredChannels(player: ServerPlayer): Collection<String> = player.bukkitEntity.listeningPluginChannels

    override fun <T : NoxesiumPacket> createPayloadType(
        namespace: String,
        id: String,
        codec: StreamCodec<RegistryFriendlyByteBuf, T>,
        clazz: Class<T>,
        clientToServer: Boolean,
    ): NoxesiumPayloadType<T> = PaperNoxesiumPayloadType(ResourceLocation.fromNamespaceAndPath(namespace, id), codec, clazz, clientToServer)

    override fun canReceive(player: Player, type: NoxesiumPayloadType<*>): Boolean {
        val serverPlayer = player as ServerPlayer
        if (serverPlayer.connection == null) return false

        // Prevent sending if the client does not know of this channel!
        if (type.id().toString() !in serverPlayer.bukkitEntity.listeningPluginChannels) return false

        // Prevent sending if the entrypoint that registered this packet is not known to this player!
        // This avoids situations where the client somehow has the correct channel without authenticating
        // properly with that endpoint.
        // If there is no entrypoint (or null which is for handshake packets) we always allow it!
        val entrypoint = entrypoints[type]?.getOrNull() ?: return true
        val playerData = NoxesiumPlayerManager.getInstance().getPlayer(player.uuid) ?: return false
        return entrypoint.id in playerData.supportedEntrypointIds
    }

    override fun <T : NoxesiumPacket> send(player: Player, type: NoxesiumPayloadType<T>, payload: T): Boolean {
        // Check if we're allowed to send it!
        if (!canReceive(player, type)) return false

        // Force serialization of the packet to happen right here so we can set the target player
        // for the stream codecs! Do not defer this to happen later in the netty thread. We do this just so
        // we can apply the ViaVersion codecs defined above, serializing on a separate thread is fine
        // for any other implementations that do not need such a technique.
        currentTargetPlayer = player
        val packet =
            ClientboundCustomPayloadPacket(
                DiscardedPayload(
                    type.id(),
                    // We have to do this custom so we can re-use the byte buf otherwise it gets padded with 0's!
                    RegistryFriendlyByteBuf(
                        Unpooled.buffer(),
                        player.registryAccess(),
                    ).also {
                        // Use the stream codec of this payload type to encode it into the buffer
                        type.codec.encode(it, payload)
                    }.let {
                        // Copy only the used bytes otherwise we send lingering empty data which crashes clients
                        val out = ByteArray(it.readableBytes())
                        System.arraycopy(it.array(), 0, out, 0, it.readableBytes())
                        out
                    },
                ),
            )
        currentTargetPlayer = null
        (player as? ServerPlayer)?.connection?.send(packet)
        return true
    }
}
