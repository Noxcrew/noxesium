package com.noxcrew.noxesium.api.fabric.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * The basis for a custom payload as used by Noxesium.
 */
public interface NoxesiumPacket extends CustomPacketPayload {

    /**
     * Returns the Noxesium payload type of this packet.
     */
    NoxesiumPayloadType<?> noxesiumType();

    /**
     * Override the super-method's type.
     */
    @NotNull
    @Override
    default Type<? extends CustomPacketPayload> type() {
        return noxesiumType().type;
    }
}
