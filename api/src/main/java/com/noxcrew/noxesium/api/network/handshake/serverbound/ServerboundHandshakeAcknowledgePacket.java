package com.noxcrew.noxesium.api.network.handshake.serverbound;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.EntrypointProtocol;
import com.noxcrew.noxesium.api.network.ModInfo;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.handshake.clientbound.ClientboundHandshakeAcknowledgePacket;
import java.util.Collection;

/**
 * Sent back to the server with a list of entrypoints from the {@link ClientboundHandshakeAcknowledgePacket}
 * and their protocol information that have been registered. This also shares all mods installed by the client
 * and their versions. This is something Forge always used to do, so there's no precedent that a client's mods
 * should be private.
 *
 * @see NoxesiumEntrypoint for more information
 */
public record ServerboundHandshakeAcknowledgePacket(Collection<EntrypointProtocol> protocols, Collection<ModInfo> mods)
        implements NoxesiumPacket {}
