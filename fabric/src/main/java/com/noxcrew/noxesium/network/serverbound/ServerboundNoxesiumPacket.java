package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A Noxesium packet that is sent by the client and handled on the server.
 */
public abstract class ServerboundNoxesiumPacket extends NoxesiumPacket {

    /**
     * Creates a new serverbound Noxesium packet with the given version.
     *
     * @param version The version of this packet, this is always
     *                the first varint of any Noxesium packet and
     *                allows the contents of packets to change
     *                over time without much issue.
     */
    public ServerboundNoxesiumPacket(int version) {
        super(version);
    }

    @Override
    public final void write(FriendlyByteBuf buf) {
        buf.writeVarInt(version);
        serialize(buf);
    }

    /**
     * Writes this packet into the given buffer.
     *
     * @param buffer The friendly byte buffer to write the packet into.
     *               The version varyint has already been written.
     */
    public abstract void serialize(FriendlyByteBuf buffer);

    /**
     * Writes this packet into the given buffer as the selected legacy version.
     *
     * @param version The version to serialize this packet as, it is guaranteed to be
     *                below the version of the packet.
     * @param buffer The buffer to serialize to, the intended version varint has already
     *               been written.
     */
    public void legacySerialize(int version, FriendlyByteBuf buffer) {
        throw new UnsupportedOperationException("Packet " + getClass().getSimpleName() + " does not support legacy serialization as version version");
    }

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    public boolean send() {
        // We assume the server indicates which packets it wishes to receive, otherwise we do not send anything.
        if (ClientPlayNetworking.canSend(getType()) && NoxesiumPackets.canSend(getType())) {
            // TODO Determine the latest protocol version the server has indicated it supports and
            // possibly use legacy serialization!

            var buffer = PacketByteBufs.create();
            buffer.writeVarInt(version);
            serialize(buffer);
            ClientPlayNetworking.send(getType().getId(), buffer);
            return true;
        }
        return false;
    }
}
