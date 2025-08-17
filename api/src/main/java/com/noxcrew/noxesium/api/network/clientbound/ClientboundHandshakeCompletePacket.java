package com.noxcrew.noxesium.api.network.clientbound;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent back to the client to inform it handshaking has completed.
 *
 * @see NoxesiumEntrypoint for more information
 */
public record ClientboundHandshakeCompletePacket() implements NoxesiumPacket {}
