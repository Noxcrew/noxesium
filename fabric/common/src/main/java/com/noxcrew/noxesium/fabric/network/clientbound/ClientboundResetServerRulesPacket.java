package com.noxcrew.noxesium.fabric.network.clientbound;

import com.noxcrew.noxesium.api.fabric.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.fabric.network.NoxesiumPayloadType;
import com.noxcrew.noxesium.fabric.network.CommonPackets;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Resets the stored value for one or more server rules.
 */
public record ClientboundResetServerRulesPacket(IntList indices) implements NoxesiumPacket {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundResetServerRulesPacket> STREAM_CODEC =
            CustomPacketPayload.codec(ClientboundResetServerRulesPacket::write, ClientboundResetServerRulesPacket::new);

    private ClientboundResetServerRulesPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readIntIdList());
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeIntIdList(indices);
    }

    @Override
    public NoxesiumPayloadType<?> noxesiumType() {
        return CommonPackets.INSTANCE.CLIENT_RESET_SERVER_RULES;
    }
}
