package com.noxcrew.noxesium.api.network.serverbound;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.clientbound.ClientboundHandshakeAcknowledgePacket;
import java.util.List;

/**
 * Sent back to the server with a list of entrypoints from the {@link ClientboundHandshakeAcknowledgePacket}
 * and their protocol information that have been registered.
 *
 * @see NoxesiumEntrypoint for more information
 */
public record ServerboundHandshakeAcknowledgePacket(List<EntrypointProtocol> protocols) implements NoxesiumPacket {}
