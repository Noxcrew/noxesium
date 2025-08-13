package com.noxcrew.noxesium.core.network.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent by a server to stop a custom sound by its id.
 */
public record ClientboundCustomSoundStopPacket(int id) implements NoxesiumPacket {}
