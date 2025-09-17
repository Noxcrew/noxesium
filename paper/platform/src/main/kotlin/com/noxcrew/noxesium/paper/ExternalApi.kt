package com.noxcrew.noxesium.paper

import com.noxcrew.noxesium.api.ExternalNoxesiumApi
import com.noxcrew.noxesium.api.player.NoxesiumPlayerManager
import java.util.UUID

/** Implements the external API. */
public class ExternalApi : ExternalNoxesiumApi() {
    override fun isUsingNoxesium(playerId: UUID): Boolean = NoxesiumPlayerManager.getInstance().getPlayer(playerId) != null

    override fun getInstalledMods(playerId: UUID): Map<String, String> {
        // Return the installed mods if they are known
        NoxesiumPlayerManager
            .getInstance()
            .getPlayer(playerId)
            ?.mods
            ?.also { return it }
        return emptyMap()
    }
}
