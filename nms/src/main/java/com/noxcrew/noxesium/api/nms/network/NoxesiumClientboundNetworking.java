package com.noxcrew.noxesium.api.nms.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sets up the Noxesium networking system in the clientbound direction.
 */
public abstract class NoxesiumClientboundNetworking extends NoxesiumNetworking {
    /**
     * Returns the singleton instance of this class.
     */
    public static NoxesiumClientboundNetworking getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get networking instance before it is defined");
        return (NoxesiumClientboundNetworking) instance;
    }

    /**
     * Sends the given packet, automatically detects the type of the packet based on the registered packets.
     */
    public static boolean send(@NotNull Player player, @NotNull NoxesiumPacket packet) {
        var type = getInstance().getPacketTypes().get(packet.getClass());
        if (type == null) return false;
        return type.sendClientboundAny(player, packet);
    }

    protected final Map<NoxesiumPayloadType<?>, Optional<NoxesiumEntrypoint>> entrypoints = new ConcurrentHashMap<>();

    @Override
    public void register(NoxesiumPayloadType<?> payloadType, @Nullable NoxesiumEntrypoint entrypoint) {
        super.register(payloadType, entrypoint);
        Preconditions.checkState(
                !entrypoints.containsKey(payloadType), "Cannot register payload type '" + payloadType + "' twice");
        entrypoints.put(payloadType, Optional.ofNullable(entrypoint));
    }

    /**
     * Checks if the connected player can receive packets of the given type.
     *
     * @param type The packet type
     * @return Whether the connected player can receive the packet
     */
    public abstract boolean canReceive(@NotNull Player player, @NotNull NoxesiumPayloadType<?> type);

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    public abstract <T extends NoxesiumPacket> boolean send(
            @NotNull Player player, @NotNull NoxesiumPayloadType<T> type, @NotNull T payload);
}
