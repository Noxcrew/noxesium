package com.noxcrew.noxesium.api.nms.network;

import com.google.common.base.Preconditions;
import com.noxcrew.noxesium.api.network.NoxesiumPacket;
import com.noxcrew.noxesium.api.nms.network.payload.NoxesiumPayloadType;
import net.kyori.adventure.text.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Sets up the Noxesium networking system.
 */
public abstract class NoxesiumNetworking {
    protected static NoxesiumNetworking instance;
    private final Map<Class<?>, NoxesiumPayloadType<?>> packetTypes = new HashMap<>();

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

    /**
     * Returns all packet types.
     */
    public Map<Class<?>, NoxesiumPayloadType<?>> getPacketTypes() {
        return packetTypes;
    }

    /**
     * Registers a new payload type.
     */
    public void register(NoxesiumPayloadType<?> payloadType) {
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
     * Returns the stream codec for components.
     */
    public abstract StreamCodec<RegistryFriendlyByteBuf, Component> getComponentStreamCodec();

    /**
     * Returns the stream codec for item stacks.
     */
    public abstract StreamCodec<RegistryFriendlyByteBuf, ItemStack> getItemStackStreamCodec();

    /**
     * Creates a new payload of the type specific to this platform.
     */
    public abstract <T extends NoxesiumPacket> NoxesiumPayloadType<T> createPayloadType(
            @NotNull String namespace, @NotNull String id, @NotNull StreamCodec<RegistryFriendlyByteBuf, T> codec, @NotNull Class<T> clazz, boolean clientToServer);
}
