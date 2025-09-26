package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;
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

    protected final Map<NoxesiumPayloadGroup, NoxesiumEntrypoint> entrypoints = new HashMap<>();

    @Override
    public void register(NoxesiumPayloadGroup group, @Nullable NoxesiumEntrypoint entrypoint) {
        super.register(group, entrypoint);
        Preconditions.checkState(
                !entrypoints.containsKey(group), "Cannot register payload group '" + group + "' twice");
        entrypoints.put(group, entrypoint);
    }

    /**
     * Returns the entrypoint that registered the given payload group.
     */
    @Nullable
    public NoxesiumEntrypoint getPacketEntrypoint(@NotNull NoxesiumPayloadGroup group) {
        return entrypoints.get(group);
    }

    /**
     * Returns the collection of channels registered for the given player.
     */
    public abstract Collection<String> getRegisteredChannels(@NotNull NoxesiumServerPlayer player);

    /**
     * Checks if the connected player can receive any packets of the given group the given class belongs to,
     * when the starting point is of the given class.
     *
     * @param clazz The packet class.
     * @return Whether the connected player can receive the packet
     */
    public <T extends NoxesiumPacket> boolean canReceive(
            @NotNull NoxesiumServerPlayer player, @NotNull Class<T> clazz) {
        var type = getPacketTypes().get(clazz);
        if (type != null) {
            // Check if any type within the group is supported!
            return type.getGroup().checkAnySupports(type, (it) -> canReceive(player, it));
        }
        return false;
    }

    /**
     * Checks if the connected player can receive packets of the specific given type.
     *
     * @param type The packet type
     * @return Whether the connected player can receive the packet
     */
    public <T extends NoxesiumPacket> boolean canReceive(
            @NotNull NoxesiumServerPlayer player, @NotNull NoxesiumPayloadType<T> type) {
        // Prevent sending if the entrypoint that registered this packet is not known to this player!
        // This avoids situations where the client somehow has the correct channel without authenticating
        // properly with that endpoint.
        // If there is no entrypoint (or null which is for handshake packets) we always allow it!
        var entrypoint = entrypoints.get(type.getGroup());
        if (entrypoint == null) return true;
        return player.getSupportedEntrypointIds().contains(entrypoint.getId());
    }

    /**
     * Sends this packet to the currently connected server, if possible. Returns whether the packet
     * was successfully sent or not.
     */
    public abstract <T extends NoxesiumPacket> void send(
            @NotNull NoxesiumServerPlayer player, @NotNull NoxesiumPayloadType<T> type, @NotNull T payload);
}
