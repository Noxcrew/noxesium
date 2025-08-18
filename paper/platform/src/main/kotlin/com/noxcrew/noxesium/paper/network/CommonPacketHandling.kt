package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.feature.NoxesiumFeature
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import com.noxcrew.noxesium.core.network.CommonPackets

/** Registers default listeners for base packets. */
public class CommonPacketHandling : NoxesiumFeature() {
    init {
        // Store any updates made to the client settings
        CommonPackets.SERVER_CLIENT_SETTINGS.addListener(this) { reference, packet, playerId ->
            if (!isRegistered) return@addListener
            val player = NoxesiumPlayerManager.getInstance().getPlayer(playerId) ?: return@addListener
            player.updateClientSettings(packet.settings)
        }
    }
}
