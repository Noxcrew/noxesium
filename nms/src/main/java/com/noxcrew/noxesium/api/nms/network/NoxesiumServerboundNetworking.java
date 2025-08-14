package com.noxcrew.noxesium.api.nms.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Sets up the Noxesium networking system.
 */
public abstract class NoxesiumServerboundNetworking {
    private static NoxesiumServerboundNetworking instance;
    private static final Map<Class<?>, NoxesiumPayloadType<?>> packetTypes = new HashMap<>();

    /**
     * Returns the singleton instance of this class.
     */
    public static NoxesiumServerboundNetworking getInstance() {
        Preconditions.checkNotNull(instance, "Cannot get networking instance before it is defined");
        return instance;
    }

    /**
     * Sets the networking instance.
     */
    public static void setInstance(NoxesiumServerboundNetworking instance) {
        Preconditions.checkState(
                NoxesiumServerboundNetworking.instance == null, "Cannot set the networking instance twice!");
        NoxesiumServerboundNetworking.instance = instance;
    }

    /**
     * Sends the given packet, automatically detects the type of the packet based on the registered packets.
     */
    public static boolean send(NoxesiumPacket packet) {
        var type = packetTypes.get(packet.getClass());
        if (type == null) return false;
        return type.sendAny(packet);
    }

    /**
     * Registers a new payload type.
     */
    public void register(NoxesiumPayloadType<?> payloadType) {
        var clazz = payloadType.getClass();
        Preconditions.checkState(!packetTypes.containsKey(clazz), "Cannot register payload type '" + clazz + "' twice");
        packetTypes.put(clazz, payloadType);
    }

    /**
     * Unregisters a payload type.
     */
    public void unregister(NoxesiumPayloadType<?> payloadType) {
        packetTypes.remove(payloadType.getClass());
    }

    /**
     * Returns the stream codec for components.
     */
    public abstract StreamCodec<RegistryFriendlyByteBuf, Component> getComponentStreamCodec();

    /**
     * Creates a new payload of the type specific to this platform.
     */
    public abstract <T extends NoxesiumPacket> NoxesiumPayloadType<T> createPayloadType(
            String namespace, String id, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean clientToServer);

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
