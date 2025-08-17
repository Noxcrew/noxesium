package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.NoxesiumEntrypoint
import com.noxcrew.noxesium.api.network.NoxesiumPacket
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType
import com.noxcrew.noxesium.paper.NoxesiumPaper
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit

/**
 * A paper-sided implementation of the payload type.
 */
public class PaperPayloadType<T : NoxesiumPacket>(
    id: Key,
    clazz: Class<T>,
    clientToServer: Boolean,
) : NoxesiumPayloadType<T>(id, clazz, clientToServer) {
    private val listener = PaperPacketHandler(this)

    override fun register(entrypoint: NoxesiumEntrypoint?) {
        super.register(entrypoint)

        if (clientToServer) {
            Bukkit.getMessenger().registerIncomingPluginChannel(NoxesiumPaper.plugin, id().toString(), listener)
        } else {
            Bukkit.getMessenger().registerOutgoingPluginChannel(NoxesiumPaper.plugin, id().toString())
        }
    }

    override fun unregister() {
        super.unregister()

        if (clientToServer) {
            Bukkit.getMessenger().unregisterIncomingPluginChannel(NoxesiumPaper.plugin, id().toString(), listener)
        } else {
            Bukkit.getMessenger().unregisterOutgoingPluginChannel(NoxesiumPaper.plugin, id().toString())
        }
    }
}
