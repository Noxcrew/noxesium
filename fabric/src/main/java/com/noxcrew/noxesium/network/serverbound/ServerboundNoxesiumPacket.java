package com.noxcrew.noxesium.network.serverbound;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import com.noxcrew.noxesium.network.NoxesiumPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * A Noxesium packet that is sent by the client and handled on the server.
 */
public interface ServerboundNoxesiumPacket extends NoxesiumPacket {

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    default boolean send() {
        // We assume the server indicates which packets it wishes to receive, otherwise we do not send anything.
        if (ClientPlayNetworking.canSend(noxesiumType().type) && NoxesiumPackets.canSend(noxesiumType())) {
            ClientPlayNetworking.send(this);
            return true;
        }
        return false;
    }
}
