package com.noxcrew.noxesium.api.fabric.network;

import com.noxcrew.noxesium.api.fabric.mixin.PayloadTypeRegistryExt;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Pair;

/**
 * A type of custom payload used by Noxesium for its packets.
 */
public class NoxesiumPayloadType<T extends NoxesiumPacket> {

    /**
     * The internal type of this payload.
     */
    public final CustomPacketPayload.Type<T> type;

    /**
     * The codec used for this payload.
     */
    public final StreamCodec<RegistryFriendlyByteBuf, T> codec;

    /**
     * Whether this payload is sent from client to server.
     */
    public final boolean clientToServer;

    /**
     * All listeners registered to this payload type.
     */
    private final Set<Pair<WeakReference<?>, TriConsumer<?, T, PacketContext>>> listeners =
            ConcurrentHashMap.newKeySet();

    /**
     * Creates a new Noxesium payload type which can be listened to
     * by custom packet handlers.
     */
    public NoxesiumPayloadType(
            CustomPacketPayload.Type<T> type, StreamCodec<RegistryFriendlyByteBuf, T> codec, boolean clientToServer) {
        this.type = type;
        this.codec = codec;
        this.clientToServer = clientToServer;
    }

    /**
     * Returns the id of this payload type.
     */
    public ResourceLocation id() {
        return type.id();
    }

    /**
     * Registers a receiver for this payload type.
     */
    public void register() {
        if (clientToServer) {
            PayloadTypeRegistry.playC2S().register(type, codec);
        } else {
            PayloadTypeRegistry.playS2C().register(type, codec);
            ClientPlayNetworking.registerReceiver(type, new NoxesiumPacketHandler<>());
        }
    }

    /**
     * Unregisters the receiver for this payload type.
     */
    public void unregister() {
        if (clientToServer) {
            unregisterPacket(PayloadTypeRegistry.playC2S(), type.id());
        } else {
            unregisterPacket(PayloadTypeRegistry.playS2C(), type.id());
            ClientPlayNetworking.unregisterReceiver(type.id());
        }
    }

    /**
     * Unregisters the packet with the given id from the given registry.
     */
    private static void unregisterPacket(PayloadTypeRegistry<RegistryFriendlyByteBuf> registry, ResourceLocation id) {
        ((PayloadTypeRegistryExt) registry).getPacketTypes().remove(id);
    }

    /**
     * Returns whether this type has listeners.
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    /**
     * Handles a new packet [payload] of this type being received with
     * [context].
     */
    public void handle(PacketContext context, Object payload) {
        var iterator = listeners.iterator();
        while (iterator.hasNext()) {
            var pair = iterator.next();
            var obj = pair.getKey().get();
            if (obj == null) {
                iterator.remove();
                continue;
            }
            acceptAny(pair.getValue(), obj, context, payload);
        }
    }

    /**
     * Registers a new listener to a packet payload of this type. Garbage collection
     * of this listener is tied to the lifetime of the reference. The reference object should
     * only ever be referenced from within the listener using the passed instance. This prevents
     * the listener from holding its own reference captive. If you do this the listener
     * will never be properly garbage collected.
     */
    public <R> void addListener(R reference, TriConsumer<R, T, PacketContext> listener) {
        listeners.removeIf((it) -> it.getKey().get() == null);
        listeners.add(Pair.of(new WeakReference<>(reference), listener));
    }

    /**
     * Casts [reference] to type [R] of [consumer].
     */
    private <R> void acceptAny(
            TriConsumer<R, T, PacketContext> consumer, Object reference, PacketContext context, Object payload) {
        consumer.accept((R) reference, (T) payload, context);
    }
}
