package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
    public static boolean send(@Nullable NoxesiumServerPlayer player, @NotNull NoxesiumPacket packet) {
        if (player == null) return false;
        var type = getInstance().getPacketTypes().get(packet.getClass());
        if (type == null) return false;
        return type.sendClientboundAny(player, packet);
    }

    protected final Map<NoxesiumPayloadType<?>, NoxesiumEntrypoint> entrypoints = new HashMap<>();

    @Override
    public void register(NoxesiumPayloadType<?> payloadType, @Nullable NoxesiumEntrypoint entrypoint) {
        super.register(payloadType, entrypoint);
        Preconditions.checkState(
                !entrypoints.containsKey(payloadType), "Cannot register payload type '" + payloadType + "' twice");
        entrypoints.put(payloadType, entrypoint);
    }

    /**
     * Returns the entrypoint that registered the given payload type.
     */
    @Nullable
    public NoxesiumEntrypoint getPacketEntrypoint(@NotNull NoxesiumPayloadType<?> type) {
        return entrypoints.get(type);
    }

    /**
     * Returns the collection of channels registered for the given player.
     */
    public abstract Collection<String> getRegisteredChannels(@NotNull NoxesiumServerPlayer player);

    /**
     * Checks if the connected player can receive packets of the given type.
     *
     * @param packet The packet class
     * @return Whether the connected player can receive the packet
     */
    public boolean canReceive(@NotNull NoxesiumServerPlayer player, @NotNull Class<?> packet) {
        var type = packetTypes.get(packet);
        if (type == null) return false;
        return canReceive(player, type);
    }

    /**
     * Checks if the connected player can receive packets of the given type.
     *
     * @param type The packet type
     * @return Whether the connected player can receive the packet
     */
    public boolean canReceive(@NotNull NoxesiumServerPlayer player, @NotNull NoxesiumPayloadType<?> type) {
        // Prevent sending if the entrypoint that registered this packet is not known to this player!
        // This avoids situations where the client somehow has the correct channel without authenticating
        // properly with that endpoint.
        // If there is no entrypoint (or null which is for handshake packets) we always allow it!
        var entrypoint = entrypoints.get(type);
        if (entrypoint == null) return true;
        return player.getSupportedEntrypointIds().contains(entrypoint.getId());
    }

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    public abstract <T extends NoxesiumPacket> boolean send(
            @NotNull NoxesiumServerPlayer player, @NotNull NoxesiumPayloadType<T> type, @NotNull T payload);
}
