package com.noxcrew.noxesium.paper.v1

import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.api.protocol.NoxesiumFeature
import com.noxcrew.noxesium.paper.api.BaseNoxesiumListener
import com.noxcrew.noxesium.paper.api.NoxesiumManager
import com.noxcrew.noxesium.paper.api.createPayloadPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets.clientboundPackets
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets.serverboundPackets
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundChangeServerRulesPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundMccGameStatePacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundMccServerPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundResetPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundResetServerRulesPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundServerInformationPacket
import com.noxcrew.noxesium.paper.api.network.serverbound.handle
import com.noxcrew.noxesium.paper.api.readPluginMessage
import it.unimi.dsi.fastutil.ints.IntImmutableList
import net.kyori.adventure.key.Key
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.bukkit.plugin.Plugin
import org.slf4j.Logger

/**
 * Listens to Noxesium clients running version v1.0.0.
 */
public class NoxesiumListenerV1(
    plugin: Plugin,
    logger: Logger,
    manager: NoxesiumManager,
) : BaseNoxesiumListener(plugin, logger, manager) {
    public companion object {
        /** The namespace under which all packets are registered. Appended by a global API version equal to the major version of Noxesium. */
        public const val PACKET_NAMESPACE: String = "${NoxesiumReferences.NAMESPACE}-v1"
    }

    init {
        // Register all packets, both client-bound and server-bound
        for (type in clientboundPackets.values) {
            registerOutgoingPluginChannel(Key.key(PACKET_NAMESPACE, type.id))
        }
        for (type in serverboundPackets.values) {
            registerIncomingPluginChannel(Key.key(PACKET_NAMESPACE, type.id)) { _, player, data ->
                data.readPluginMessage { buffer ->
                    // Read legacy version integer
                    buffer.readVarInt()

                    type.reader?.let { it(buffer, player, manager.getProtocolVersion(player) ?: 0) }?.handle(player)
                }
            }
        }
    }

    override fun createPacket(
        player: Player,
        packet: NoxesiumPacket,
    ): ClientboundCustomPayloadPacket? =
        player.createPayloadPacket(Key.key(PACKET_NAMESPACE, packet.type.id)) { buffer ->
            when (packet) {
                is ClientboundChangeServerRulesPacket -> {
                    buffer.writeVarInt(1)
                    val values = packet.writers.entries.toList()
                    val indices = values.map { it.key }
                    buffer.writeIntIdList(IntImmutableList(indices))
                    for (entry in values) {
                        entry.value(buffer)
                    }
                }

                is ClientboundResetServerRulesPacket -> {
                    buffer.writeVarInt(1)
                    buffer.writeIntIdList(packet.indices)
                }

                is ClientboundResetPacket -> {
                    buffer.writeVarInt(1)
                    buffer.writeByte(packet.flags.toInt())
                }

                is ClientboundMccGameStatePacket -> {
                    val protocolVersion = manager.getProtocolVersion(player) ?: 0
                    if (protocolVersion >= NoxesiumFeature.CUSTOM_SOUNDS.minProtocolVersion) {
                        buffer.writeVarInt(2)
                    } else {
                        buffer.writeVarInt(1)
                    }

                    buffer.writeUtf(packet.phaseType)
                    buffer.writeUtf(packet.stageKey)
                    if (protocolVersion >= NoxesiumFeature.CUSTOM_SOUNDS.minProtocolVersion) {
                        buffer.writeVarInt(packet.round)
                        buffer.writeVarInt(packet.totalRounds)
                        buffer.writeUtf(packet.mapId)
                        buffer.writeUtf(packet.mapName)
                    }
                }

                is ClientboundMccServerPacket -> {
                    buffer.writeVarInt(1)
                    buffer.writeUtf(packet.serverType)
                    buffer.writeUtf(packet.subType)
                    buffer.writeUtf(packet.game ?: "")
                    buffer.writeUtf("unused")
                }

                is ClientboundServerInformationPacket -> {
                    buffer.writeVarInt(1)
                    buffer.writeVarInt(packet.maxProtocolVersion)
                }
            }
        }

    @EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        if (event.channel != Key.key(PACKET_NAMESPACE, NoxesiumPackets.CLIENT_SERVER_INFO.id).asString()) return
        manager.markReady(event.player)
    }
}
