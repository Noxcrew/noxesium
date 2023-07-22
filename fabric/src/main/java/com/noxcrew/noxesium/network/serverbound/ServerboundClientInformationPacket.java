package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent to the server when the client first joins to establish the version of the
 * client being used.
 */
public class ServerboundClientInformationPacket extends ServerboundNoxesiumPacket {

    private final int protocolVersion;

    public ServerboundClientInformationPacket(int protocolVersion) {
        super(1);
        this.protocolVersion = protocolVersion;
    }

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeVarInt(protocolVersion);
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.SERVER_CLIENT_INFO;
    }
}
