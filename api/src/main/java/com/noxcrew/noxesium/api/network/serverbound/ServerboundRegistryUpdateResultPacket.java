package com.noxcrew.noxesium.api.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import java.util.List;

/**
 * Sent to the server after receiving a registry update packet with information on which ids were not known.
 */
public record ServerboundRegistryUpdateResultPacket(int id, List<Integer> unknownKeys) implements NoxesiumPacket {}
