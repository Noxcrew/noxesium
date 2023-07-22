package com.noxcrew.noxesium.network.clientbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A Noxesium packet that is sent by the server and handled on the client.
 */
public abstract class ClientboundNoxesiumPacket extends NoxesiumPacket {

    /**
     * Creates a new clientbound Noxesium packet with the given version.
     *
     * @param version The version of this packet, this is always
     *                the first varint of any Noxesium packet and
     *                allows the contents of packets to change
     *                over time without much issue.
     */
    public ClientboundNoxesiumPacket(int version) {
        super(version);
    }

    /**
     * Handles the incoming packet. This is called on the render thread, and can safely
     * call client methods.
     */
    public abstract void receive(LocalPlayer player, PacketSender responseSender);

    @Override
    public final void write(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Clientbound Noxesium packets cannot be serialized!");
    }
}
