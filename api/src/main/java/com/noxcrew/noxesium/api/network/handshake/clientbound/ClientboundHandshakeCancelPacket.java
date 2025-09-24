package com.noxcrew.noxesium.api.network.handshake.clientbound;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumErrorReason;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent to the client to inform it the handshake has been cancelled.
 *
 * @see NoxesiumEntrypoint for more information
 */
public record ClientboundHandshakeCancelPacket(NoxesiumErrorReason reason) implements NoxesiumPacket {}
