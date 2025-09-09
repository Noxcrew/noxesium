package com.noxcrew.noxesium.api.network.handshake.clientbound;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent back to the client to inform it that it is now connected to a different
 * server that has carried across the handshake. Clears any information on the
 * client that the new server cannot reasonably know about.
 *
 * @see NoxesiumEntrypoint for more information
 */
public record ClientboundHandshakeTransferredPacket() implements NoxesiumPacket {}
