package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import com.noxcrew.noxesium.network.NoxesiumPayloadType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Resets the stored value for one or more server rules.
 */
public record ClientboundResetServerRulesPacket(IntList indices) implements NoxesiumPacket {
    public static final StreamCodec<FriendlyByteBuf, ClientboundResetServerRulesPacket> STREAM_CODEC = CustomPacketPayload.codec(ClientboundResetServerRulesPacket::write, ClientboundResetServerRulesPacket::new);
    public static final NoxesiumPayloadType<ClientboundResetServerRulesPacket> TYPE = NoxesiumPackets.client("reset_server_rules", STREAM_CODEC);

    private ClientboundResetServerRulesPacket(FriendlyByteBuf buf) {
        this(buf.readIntIdList());
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeIntIdList(indices);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return TYPE;
    }
}
