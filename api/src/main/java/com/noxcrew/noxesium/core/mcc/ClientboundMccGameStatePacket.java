package com.noxcrew.noxesium.core.mcc;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent by MCC Island whenever the game state changes.
 * Possible values for the phase type:
 * - loading
 * - waiting_for_players
 * - pre_game
 * - play
 * - intermission
 * - post_game
 * <p>
 * Values for the stage key differ based on the game.
 *
 * @param game        The type of game being played. This may be the same for different queues.
 * @param queueType   The type of queued game being played.
 * @param phaseType   The current type of phase.
 * @param stage       The current stage id.
 * @param round       The current round of the game.
 * @param totalRounds The total amount of rounds in the game.
 * @param mapId       The id assigned to the current map.
 * @param mapName     The display name of the current map localised to the client's language.
 */
public record ClientboundMccGameStatePacket(
        String game,
        String queueType,
        String phaseType,
        String stage,
        int round,
        int totalRounds,
        String mapId,
        String mapName)
        implements NoxesiumPacket {}
