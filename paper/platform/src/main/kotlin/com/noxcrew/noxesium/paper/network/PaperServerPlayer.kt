package com.noxcrew.noxesium.paper.network

import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer
import com.noxcrew.noxesium.api.player.SerializedNoxesiumServerPlayer
import net.minecraft.server.level.ServerPlayer

/** A variant of a Noxesium player that holds a reference to a server player. */
public class PaperServerPlayer(
    /** The nms player instance. */
    public val player: ServerPlayer,
    serializedPlayer: SerializedNoxesiumServerPlayer? = null,
) : NoxesiumServerPlayer(player.uuid, player.gameProfile.name, player.`adventure$displayName`, serializedPlayer)
