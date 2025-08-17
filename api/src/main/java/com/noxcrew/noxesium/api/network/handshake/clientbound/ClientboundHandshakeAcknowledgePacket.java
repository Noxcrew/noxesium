package com.noxcrew.noxesium.api.network.handshake.clientbound;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.handshake.serverbound.ServerboundHandshakePacket;
import java.util.Map;

/**
 * Sent back to the client with a list of entrypoints from the {@link ServerboundHandshakePacket}
 * that it knows about and wants to register.
 *
 * @see NoxesiumEntrypoint for more information
 */
public record ClientboundHandshakeAcknowledgePacket(Map<String, String> entrypoints) implements NoxesiumPacket {}
