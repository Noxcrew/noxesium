package com.noxcrew.noxesium.core.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent to the server to inform it that the client changed its
 * gliding state.
 */
public record ServerboundGlidePacket(boolean gliding) implements NoxesiumPacket {}
