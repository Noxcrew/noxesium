package com.noxcrew.noxesium.paper.api.network.clientbound

import com.noxcrew.noxesium.paper.api.network.NoxesiumPacket
import com.noxcrew.noxesium.paper.api.network.NoxesiumPackets

/**
 * Sent by MCC Island whenever the game state changes.
 * Possible values for the phase type:
 * - LOADING
 * - WAITING_FOR_PLAYERS
 * - PRE_GAME
 * - PLAY
 * - INTERMISSION
 * - POST_GAME
 *
 * Values for the stage key differ based on the game.
 */
public data class ClientboundMccGameStatePacket(
    public val phaseType: String,
    public val stageKey: String,
    public val round: Int,
    public val totalRounds: Int,
    public val mapId: String,
    public val mapName: String,
) : NoxesiumPacket(NoxesiumPackets.CLIENT_MCC_GAME_STATE)
