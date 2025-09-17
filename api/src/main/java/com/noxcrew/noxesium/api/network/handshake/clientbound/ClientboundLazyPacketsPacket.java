package com.noxcrew.noxesium.api.network.handshake.clientbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Collection;
import net.kyori.adventure.key.Key;

/**
 * Sent to a client to inform it which lazy packets it wants to receive.
 */
public record ClientboundLazyPacketsPacket(Collection<Key> packets) implements NoxesiumPacket {}
