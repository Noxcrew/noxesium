package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent by the client to tell them to change their gliding state immediately.
 */
public record ClientboundGlidePacket(boolean gliding) implements NoxesiumPacket {}
