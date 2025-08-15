package com.noxcrew.noxesium.api.nms.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Sets up the Noxesium networking system in the clientbound direction.
 */
public abstract class NoxesiumClientboundNetworking extends NoxesiumNetworking {
    private static NoxesiumClientboundNetworking instance;

    /**
     * Returns the singleton instance of this class.
     */
    public static NoxesiumClientboundNetworking getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get networking instance before it is defined");
        return instance;
    }

    /**
     * Sets the networking instance.
     */
    public static void setInstance(NoxesiumClientboundNetworking instance) {
        Preconditions.checkState(
                NoxesiumClientboundNetworking.instance == null, "Cannot set the networking instance twice!");
        NoxesiumClientboundNetworking.instance = instance;
        NoxesiumNetworking.setInstance(instance);
    }

    /**
     * Sends the given packet, automatically detects the type of the packet based on the registered packets.
     */
    public static boolean send(@NotNull Player player, @NotNull NoxesiumPacket packet) {
        var type = getInstance().getPacketTypes().get(packet.getClass());
        if (type == null) return false;
        return type.sendClientboundAny(player, packet);
    }

    /**
     * Checks if the connected server should be receiving packets of the given type.
     *
     * @param type The packet type
     * @return Whether the connected server should be receiving the packet
     */
    public abstract boolean canSend(@NotNull Player player, @NotNull NoxesiumPayloadType<?> type);

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    public abstract <T extends NoxesiumPacket> boolean send(@NotNull Player player, @NotNull NoxesiumPayloadType<T> type, @NotNull T payload);
}
