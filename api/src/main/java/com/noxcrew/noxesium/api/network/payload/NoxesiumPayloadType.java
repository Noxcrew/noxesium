package com.noxcrew.noxesium.api.network.payload;

import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.ConnectionProtocolType;
import com.noxcrew.noxesium.api.network.NoxesiumClientboundNetworking;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.network.NoxesiumServerboundNetworking;
import com.noxcrew.noxesium.api.network.json.JsonSerializedPacket;
import com.noxcrew.noxesium.api.player.NoxesiumServerPlayer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A type of custom payload used by Noxesium for its packets.
 */
public class NoxesiumPayloadType<T extends NoxesiumPacket> {
    /**
     * The group of this payload type.
     */
    @NotNull
    private final NoxesiumPayloadGroup group;

    /**
     * The id of this payload type.
     */
    @NotNull
    private final Key id;

    /**
     * The class of the packet type.
     */
    @NotNull
    public final Class<T> clazz;

    /**
     * Whether this payload is sent from client to server.
     */
    public final boolean clientToServer;

    /**
     * Whether this packet is JSON serialized.
     */
    public final boolean jsonSerialized;

    /**
     * Creates a new Noxesium payload type which can be listened to
     * by custom packet handlers.
     */
    public NoxesiumPayloadType(
            @NotNull NoxesiumPayloadGroup group, @NotNull Key id, @NotNull Class<T> clazz, boolean clientToServer) {
        this.group = group;
        this.id = id;
        this.clazz = clazz;
        this.clientToServer = clientToServer;
        this.jsonSerialized = clazz.isAnnotationPresent(JsonSerializedPacket.class);
    }

    /**
     * Returns the payload group of this type.
     */
    @NotNull
    public NoxesiumPayloadGroup getGroup() {
        return group;
    }

    /**
     * Returns the id of this specific payload type.
     */
    @NotNull
    public Key id() {
        return id;
    }

    /**
     * Returns the class of the packet payload type.
     */
    @NotNull
    public Class<T> typeClass() {
        return clazz;
    }

    /**
     * Registers a receiver for this payload type.
     */
    public void register(@Nullable NoxesiumEntrypoint entrypoint) {}

    /**
     * Unregisters the receiver for this payload type.
     */
    public void unregister() {}

    /**
     * Binds this packet to the given protocol.
     */
    public void bind(ConnectionProtocolType protocolType) {}

    /**
     * Unbinds this packet from the given protocol.
     */
    public void unbind(ConnectionProtocolType protocolType) {}

    /**
     * Creates a platform-specific payload object for this packet from the given [payload].
     */
    @Nullable
    public Object createClientboundAny(NoxesiumServerPlayer player, Object payload) {
        return NoxesiumClientboundNetworking.getInstance().create(player, this, (T) payload);
    }

    /**
     * Sends the given [payload] as the type of this payload.
     */
    public void sendServerboundAny(Object payload) {
        NoxesiumServerboundNetworking.getInstance().send(this, (T) payload);
    }
}
