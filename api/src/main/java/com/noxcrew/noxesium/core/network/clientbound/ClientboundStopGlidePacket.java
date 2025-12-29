package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent by the client to tell them to stop gliding immediately.
 */
public class ClientboundStopGlidePacket implements NoxesiumPacket {
    public static ClientboundStopGlidePacket INSTANCE = new ClientboundStopGlidePacket();
}
