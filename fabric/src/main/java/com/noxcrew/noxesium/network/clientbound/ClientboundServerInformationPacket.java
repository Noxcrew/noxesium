package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.NoxesiumMod;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent to the client when the server is first informed of it existing, this contains information
 * about what protocol version the server supports.
 */
public class ClientboundServerInformationPacket extends ClientboundNoxesiumPacket {

    private final int maxProtocolVersion;

    public ClientboundServerInformationPacket(FriendlyByteBuf buf) {
        super(buf.readVarInt());
        this.maxProtocolVersion = buf.readVarInt();
    }

    @Override
    public void receive(LocalPlayer player, PacketSender responseSender) {
        // Whenever the server sends information about the supported protocol version we store
        // that and can use it to downgrade server-side packets based on the newest version
        // that can be sent.
        NoxesiumMod.getInstance().setServerVersion(maxProtocolVersion);
    }

    @Override
    public PacketType<?> getType() {
        return NoxesiumPackets.CLIENT_SERVER_INFO;
    }
}
