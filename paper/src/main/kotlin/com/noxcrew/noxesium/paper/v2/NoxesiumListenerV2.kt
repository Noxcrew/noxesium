package com.noxcrew.noxesium.paper.v2

import com.noxcrew.noxesium.api.protocol.ProtocolVersion
import com.noxcrew.noxesium.paper.api.BaseNoxesiumListener
import com.noxcrew.noxesium.paper.api.NoxesiumManager
import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets.clientboundPackets
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets.serverboundPackets
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundChangeServerRulesPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundCustomSoundModifyPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundCustomSoundStartPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundCustomSoundStopPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundMccGameStatePacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundMccServerPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundResetPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundResetServerRulesPacket
import com.noxcrew.noxesium.paper.api.network.clientbound.ClientboundServerInformationPacket
import com.noxcrew.noxesium.paper.api.network.serverbound.handle
import it.unimi.dsi.fastutil.ints.IntImmutableList
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.slf4j.Logger

/**
 * Listens to Noxesium clients running version v2.0.0.
 */
public class NoxesiumListenerV2(
    plugin: Plugin,
    logger: Logger,
    manager: NoxesiumManager,
) : BaseNoxesiumListener(plugin, logger, manager) {

    public companion object {
        /** The namespace under which all packets are registered. Appended by a global API version equal to the major version of Noxesium. */
        public const val PACKET_NAMESPACE: String = "${ProtocolVersion.NAMESPACE}-v2"
    }

    init {
        // Register all packets, both client-bound and server-bound
        for (type in clientboundPackets.values) {
            registerOutgoingPluginChannel(Key.key(PACKET_NAMESPACE, type.id))
        }
        for (type in serverboundPackets.values) {
            registerIncomingPluginChannel(Key.key(PACKET_NAMESPACE, type.id)) { _, player, data ->
                data.readPluginMessage { buffer ->
                    type.reader?.let { it(buffer, player, manager.getProtocolVersion(player) ?: 0) }?.handle(player)
                }
            }
        }
    }

    override fun sendPacket(player: Player, packet: NoxesiumPacket): Unit = player.sendPluginMessage(Key.key(PACKET_NAMESPACE, packet.type.id)) { buffer ->
        when (packet) {
            is ClientboundChangeServerRulesPacket -> {
                val values = packet.writers.entries.toList()
                val indices = values.map { it.key }
                buffer.writeIntIdList(IntImmutableList(indices))
                for (entry in values) {
                    entry.value(buffer)
                }
            }

            is ClientboundResetServerRulesPacket -> {
                buffer.writeIntIdList(packet.indices)
            }

            is ClientboundResetPacket -> {
                buffer.writeByte(packet.flags.toInt())
            }

            is ClientboundMccGameStatePacket -> {
                buffer.writeUtf(packet.phaseType)
                buffer.writeUtf(packet.stageKey)
                buffer.writeVarInt(packet.round)
                buffer.writeVarInt(packet.totalRounds)
                buffer.writeUtf(packet.mapId)
                buffer.writeUtf(packet.mapName)
            }

            is ClientboundMccServerPacket -> {
                buffer.writeUtf(packet.serverType)
                buffer.writeUtf(packet.subType)
                buffer.writeUtf(packet.game ?: "")
            }

            is ClientboundServerInformationPacket -> {
                buffer.writeVarInt(packet.maxProtocolVersion)
            }

            is ClientboundCustomSoundStartPacket -> {
                buffer.writeVarInt(packet.id)
                buffer.writeResourceLocation(packet.sound)
                buffer.writeEnum(packet.source)
                buffer.writeBoolean(packet.looping)
                buffer.writeBoolean(packet.attenuation)
                buffer.writeBoolean(packet.ignoreIfPlaying)
                buffer.writeFloat(packet.volume)
                buffer.writeFloat(packet.pitch)

                if (packet.position != null) {
                    buffer.writeVarInt(0)
                    buffer.writeDouble(packet.position.x.toDouble())
                    buffer.writeDouble(packet.position.y.toDouble())
                    buffer.writeDouble(packet.position.z.toDouble())
                } else if (packet.entityId != null) {
                    buffer.writeVarInt(1)
                    buffer.writeVarInt(packet.entityId)
                } else {
                    buffer.writeVarInt(2)
                }
                if (packet.unix != null) {
                    buffer.writeBoolean(true)
                    buffer.writeLong(packet.unix)
                } else {
                    buffer.writeBoolean(false)
                    buffer.writeFloat(packet.offset ?: 0f)
                }
            }

            is ClientboundCustomSoundModifyPacket -> {
                buffer.writeVarInt(packet.id)
                buffer.writeFloat(packet.volume)
                buffer.writeVarInt(packet.interpolationTicks)
                if (packet.startVolume == null) {
                    buffer.writeBoolean(false)
                } else {
                    buffer.writeBoolean(true)
                    buffer.writeFloat(packet.startVolume)
                }
            }

            is ClientboundCustomSoundStopPacket -> {
                buffer.writeVarInt(packet.id)
            }
        }
    }
}