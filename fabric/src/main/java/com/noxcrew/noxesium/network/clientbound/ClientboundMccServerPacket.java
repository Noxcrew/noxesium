package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by MCC Island whenever you switch servers. All values are dynamic and may change over time.
 */
public record ClientboundMccServerPacket(String serverType, String subType, String associatedGame) implements NoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundMccServerPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundMccServerPacket::write, ClientboundMccServerPacket::new);

    private ClientboundMccServerPacket(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUtf(serverType);
        buf.writeUtf(subType);
        buf.writeUtf(associatedGame);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return NoxesiumPackets.CLIENT_MCC_SERVER;
    }
}