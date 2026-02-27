package com.noxcrew.noxesium.legacy.paper.v0

import com.noxcrew.noxesium.core.feature.ClientSettings
import com.noxcrew.noxesium.legacy.paper.api.BaseNoxesiumListener
import com.noxcrew.noxesium.legacy.paper.api.NoxesiumManager
import com.noxcrew.noxesium.legacy.paper.api.createPayloadPacket
import com.noxcrew.noxesium.legacy.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.legacy.paper.api.network.clientbound.ClientboundChangeServerRulesPacket
import com.noxcrew.noxesium.legacy.paper.api.network.clientbound.ClientboundResetServerRulesPacket
import com.noxcrew.noxesium.legacy.paper.api.network.serverbound.ServerboundClientInformationPacket
import com.noxcrew.noxesium.legacy.paper.api.network.serverbound.ServerboundClientSettingsPacket
import com.noxcrew.noxesium.legacy.paper.api.network.serverbound.handle
import com.noxcrew.noxesium.legacy.paper.api.readPluginMessage
import net.kyori.adventure.key.Key
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerRegisterChannelEvent
import org.bukkit.plugin.Plugin
import org.slf4j.Logger

/**
 * Listens to Noxesium clients running versions before v1.0.0.
 */
public class NoxesiumListenerV0(
    plugin: Plugin,
    logger: Logger,
    manager: NoxesiumManager,
) : BaseNoxesiumListener(plugin, logger, manager) {
    public companion object {
        /** The name of the plugin channel used for setting server rules. */
        public val NOXESIUM_RULES_CHANNEL: Key = Key.key("$NOXESIUM_NAMESPACE:server_rules")
    }

    init {
        registerIncomingPluginChannel(Key.key(NOXESIUM_NAMESPACE, "client_information")) { _, player, data ->
            data.readPluginMessage { buffer ->
                val version = buffer.readInt()
                ServerboundClientInformationPacket(version, "unknown").handle(player)
            }
        }

        registerIncomingPluginChannel(Key.key(NOXESIUM_NAMESPACE, "client_settings")) { _, player, data ->
            data.readPluginMessage { buffer ->
                val guiScale = buffer.readInt()
                val enforceUnicode = buffer.readBoolean()
                val settings =
                    ClientSettings(
                        guiScale,
                        0.0,
                        0,
                        0,
                        enforceUnicode,
                        false,
                        0.0,
                    )
                ServerboundClientSettingsPacket(settings).handle(player)
            }
        }

        registerOutgoingPluginChannel(NOXESIUM_RULES_CHANNEL)
    }

    override fun createPacket(player: Player, packet: NoxesiumPacket): ClientboundCustomPayloadPacket? =
        if (packet is ClientboundResetServerRulesPacket) {
            player.createPayloadPacket(NOXESIUM_RULES_CHANNEL) { buffer ->
                buffer.writeVarIntArray(packet.indices.toIntArray())
                buffer.writeInt(0)
            }
        } else if (packet is ClientboundChangeServerRulesPacket) {
            player.createPayloadPacket(NOXESIUM_RULES_CHANNEL) { buffer ->
                buffer.writeVarIntArray(IntArray(0))
                buffer.writeInt(packet.writers.size)

                for ((id, writer) in packet.writers) {
                    buffer.writeInt(id)
                    writer(buffer)
                }
            }
        } else {
            null
        }

    @EventHandler
    public fun onChannelRegistered(event: PlayerRegisterChannelEvent) {
        if (event.channel != NOXESIUM_RULES_CHANNEL.asString()) return
        manager.markReady(event.player)
    }
}
