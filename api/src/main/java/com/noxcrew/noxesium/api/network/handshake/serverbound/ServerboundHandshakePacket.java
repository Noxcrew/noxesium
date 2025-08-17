package com.noxcrew.noxesium.api.network.handshake.serverbound;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Map;

/**
 * Sent to the server with a set of entrypoints that are available.
 *
 * @see NoxesiumEntrypoint for more information
 */
public record ServerboundHandshakePacket(Map<String, String> entrypoints) implements NoxesiumPacket {}
