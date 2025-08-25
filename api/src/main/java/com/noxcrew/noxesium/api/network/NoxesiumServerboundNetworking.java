package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;

/**
 * Sets up the Noxesium networking system in the serverbound direction.
 */
public abstract class NoxesiumServerboundNetworking extends NoxesiumNetworking {
    /**
     * Returns the singleton instance of this class.
     */
    public static NoxesiumServerboundNetworking getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get networking instance before it is defined");
        return (NoxesiumServerboundNetworking) instance;
    }

    /**
     * Sends the given packet, automatically detects the type of the packet based on the registered packets.
     */
    public static boolean send(NoxesiumPacket packet) {
        var type = getInstance().getPacketTypes().get(packet.getClass());
        if (type == null) return false;
        return type.sendServerboundAny(packet);
    }

    /**
     * Checks if the connected server should be receiving packets of the given type.
     *
     * @param packet The packet class
     * @return Whether the connected server should be receiving the packet
     */
    public boolean canSend(Class<?> packet) {
        var type = packetTypes.get(packet);
        if (type == null) return false;
        return canSend(type);
    }

    /**
     * Checks if the connected server should be receiving packets of the given type.
     *
     * @param type The packet type
     * @return Whether the connected server should be receiving the packet
     */
    public abstract boolean canSend(NoxesiumPayloadType<?> type);

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    public abstract <T extends NoxesiumPacket> boolean send(NoxesiumPayloadType<T> type, T payload);
}
