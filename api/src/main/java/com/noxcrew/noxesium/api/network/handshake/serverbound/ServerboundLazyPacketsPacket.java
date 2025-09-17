package com.noxcrew.noxesium.api.network.handshake.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.Collection;
import net.kyori.adventure.key.Key;

/**
 * Sent to a server to inform it which lazy packets it wants to receive.
 */
public record ServerboundLazyPacketsPacket(Collection<Key> packets) implements NoxesiumPacket {}
