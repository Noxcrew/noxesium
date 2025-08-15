package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.nms.network.NoxesiumClientboundNetworking
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType
import com.viaversion.viaversion.api.Via
import com.viaversion.viaversion.api.type.Types
import io.netty.buffer.Unpooled
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.Component
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

/** Implements clientbound networking for Paper. */
public class FabricPaperClientboundNetworking : NoxesiumClientboundNetworking() {
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
                    Via.getManager()
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
            override fun decode(buffer: RegistryFriendlyByteBuf): ItemStack =
                ItemStack.STREAM_CODEC.decode(buffer)

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
                    Via.getManager()
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

    override fun <T : NoxesiumPacket> createPayloadType(
        namespace: String,
        id: String,
        codec: StreamCodec<RegistryFriendlyByteBuf, T>,
        clientToServer: Boolean,
    ): NoxesiumPayloadType<T> {
        TODO("Not yet implemented")
    }

    override fun canSend(
        player: Player,
        type: NoxesiumPayloadType<*>,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun <T : NoxesiumPacket> send(
        player: Player,
        type: NoxesiumPayloadType<T>,
        payload: T,
    ): Boolean {
        TODO("Not yet implemented")
    }
}