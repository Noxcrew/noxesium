package com.noxcrew.noxesium.api.network.handshake.serverbound;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent to the server to inform it the handshake has been cancelled.
 *
 * @see NoxesiumEntrypoint for more information
 */
public record ServerboundHandshakeCancelPacket() implements NoxesiumPacket {}
