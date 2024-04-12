package com.noxcrew.noxesium.network.payload;

import com.noxcrew.noxesium.network.NoxesiumPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A type of custom payload used by Noxesium for its packets.
 */
public class NoxesiumPayloadType<T extends NoxesiumPacket> {

    /**
     * The internal type of this payload.
     */
    public final CustomPacketPayload.Type<T> type;

    /**
     * All listeners registered to this payload type.
     */
    private final Set<Pair<WeakReference<?>, TriConsumer<?, T, ClientPlayNetworking.Context>>> listeners = ConcurrentHashMap.newKeySet();

    /**
     * Creates a new Noxesium payload type which can be listened to
     * by custom packet handlers.
     */
    public NoxesiumPayloadType(CustomPacketPayload.Type<T> type) {
        this.type = type;
    }

    /**
     * Returns the id of this payload type.
     */
    public ResourceLocation id() {
        return type.id();
    }

    /**
     * Handles a new packet [payload] of this type being received with
     * [context].
     */
    public void handle(ClientPlayNetworking.Context context, Object payload) {
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
    public <R> void addListener(R reference, TriConsumer<R, T, ClientPlayNetworking.Context> listener) {
        listeners.removeIf((it) -> it.getKey().get() == null);
        listeners.add(Pair.of(new WeakReference<>(reference), listener));
    }

    /**
     * Casts [reference] to type [R] of [consumer].
     */
    private <R> void acceptAny(TriConsumer<R, T, ClientPlayNetworking.Context> consumer, Object reference, ClientPlayNetworking.Context context, Object payload) {
        consumer.accept((R) reference, (T) payload, context);
    }
}
