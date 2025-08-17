package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Sets up the Noxesium networking system.
 */
public abstract class NoxesiumNetworking {
    protected static NoxesiumNetworking instance;

    /**
     * Returns the singleton instance of this class.
     */
    public static NoxesiumNetworking getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get networking instance before it is defined");
        return instance;
    }

    /**
     * Sets the networking instance.
     */
    public static void setInstance(NoxesiumNetworking instance) {
        Preconditions.checkState(NoxesiumNetworking.instance == null, "Cannot set the networking instance twice!");
        NoxesiumNetworking.instance = instance;
    }

    protected final Map<Class<?>, NoxesiumPayloadType<?>> packetTypes = new ConcurrentHashMap<>();

    /**
     * Returns all packet types.
     */
    public Map<Class<?>, NoxesiumPayloadType<?>> getPacketTypes() {
        return packetTypes;
    }

    /**
     * Registers a new payload type.
     */
    public void register(NoxesiumPayloadType<?> payloadType, @Nullable NoxesiumEntrypoint entrypoint) {
        var clazz = payloadType.typeClass();
        Preconditions.checkState(!packetTypes.containsKey(clazz), "Cannot register payload type '" + clazz + "' twice");
        packetTypes.put(clazz, payloadType);
    }

    /**
     * Unregisters a payload type.
     */
    public void unregister(NoxesiumPayloadType<?> payloadType) {
        packetTypes.remove(payloadType.typeClass());
    }

    /**
     * Creates a new payload of the type specific to this platform.
     */
    public abstract <T extends NoxesiumPacket> NoxesiumPayloadType<T> createPayloadType(
            @NotNull String namespace, @NotNull String id, @NotNull Class<T> clazz, boolean clientToServer);
}
