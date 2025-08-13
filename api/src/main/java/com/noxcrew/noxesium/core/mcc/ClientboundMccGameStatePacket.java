package com.noxcrew.noxesium.core.mcc;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent by MCC Island whenever the game state changes.
 * Possible values for the phase type:
 * - LOADING
 * - WAITING_FOR_PLAYERS
 * - PRE_GAME
 * - PLAY
 * - INTERMISSION
 * - POST_GAME
 * <p>
 * Values for the stage key differ based on the game.
 * <p>
 * Clients should be able to parse any version of the packet so any server
 * can send any version.
 *
 * @param phaseType   The current type of phase.
 * @param stage       The current stage id.
 * @param round       The current round of the game.
 * @param totalRounds The total amount of rounds in the game.
 * @param mapId       The internal id assigned to the current map.
 * @param mapName     The display name of the current map localised to the client's language.
 */
public record ClientboundMccGameStatePacket(
        String phaseType, String stage, int round, int totalRounds, String mapId, String mapName)
        implements NoxesiumPacket {}
