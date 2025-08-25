package com.noxcrew.noxesium.core.network.serverbound;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;

/**
 * Sent to the server to inform it that it just riptided. More accurate than the server
 * running equal logic to check if the player is charging the riptide and in water.
 */
public record ServerboundRiptidePacket(int slot) implements NoxesiumPacket {}
