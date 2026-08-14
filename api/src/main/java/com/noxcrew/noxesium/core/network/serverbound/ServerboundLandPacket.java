package com.noxcrew.noxesium.core.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent to the server to inform it that the client touched the ground
 * while gliding. Depending on the coyote time configured this may or
 * may not cause them to stop gliding.
 */
public record ServerboundLandPacket() implements NoxesiumPacket {
    public static final ServerboundLandPacket INSTANCE = new ServerboundLandPacket();
}
