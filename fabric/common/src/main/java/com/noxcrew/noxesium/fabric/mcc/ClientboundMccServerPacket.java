package com.noxcrew.noxesium.fabric.mcc;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by MCC Island whenever you switch servers. All values are dynamic and may change over time.
 */
public record ClientboundMccServerPacket(String serverType, String subType, String associatedGame)
        implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMccServerPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundMccServerPacket::write, ClientboundMccServerPacket::new);

    private ClientboundMccServerPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUtf(), buf.readUtf());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(serverType);
        buf.writeUtf(subType);
        buf.writeUtf(associatedGame);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return MccPackets.INSTANCE.CLIENT_MCC_SERVER;
    }
}
