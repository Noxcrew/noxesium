package com.noxcrew.noxesium.api.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.NoxesiumEntrypoint;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadGroup;
import com.noxcrew.noxesium.api.network.payload.NoxesiumPayloadType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
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
    public void register(NoxesiumPayloadGroup group, @Nullable NoxesiumEntrypoint entrypoint) {
        for (var type : group.getPayloadTypes()) {
            var clazz = type.typeClass();
            Preconditions.checkState(
                    !packetTypes.containsKey(clazz), "Cannot register payload type '" + clazz + "' twice");
            packetTypes.put(clazz, type);
        }
    }

    /**
     * Unregisters a payload type.
     */
    public void unregister(NoxesiumPayloadGroup group) {
        for (var type : group.getPayloadTypes()) {
            packetTypes.remove(type.typeClass());
        }
    }

    /**
     * Indicates that it should be re-assessed whether the given payload type is lazy.
     */
    public abstract void markLazyActive(NoxesiumPayloadGroup payloadGroup);

    /**
     * Creates a new payload of the type specific to this platform.
     */
    public abstract <T extends NoxesiumPacket> NoxesiumPayloadType<T> createPayloadType(
            @NotNull NoxesiumPayloadGroup group, @NotNull Key id, @NotNull Class<T> clazz, boolean clientToServer);
}
