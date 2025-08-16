package com.noxcrew.noxesium.api.nms.network.payload;

import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * The basis for a custom payload used by Noxesium.
 */
public record NoxesiumPayload<T extends NoxesiumPacket>(NoxesiumPayloadType<T> noxesiumType, T value)
        implements CustomPacketPayload {

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return noxesiumType().type;
    }
}
