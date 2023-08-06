package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.NoxesiumMod;
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
     * Creates a new serverbound Noxesium packet with the given latest version.
     *
     * @param latestVersion The latest version of this packet, this is always
     *                      the first varint of any Noxesium packet and
     *                      allows the contents of packets to change
     *                      over time without much issue.
     */
    public ServerboundNoxesiumPacket(int latestVersion) {
        super(latestVersion);
    }

    @Override
    public final void write(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Cannot directly write a ServerboundNoxesiumPacket, use send()");
    }

    /**
     * Writes this packet into the given buffer.
     *
     * @param buffer The friendly byte buffer to write the packet into.
     *               The version varyint has already been written.
     */
    public abstract void serialize(FriendlyByteBuf buffer);

    /**
     * Returns the maximum packet version this packet should be serialized
     * as based on the given protocol version. This is the protocol version
     * used by the server, e.g. if this is 3 and the packet being serialized
     * was v1 until protocol 4 and v2 after then this should return 1.
     */
    public Integer getVersion(int protocolVersion) {
        return version;
    }

    /**
     * Writes this packet into the given buffer as the selected legacy version.
     *
     * @param version The version to serialize this packet as, it is guaranteed to be
     *                below the version of the packet.
     * @param buffer  The buffer to serialize to, the intended version varint has already
     *                been written.
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
            var maxProtocol = NoxesiumMod.getMaxProtocolVersion();
            var maxVersion = getVersion(maxProtocol);
            if (maxVersion == null) {
                // If the server does not know how to handle this packet we don't send it!
                return false;
            }

            var buffer = PacketByteBufs.create();
            if (maxVersion >= version) {
                buffer.writeVarInt(version);
                serialize(buffer);
            } else {
                buffer.writeInt(maxVersion);
                legacySerialize(maxVersion, buffer);
            }
            ClientPlayNetworking.send(getType().getId(), buffer);
            return true;
        }
        return false;
    }
}
